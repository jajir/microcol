package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.ColonySightingPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.store.TaxPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Player model class. Class doesn't perform itself it just provide methods to
 * implement players behavior somewhere else.
 */
public abstract class Player {

    /** Independence must be declared before spring of this year. */
    public static final int INDEPENDENCE_DEADLINE_YEAR = 1800;

    /** Exact national and local support percentage needed for the revolt. */
    public static final int REQUIRED_SUPPORT_PERCENT = 50;

    private final Model model;

    private final String name;

    private final boolean computer;

    private boolean declaredIndependence;

    /** Guards the declaration against reentrant calls from callbacks. */
    private boolean declaringIndependence;

    private int gold;

    private final Map<String, Object> extraData = new HashMap<>();

    private final Visibility visibility;

    private final RecruitmentPool recruitment;

    private final Congress congress;

    /** Null for a Crown, which only reads its nation's state. */
    private final TaxState tax;

    private final Map<UnitType, Integer> unitPurchaseCounts = new HashMap<>();

    /**
     * Colonies remembered from actual observation, by location. Concurrent
     * because the interface paints while the model thread observes.
     */
    private final Map<Location, ColonySighting> colonySightings = new java.util.concurrent.ConcurrentHashMap<>();

    protected Player(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible,
            final RecruitmentPo recruitmentPo, final CongressPo congressPo,
            final TaxPo taxPo) {
        this.model = Preconditions.checkNotNull(model);
        this.name = Preconditions.checkNotNull(name);
        this.computer = computer;
        this.gold = initialGold;
        this.declaredIndependence = declaredIndependence;
        this.extraData.putAll(Preconditions.checkNotNull(extraData));
        if (this.extraData.containsKey("displayName")) {
            getDisplayName();
        }
        this.visibility = new Visibility(
                Preconditions.checkNotNull(visible, "Visible is null"));
        this.recruitment = recruitmentPo == null ? null
                : new RecruitmentPool(recruitmentPo);
        this.congress = congressPo == null ? null
                : new Congress(congressPo, model.getDifficulty());
        this.tax = taxPo == null ? null
                : new TaxState(taxPo, model.getDifficulty());
        if (recruitment != null) {
            try {
                recruitment.price(model.getDifficulty(), name);
                recruitment.validateBrewsterPool(getCongressMembers()
                        .contains(Founder.WILLIAM_BREWSTER));
            } catch (final ArithmeticException overflow) {
                throw new IllegalArgumentException(
                        "Recruitment counters exceed supported range",
                        overflow);
            }
        }
    }

    public static Player make(final PlayerPo playerPo, final Model model,
            final PlayerStore playerStore) {
        Preconditions.checkNotNull(playerPo.getVisible(),
                "Visible is null during creating '%s'", playerPo.getName());
        Preconditions.checkArgument(playerPo.getUnitPurchaseCounts() != null,
                "Missing unit purchase counts");
        if (playerPo.getWhosKingThisPlayerIs() == null) {
            Preconditions.checkArgument(playerPo.getRecruitment() != null,
                    "Missing colonial recruitment pool");
            Preconditions.checkArgument(playerPo.getCongress() != null,
                    "Missing colonial Congress");
            Preconditions.checkArgument(playerPo.getTax() != null,
                    "Missing colonial tax state");
            final Player player = new PlayerHuman(playerPo.getName(),
                    playerPo.isComputer(), playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(),
                    playerPo.getRecruitment(), playerPo.getCongress(),
                    playerPo.getTax());
            player.loadUnitPurchaseCounts(playerPo.getUnitPurchaseCounts());
            return player;
        } else {
            Preconditions.checkArgument(playerPo.getRecruitment() == null,
                    "Kings cannot have recruitment pools");
            Preconditions.checkArgument(playerPo.getCongress() == null,
                    "Kings cannot have Congress");
            Preconditions.checkArgument(playerPo.getTax() == null,
                    "Kings cannot have their own tax state");
            Preconditions.checkArgument(
                    playerPo.getUnitPurchaseCounts().isEmpty(),
                    "Kings cannot have purchase counts");
            final Player subdued = playerStore
                    .getPlayerByName(playerPo.getWhosKingThisPlayerIs());
            return new PlayerKing(playerPo.getName(), playerPo.isComputer(),
                    playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), subdued,
                    playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet());
        }
    }

    /**
     * Accumulated exploration of terrain. It stays true after the player's
     * units leave; use {@link #isCurrentlyVisible(Location)} for foreign units
     * and colonies.
     *
     * @param location required location
     * @return whether the player has ever seen the terrain at the location
     */
    public boolean isVisible(final Location location) {
        model.isValid(location);
        return visibility.isVisible(location);
    }

    /**
     * Present sight: the effective sight of every own unit on the map and the
     * owned colony tile with its eight neighbors, even when all residents are
     * inside. It is computed from live ownership and positions, so losing a
     * unit or colony removes its sight immediately.
     *
     * @param location required location
     * @return whether the player observes the location right now
     */
    public boolean isCurrentlyVisible(final Location location) {
        Preconditions.checkNotNull(location);
        if (!model.isValid(location)) {
            return false;
        }
        return model.getAllUnits().stream()
                .anyMatch(unit -> unit.getOwner().equals(this)
                        && unit.isAtPlaceLocation()
                        && unit.getVisibleLocations().contains(location))
                || model.getColonies(this).stream().anyMatch(colony -> colony
                        .getLocation().getDistanceChebyshev(location) <= 1);
    }

    /**
     * @param location required location
     * @return units at the location when it is in present sight, otherwise
     *         empty list
     */
    public List<Unit> getObservedUnitsAt(final Location location) {
        return isCurrentlyVisible(location) ? model.getUnitsAt(location)
                : List.of();
    }

    /**
     * Colony known at the location: the live colony inside present sight,
     * otherwise the remembered snapshot of the last observation.
     *
     * @param location required location
     * @return known colony or empty when none was ever observed there
     */
    public Optional<ColonySighting> getKnownColonyAt(final Location location) {
        Preconditions.checkNotNull(location);
        if (isCurrentlyVisible(location)) {
            return model.getColonyAt(location).map(this::sightingOf);
        }
        return Optional.ofNullable(colonySightings.get(location));
    }

    /**
     * @return every colony known to the player: live inside present sight and
     *         remembered snapshots outside it
     */
    public List<ColonySighting> getKnownColonies() {
        final Map<Location, ColonySighting> known = new HashMap<>();
        colonySightings.forEach((location, sighting) -> {
            if (!isCurrentlyVisible(location)) {
                known.put(location, sighting);
            }
        });
        model.getColonies().stream()
                .filter(colony -> isCurrentlyVisible(colony.getLocation()))
                .forEach(colony -> known.put(colony.getLocation(),
                        sightingOf(colony)));
        return List.copyOf(known.values());
    }

    private ColonySighting sightingOf(final Colony colony) {
        return ColonySighting.of(colony, model.getCalendar());
    }

    /**
     * Record colonies inside present sight and forget remembered ones that are
     * observed to be gone. Colonies outside present sight are untouched.
     */
    void observeColonies() {
        for (final Colony colony : model.getColonies()) {
            if (colony.getOwner().equals(this)) {
                // Own colonies are always live; nothing stale to remember.
                colonySightings.remove(colony.getLocation());
                // Colony sight is explored terrain too, and stays so after
                // the colony is lost.
                final List<Location> around = new java.util.ArrayList<>(
                        colony.getLocation().getNeighbors());
                around.add(colony.getLocation());
                visibility.reveal(around.stream().filter(model::isValid)
                        .collect(java.util.stream.Collectors.toList()));
            } else if (isCurrentlyVisible(colony.getLocation())) {
                colonySightings.put(colony.getLocation(), sightingOf(colony));
            }
        }
        colonySightings.keySet()
                .removeIf(location -> isCurrentlyVisible(location)
                        && model.getColonyAt(location).isEmpty());
    }

    /** Remember a colony the player was directly told about. */
    void recordColony(final Colony colony) {
        colonySightings.put(colony.getLocation(), sightingOf(colony));
    }

    /** Forget a colony the player was directly told is gone. */
    void forgetColony(final Location location) {
        colonySightings.remove(location);
    }

    void restoreColonyKnowledge(final List<ColonySightingPo> saved) {
        Preconditions.checkArgument(saved != null,
                "Missing remembered colonies");
        colonySightings.clear();
        for (final ColonySightingPo po : saved) {
            final ColonySighting sighting = ColonySighting.restore(po, model);
            Preconditions
                    .checkArgument(
                            colonySightings.put(sighting.getLocation(),
                                    sighting) == null,
                            "Duplicate remembered colony");
        }
    }

    /**
     * Method reveals map visible for given unit.
     *
     * @param unit required unit. Unit have to be on map.
     */
    public void revealMapForUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        Preconditions.checkState(unit.getOwner().equals(this),
                "Unit's owher '%s' is to same as '%s'", unit.getOwner(), this);
        visibility.makeVisibleMapForUnit(unit);
        observeColonies();
    }

    public PlayerPo save() {
        final PlayerPo out = new PlayerPo();
        out.setName(name);
        out.setComputer(computer);
        out.setGold(gold);
        out.setDeclaredIndependence(declaredIndependence);
        out.getExtraData().putAll(extraData);
        out.setVisible(new VisibilityPo());
        if (recruitment != null) {
            out.setRecruitment(recruitment.save());
        }
        if (congress != null) {
            out.setCongress(congress.save());
        }
        if (tax != null) {
            out.setTax(tax.save());
        }
        final Map<String, Integer> counts = new HashMap<>();
        unitPurchaseCounts
                .forEach((type, count) -> counts.put(type.name(), count));
        out.setUnitPurchaseCounts(counts);
        colonySightings.values().stream().sorted(java.util.Comparator
                .comparingInt((ColonySighting sighting) -> sighting
                        .getLocation().getX())
                .thenComparingInt(sighting -> sighting.getLocation().getY()))
                .forEach(sighting -> out.getColonySightings()
                        .add(sighting.save()));
        visibility.store(out.getVisible(), model.getMap().getMaxLocationX(),
                model.getMap().getMaxLocationY());
        return out;
    }

    public static String normalizeDisplayName(final String value) {
        final String normalized = ColonyNames.normalize(value);
        Preconditions.checkArgument(!normalized.isEmpty(),
                "Name cannot be empty");
        Preconditions.checkArgument(Character.codePointCount(normalized, 0,
                normalized.length()) <= 64, "Name too long");
        Preconditions.checkArgument(
                normalized.codePoints()
                        .noneMatch(point -> Character.isISOControl(point)
                                || (point >= 0xD800 && point <= 0xDFFF)),
                "Name contains invalid code points");
        return normalized;
    }

    public void setDisplayName(final String value) {
        extraData.put("displayName", normalizeDisplayName(value));
    }

    public String getDisplayName() {
        if (!extraData.containsKey("displayName")) {
            return name;
        }
        Object value = extraData.get("displayName");
        Preconditions.checkArgument(value instanceof String,
                "Invalid display name");
        return normalizeDisplayName((String) value);
    }

    public String getName() {
        return name;
    }

    public boolean isComputer() {
        return computer;
    }

    public boolean isHuman() {
        return !computer;
    }

    public boolean isKing() {
        return this instanceof PlayerKing;
    }

    public List<Unit> getUnits() {
        return model.getUnitsOwnedBy(this, false);
    }

    public List<Unit> getAllUnits() {
        return model.getUnitsOwnedBy(this, true);
    }

    public int getNumberOfMilitaryUnits() {
        return (int) getAllUnits().stream()
                .filter(unit -> unit.getType().canAttack()).count();
    }

    public Map<Location, List<Unit>> getUnitsAt() {
        return model.getUnitsAt(this);
    }

    public List<Unit> getUnitsAt(final Location location) {
        return model.getUnitsAt(this, location);
    }

    public Optional<Colony> getColoniesAt(final Location location) {
        return model.getColoniesAt(location, this);
    }

    public List<Colony> getColonies() {
        return model.getColonies(this);
    }

    public List<Unit> getEnemyUnits() {
        return model.getEnemyUnits(this, false);
    }

    public Map<Location, List<Unit>> getEnemyUnitsAt() {
        return model.getEnemyUnitsAt(this);
    }

    public List<Unit> getEnemyUnitsAt(final Location location) {
        return model.getEnemyUnitsAt(this, location);
    }

    boolean startTurn() {
        final boolean hadFranklin = getCongressMembers()
                .contains(Founder.BENJAMIN_FRANKLIN);
        getAllUnits().stream().filter(Unit::isShip).map(Ship.class::cast)
                .collect(java.util.stream.Collectors.toList()).forEach(ship -> {
                    ship.advanceVoyage();
                    ship.advanceRepair();
                });
        if (!settleEarnedImmigrants()) {
            return false;
        }
        if (!settleTaxCheckpoint()) {
            return false;
        }
        finishTurnPreparation(hadFranklin);
        return true;
    }

    /**
     * Continue a preparation that paused for an earned-immigrant choice: the
     * tax checkpoint follows, then the remaining production and unit work.
     *
     * @return false when a tax demand now waits for the player's answer
     */
    boolean continueAfterImmigration() {
        if (!settleTaxCheckpoint()) {
            return false;
        }
        finishTurnPreparation();
        return true;
    }

    void finishTurnPreparation() {
        finishTurnPreparation(
                getCongressMembers().contains(Founder.BENJAMIN_FRANKLIN));
    }

    /**
     * Count one own update toward the next Crown demand. A computer player
     * accepts through the same state change; a human demand is frozen and waits
     * for an explicit answer.
     *
     * @return false when a human demand is pending
     */
    private boolean settleTaxCheckpoint() {
        if (tax == null || declaredIndependence) {
            return true;
        }
        Preconditions.checkState(!tax.isPending(),
                "A tax demand is already pending");
        if (!tax.advance(!getColonies().isEmpty(), model.getDifficulty())
                || tax.getRate() >= TaxState.MAX_RATE) {
            return true;
        }
        final int serial = tax.issue();
        tax.setPending(freezeTaxDemand(quoteTaxDemand(serial)));
        if (isComputer()) {
            // The computer answers through the very operation a human uses.
            applyTaxAcceptance();
            return true;
        }
        return false;
    }

    /** Raise the rate to the pending proposal once and clear the demand. */
    private void applyTaxAcceptance() {
        tax.setRate(tax.getPending().getProposedRate());
        tax.clearPending();
    }

    private static TaxState.Frozen freezeTaxDemand(final TaxDemand demand) {
        final TaxProtest protest = demand.getProtest().orElse(null);
        return protest == null
                ? new TaxState.Frozen(demand.getProposedRate(), null, -1, 0, 0)
                : new TaxState.Frozen(demand.getProposedRate(),
                        protest.getGoods(), protest.getColonyId(),
                        protest.getQuantity(), protest.getBid());
    }

    /** Quote a demand from the live rate, stock and market. */
    private TaxDemand quoteTaxDemand(final int serial) {
        return new TaxDemand(this, serial, tax.getRate(), tax.nextRate(),
                selectProtest().orElse(null));
    }

    /**
     * Greatest full-stock value, whole stock times the quoted bid before tax,
     * among tradable stock of commodities that are not yet boycotted in owned
     * colonies; ties go to the lower goods order, then the lower colony
     * identity.
     */
    private Optional<TaxProtest> selectProtest() {
        Colony bestColony = null;
        GoodsType bestType = null;
        long bestValue = -1;
        for (final Colony colony : getColonies()) {
            for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
                final int stock = colony.getWarehouse().getGoods(type)
                        .getAmount();
                if (stock <= 0 || isBoycotted(type)) {
                    continue;
                }
                final long value = (long) stock * model.getEurope()
                        .getGoodsTradeForType(type, getEuropeanNation())
                        .getSellPrice();
                if (bestColony == null || value > bestValue
                        || value == bestValue && isStableBefore(type, colony,
                                bestType, bestColony)) {
                    bestColony = colony;
                    bestType = type;
                    bestValue = value;
                }
            }
        }
        if (bestColony == null) {
            return Optional.empty();
        }
        // Only the winner is described. If its arrears cannot be represented
        // there is no protest; another commodity is never substituted.
        try {
            return Optional.of(new TaxProtest(bestType, bestColony.getId(),
                    bestColony.getName(),
                    bestColony.getWarehouse().getGoods(bestType).getAmount(),
                    model.getEurope()
                            .getGoodsTradeForType(bestType, getEuropeanNation())
                            .getSellPrice(),
                    bestColony
                            .previewProtestLiberty(TaxProtest.LIBERTY_POINTS)));
        } catch (final ArithmeticException overflow) {
            return Optional.empty();
        }
    }

    private static boolean isStableBefore(final GoodsType type,
            final Colony colony, final GoodsType other,
            final Colony otherColony) {
        final int byGoods = Integer.compare(GoodsType.GOOD_TYPES.indexOf(type),
                GoodsType.GOOD_TYPES.indexOf(other));
        return byGoods != 0 ? byGoods < 0
                : colony.getId() < otherColony.getId();
    }

    private void finishTurnPreparation(final boolean hadFranklin) {
        if (congress != null && !getColonies().isEmpty()) {
            congress.activate();
        }
        if (congress != null && congress.isActivated()) {
            int projectedBells = 0;
            for (final Colony colony : getColonies()) {
                projectedBells = Math.addExact(projectedBells,
                        Math.max(0,
                                colony.getGoodsStats()
                                        .getStatsByType(GoodsType.BELL)
                                        .getRowProduction()));
            }
            congress.checkCredit(projectedBells);
            if (congress.selected().orElse(null) == Founder.JOHN_PAUL_JONES
                    && congress.willJoinAfter(projectedBells,
                            model.getDifficulty())) {
                Preconditions.checkState(model.canAddUnitInEurope(),
                        "No unit IDs remain for Jones's frigate");
            }
            if (congress.selected().orElse(null) == Founder.WILLIAM_BREWSTER
                    && congress.willJoinAfter(projectedBells,
                            model.getDifficulty())) {
                recruitment.checkBrewsterJoin();
            }
        }
        getColonies().forEach(colony -> colony.startTurn());
        if (!isKing()) {
            // After colony production, so this update's custom-house sales
            // count as trades affecting the national markets. A scripted
            // actor that is not a real colonial nation has no market.
            EuropeanNation.findByPlayerName(getName()).ifPresent(
                    nation -> model.getEurope().recoverMarkets(nation));
        }
        settleCongress();
        getAllUnits().forEach(unit -> unit.startTurn());
        if (hadFranklin) {
            model.prepareFranklinPeaceOffers(this);
        }
    }

    void activateCongressAfterFirstColony() {
        if (congress != null) {
            congress.activate();
        }
    }

    public List<FounderOffer> getFounderOffers() {
        return congress == null ? List.of() : congress.offers(this);
    }

    public List<Founder> getCongressMembers() {
        return congress == null ? List.of() : congress.members();
    }

    int getEffectiveSpeed(final UnitType type) {
        return type.getSpeed() + (type.isShip()
                && getCongressMembers().contains(Founder.FERDINAND_MAGELLAN) ? 1
                        : 0);
    }

    /**
     * Exact population-weighted rebel support of this nation. Includes every
     * colony whose last colonial affiliation is this player, also while the
     * Crown occupies it. Read-only; the Crown itself has none.
     */
    public RebelSupport getNationalRebelSupport() {
        RebelSupport out = RebelSupport.NONE;
        if (isKing()) {
            return out;
        }
        for (final Colony colony : model.getColonies()) {
            if (colony.getLastColonialAffiliation() == this) {
                out = out.plus(colony.getRebelSupport());
            }
        }
        return out;
    }

    public int getCongressBells() {
        return congress == null ? 0 : congress.bells();
    }

    public int getCongressTarget() {
        return congress == null ? 0 : congress.target(model.getDifficulty());
    }

    public Optional<Founder> getSelectedFounder() {
        return congress == null ? Optional.empty() : congress.selected();
    }

    public boolean canChooseFounder(final FounderOffer offer) {
        return congress != null && model.isGameRunning()
                && model.getCurrentPlayer() == this
                && model.getPlayers().stream()
                        .anyMatch(player -> player == this)
                && congress.canChoose(this, offer);
    }

    public void chooseFounder(final FounderOffer offer) {
        Preconditions.checkNotNull(offer);
        model.checkTurnPreparationComplete();
        Preconditions.checkState(canChooseFounder(offer),
                "Founder offer is unavailable");
        congress.choose(this, offer);
    }

    public boolean isJonesDeliveryPending() {
        return congress != null && congress.isJonesDeliveryPending();
    }

    void checkCanCreditProducedBells(final int produced) {
        if (congress != null) {
            congress.checkCredit(produced);
        }
    }

    void creditProducedBells(final int produced) {
        if (congress != null) {
            congress.credit(produced);
        }
    }

    private void settleCongress() {
        if (congress == null) {
            return;
        }
        if (congress.canJoin(model.getDifficulty())) {
            final Founder selected = congress.selected().orElseThrow();
            if (selected == Founder.JOHN_PAUL_JONES) {
                Preconditions.checkState(model.canAddUnitInEurope(),
                        "No unit IDs remain for Jones's frigate");
            }
            final Founder arrived = congress.join(model.getDifficulty());
            if (arrived == Founder.WILLIAM_BREWSTER) {
                recruitment.applyBrewster();
            }
            if (arrived == Founder.SIEUR_DE_LA_SALLE) {
                getColonies().forEach(Colony::grantLaSalleStockade);
            }
            if (arrived == Founder.JAKOB_FUGGER && tax != null) {
                // Only the arrears that exist at this arrival are removed;
                // later protests are boycotted as usual.
                tax.clearArrears();
            }
            if (arrived == Founder.HERNANDO_DE_SOTO) {
                getAllUnits().stream().filter(Unit::isAtPlaceLocation)
                        .forEach(this::revealMapForUnit);
            }
            model.getTurnEventStore()
                    .add(TurnEventProvider.getFounderArrived(this, arrived));
        }
        if (congress.isJonesDeliveryPending()) {
            if (!model.canAddUnitInEurope()) {
                return;
            }
            if (!declaredIndependence) {
                model.addUnitInEurope(UnitType.FRIGATE, this);
                congress.markJonesDelivered();
                model.getTurnEventStore()
                        .add(TurnEventProvider.getNewUnitInEurope(this));
            } else {
                model.deliverJonesFrigateAtCoastalColony(this)
                        .ifPresent(unit -> congress.markJonesDelivered());
            }
        }
    }

    private boolean settleEarnedImmigrants() {
        if (recruitment == null || !canTradeInEurope()) {
            return true;
        }
        while (hasPendingImmigrant()) {
            final int threshold = recruitment
                    .checkCanSettleEarned(model.getDifficulty(), name);
            Preconditions.checkState(model.canAddUnitInEurope(),
                    "No unit IDs remain for an earned immigrant");
            if (isBrewster() && !isComputer()) {
                recruitment.markPendingEarnedChoice(model.getDifficulty(),
                        name);
                return false;
            }
            final int slot = recruitment.chooseEarnedSlot();
            settleEarnedChoice(slot, threshold);
        }
        return true;
    }

    private void settleEarnedChoice(final int slot, final int threshold) {
        final UnitType type = recruitment.getCandidateType(slot);
        model.addUnitInEurope(type, this);
        recruitment.settleEarned(slot, threshold, isBrewster());
        model.getTurnEventStore()
                .add(TurnEventProvider.getNewUnitInEurope(this));
    }

    private boolean isBrewster() {
        return getCongressMembers().contains(Founder.WILLIAM_BREWSTER);
    }

    boolean hasFrozenEarnedChoice() {
        return recruitment != null && recruitment.isPendingEarnedChoice();
    }

    void validateFrozenEarnedChoice() {
        Preconditions.checkArgument(
                hasFrozenEarnedChoice() && isBrewster() && !isComputer()
                        && canTradeInEurope() && hasPendingImmigrant(),
                "Invalid saved earned immigration choice");
    }

    /** @return the three frozen free candidates during an earned choice */
    public List<EarnedImmigrationOffer> getEarnedImmigrationOffers() {
        return model.isAwaitingImmigrationChoice()
                && model.getCurrentPlayer() == this && hasFrozenEarnedChoice()
                        ? recruitment.earnedOffers(this, model.getDifficulty(),
                                name)
                        : List.of();
    }

    public boolean canChooseEarnedImmigrant(
            final EarnedImmigrationOffer offer) {
        if (offer == null || !model.isAwaitingImmigrationChoice()
                || model.getCurrentPlayer() != this
                || model.getPlayers().stream().noneMatch(p -> p == this)
                || !hasFrozenEarnedChoice() || !model.canAddUnitInEurope()) {
            return false;
        }
        try {
            recruitment.checkCanSettleEarned(model.getDifficulty(), name);
            return recruitment.isCurrentEarnedOffer(offer, this,
                    model.getDifficulty(), name);
        } catch (final ArithmeticException | IllegalStateException invalid) {
            return false;
        }
    }

    public void chooseEarnedImmigrant(final EarnedImmigrationOffer offer) {
        synchronized (model) {
            Preconditions.checkNotNull(offer);
            Preconditions.checkState(canChooseEarnedImmigrant(offer),
                    "Earned immigration choice is unavailable or stale");
            final int threshold = recruitment
                    .checkCanSettleEarned(model.getDifficulty(), name);
            model.beginImmigrationChoice(this);
            settleEarnedChoice(offer.getSlot(), threshold);
            if (hasPendingImmigrant()) {
                recruitment.markPendingEarnedChoice(model.getDifficulty(),
                        name);
                model.continueImmigrationChoice();
            } else {
                model.finishImmigrationChoice(this);
            }
        }
    }

    public int getImmigrationCrosses() {
        Preconditions.checkState(recruitment != null,
                "Kings have no immigration pool");
        return recruitment.getCrosses();
    }

    public int getImmigrationTarget() {
        Preconditions.checkState(recruitment != null,
                "Kings have no immigration pool");
        return recruitment.getTarget(model.getDifficulty(), name);
    }

    public boolean hasPendingImmigrant() {
        return recruitment != null
                && recruitment.hasReachedTarget(model.getDifficulty(), name);
    }

    void checkCanCreditProducedCrosses(final int produced) {
        if (recruitment != null) {
            recruitment.checkCanCreditCrosses(produced);
        }
    }

    void creditProducedCrosses(final int produced) {
        if (recruitment != null) {
            recruitment.creditCrosses(produced);
        }
    }

    /**
     * @return the single tax percentage of this nation; a Crown reports the
     *         rate of the nation it rules
     */
    public int getTaxRate() {
        return tax.getRate();
    }

    /** Controlled-test and Crown-adapter write access to the nation's rate. */
    void setTaxRate(final int percentage) {
        Preconditions.checkState(tax != null, "Only a nation has a tax rate");
        tax.setRate(percentage);
    }

    /**
     * Controlled-test access: record arrears, which boycott the commodity.
     * Gameplay creates arrears only through an answered protest.
     */
    void addTaxArrears(final GoodsType type, final int owed) {
        Preconditions.checkState(tax != null, "Only a nation owes arrears");
        Preconditions.checkArgument(
                GoodsType.BUYABLE_GOOD_TYPES.contains(type)
                        && owed >= TaxProtest.MIN_ARREARS,
                "Invalid tax arrears");
        tax.addArrears(type, owed);
    }

    /** @return whether the commodity is boycotted for this nation */
    public boolean isBoycotted(final GoodsType type) {
        return tax != null && tax.isBoycotted(type);
    }

    /**
     * @return gold owed per boycotted commodity, in stable goods order; empty
     *         for a Crown or a nation without boycotts
     */
    public Map<GoodsType, Integer> getTaxArrears() {
        return tax == null ? Map.of() : tax.getArrears();
    }

    /**
     * Quote the frozen back taxes of one boycotted commodity.
     *
     * @param type the commodity the player wants to pay for
     * @return the exact amount and demand generation, or empty when the
     *         commodity is not boycotted
     */
    public Optional<TaxArrearsQuote> quoteTaxArrears(final GoodsType type) {
        Preconditions.checkNotNull(type);
        if (tax == null || !tax.isBoycotted(type)) {
            return Optional.empty();
        }
        return Optional.of(new TaxArrearsQuote(this, type,
                tax.getArrears().get(type), tax.getIssued()));
    }

    /** @return whether paying this exact quote is possible right now */
    public boolean canPayTaxArrears(final TaxArrearsQuote quote) {
        try {
            checkTaxArrearsPayable(quote);
            return true;
        } catch (final IllegalStateException | NullPointerException stale) {
            return false;
        }
    }

    /**
     * Pay the frozen arrears of one commodity once and lift only its boycott.
     * Tax rate, other arrears and Congress stay as they are. A stale or
     * repeated quote changes nothing.
     *
     * @param quote the snapshot the player reviewed
     * @throws IllegalStateException when the quote is stale or gold is short
     */
    public void payTaxArrears(final TaxArrearsQuote quote) {
        synchronized (model) {
            checkTaxArrearsPayable(quote);
            // Lift the boycott before the gold event, so listeners see both
            // completed changes and a reentrant confirmation finds no debt.
            tax.removeArrears(quote.getGoods());
            setGold(getGold() - quote.getAmount());
        }
    }

    private void checkTaxArrearsPayable(final TaxArrearsQuote quote) {
        Preconditions.checkNotNull(quote, "Tax arrears quote is null");
        model.checkGameRunning();
        model.checkTurnPreparationComplete();
        model.checkCurrentPlayer(this);
        Preconditions.checkState(quote.getPlayer() == this && tax != null,
                "The arrears quote belongs to another nation");
        Preconditions.checkState(canTradeInEurope(),
                "European trade is unavailable");
        final Integer owed = tax.getArrears().get(quote.getGoods());
        Preconditions.checkState(
                owed != null && owed == quote.getAmount()
                        && tax.getIssued() == quote.getGeneration(),
                "The back taxes changed");
        Preconditions.checkState(getGold() >= quote.getAmount(),
                "Not enough gold to pay %s", quote.getAmount());
    }

    boolean hasPendingTaxDemand() {
        return tax != null && tax.isPending();
    }

    /**
     * Check a loaded unresolved demand against the whole position. Called once
     * the colonies exist.
     */
    void validateFrozenTaxDemand() {
        Preconditions.checkArgument(
                hasPendingTaxDemand() && !isComputer() && !declaredIndependence
                        && !hasFrozenEarnedChoice() && !hasPendingImmigrant(),
                "Invalid saved tax demand");
        final TaxState.Frozen frozen = tax.getPending();
        if (frozen.hasProtest()) {
            final Colony colony = getColonies().stream().filter(
                    candidate -> candidate.getId() == frozen.getColonyId())
                    .findAny().orElse(null);
            Preconditions.checkArgument(colony != null && colony.isValid(),
                    "Saved tax protest names an unavailable colony");
            final int stock = colony.getWarehouse().getGoods(frozen.getGoods())
                    .getAmount();
            Preconditions.checkArgument(
                    stock > 0 && Math.min(TaxProtest.MAX_DESTROYED,
                            stock) == frozen.getQuantity(),
                    "Saved tax protest disagrees with the colony stock");
        }
    }

    /**
     * @return the unresolved demand the player must answer, with its frozen
     *         consequence, or empty when none is waiting for this player now
     */
    public Optional<TaxDemand> getTaxDemand() {
        if (!isTaxDecisionCurrent()) {
            return Optional.empty();
        }
        final TaxState.Frozen frozen = tax.getPending();
        TaxProtest protest = null;
        if (frozen.hasProtest()) {
            protest = resolveProtest(frozen).orElse(null);
        }
        return Optional.of(new TaxDemand(this, tax.getIssued(), tax.getRate(),
                frozen.getProposedRate(), protest));
    }

    private boolean isTaxDecisionCurrent() {
        return tax != null && tax.isPending() && model.isAwaitingTaxDecision()
                && model.getCurrentPlayer() == this
                && model.getPlayers().stream().anyMatch(p -> p == this)
                && !declaredIndependence;
    }

    private Optional<TaxProtest> resolveProtest(final TaxState.Frozen frozen) {
        final Colony colony = getColonies().stream()
                .filter(candidate -> candidate.getId() == frozen.getColonyId())
                .findAny().orElse(null);
        if (colony == null || !colony.isValid()
                || isBoycotted(frozen.getGoods())) {
            return Optional.empty();
        }
        final int stock = colony.getWarehouse().getGoods(frozen.getGoods())
                .getAmount();
        // The frozen consequence only holds while it destroys the same amount.
        if (stock <= 0 || Math.min(TaxProtest.MAX_DESTROYED, stock) != frozen
                .getQuantity()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new TaxProtest(frozen.getGoods(), colony.getId(),
                    colony.getName(), stock, frozen.getBid(),
                    colony.previewProtestLiberty(TaxProtest.LIBERTY_POINTS)));
        } catch (final ArithmeticException overflow) {
            return Optional.empty();
        }
    }

    private void checkAcceptable(final TaxDemand demand) {
        Preconditions.checkNotNull(demand, "Tax demand is null");
        Preconditions.checkState(isTaxDecisionCurrent(),
                "No tax demand is waiting for this player");
        final TaxState.Frozen frozen = tax.getPending();
        Preconditions
                .checkState(
                        demand.getPlayer() == this
                                && demand.getSerial() == tax.getIssued()
                                && demand.getCurrentRate() == tax.getRate()
                                && demand.getProposedRate() == frozen
                                        .getProposedRate(),
                        "The tax demand changed");
    }

    private TaxProtest checkProtestable(final TaxDemand demand) {
        checkAcceptable(demand);
        final TaxState.Frozen frozen = tax.getPending();
        final TaxProtest quoted = demand.getProtest().orElseThrow(
                () -> new IllegalStateException("There is nothing to protest"));
        Preconditions.checkState(frozen.hasProtest(),
                "There is nothing to protest");
        final TaxProtest resolved = resolveProtest(frozen).orElseThrow(
                () -> new IllegalStateException("The protested stock is gone"));
        final TaxProtest live = selectProtest().orElse(null);
        Preconditions.checkState(
                quoted.equals(resolved) && resolved.equals(live),
                "The protest consequence changed");
        Preconditions.checkState(
                tax.canAddArrears(resolved.getGoods(), resolved.getArrears()),
                "Tax arrears exceed the supported range");
        return resolved;
    }

    /** @return whether accepting this exact demand is possible right now */
    public boolean canAcceptTaxDemand(final TaxDemand demand) {
        try {
            checkAcceptable(demand);
            return true;
        } catch (final IllegalStateException | NullPointerException stale) {
            return false;
        }
    }

    /** @return whether protesting this exact demand is possible right now */
    public boolean canProtestTaxDemand(final TaxDemand demand) {
        try {
            checkProtestable(demand);
            return true;
        } catch (final IllegalStateException | NullPointerException stale) {
            return false;
        }
    }

    /**
     * Accept the demand: the rate rises once and the paused preparation
     * continues. A stale or repeated demand changes nothing.
     *
     * @param demand the snapshot the player reviewed
     * @throws IllegalStateException when the demand is stale or not current
     */
    public void acceptTaxDemand(final TaxDemand demand) {
        synchronized (model) {
            checkAcceptable(demand);
            model.beginTaxChoice(this);
            applyTaxAcceptance();
            model.finishTaxChoice(this);
        }
    }

    /**
     * Protest the demand: the quoted stock is destroyed, the colony gains
     * liberty, the commodity is boycotted with frozen arrears and the rate is
     * unchanged. A stale or repeated demand changes nothing.
     *
     * @param demand the snapshot the player reviewed
     * @throws IllegalStateException when the consequence changed or is missing
     */
    public void protestTaxDemand(final TaxDemand demand) {
        synchronized (model) {
            final TaxProtest protest = checkProtestable(demand);
            final Colony colony = getColonies().stream().filter(
                    candidate -> candidate.getId() == protest.getColonyId())
                    .findAny().orElseThrow();
            model.beginTaxChoice(this);
            colony.getWarehouse().removeGoods(
                    Goods.of(protest.getGoods(), protest.getQuantity()));
            colony.addProtestLiberty(TaxProtest.LIBERTY_POINTS);
            tax.addArrears(protest.getGoods(), protest.getArrears());
            tax.clearPending();
            model.finishTaxChoice(this);
        }
    }

    public void endTurn() {
        model.checkGameRunning();
        model.checkCurrentPlayer(this);
        model.endTurn();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Player)) {
            return false;
        }

        final Player player = (Player) object;

        return name.equals(player.name) && computer == player.computer;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name)
                .add("computer", computer).toString();
    }

    /**
     * Get information if it's possible to sail to given location. Method verify
     * that given location is empty sea or sea occupied by player's ships.
     *
     * @param target required target location
     * @return return <code>true</code> when it's possible to sail at given
     *         location otherwise return <code>false</code>.
     */
    public boolean isPossibleToSailAt(final Location target) {
        final TerrainType t = model.getMap().getTerrainTypeAt(target);
        if (t.isOrdinaryWater()) {
            return isItPlayersUnits(model.getUnitsAt(target));
        } else {
            return false;
        }
    }

    /**
     * Find if list of units could belong to this user.
     *
     * @param units required list of units
     * @return return <code>false</code> when list contains at least one unit
     *         which not belongs to this player otherwise return
     *         <code>true</code>.
     */
    public boolean isItPlayersUnits(final List<Unit> units) {
        return !units.stream().filter(unit -> !unit.getOwner().equals(this))
                .findAny().isPresent();
    }

    public int getGold() {
        return gold;
    }

    public void setGold(final int newGoldValue) {
        Preconditions.checkArgument(newGoldValue >= 0,
                "New gold value '%s' is less than zero", newGoldValue);
        final int oldValue = gold;
        this.gold = newGoldValue;
        model.fireGoldWasChanged(this, oldValue, newGoldValue);
    }

    void checkTurnPreparationComplete() {
        model.checkTurnPreparationComplete();
    }

    public void buyGoods(final Goods goods) {
        buyGoods(goods, quoteEuropeTrade(goods, true));
    }

    /**
     * Buy at the terms the player confirmed. Gold and cargo are settled first;
     * the national market moves only after the complete price is charged.
     *
     * @param goods   required goods to buy
     * @param offered required purchase quote the player confirmed
     * @throws EuropePriceChangedException when the live price no longer matches
     */
    public void buyGoods(final Goods goods, final EuropeTradeQuote offered) {
        checkTurnPreparationComplete();
        final EuropeTradeQuote quote = quoteEuropeTrade(goods, true);
        Preconditions.checkState(canTradeInEurope(),
                "European trade is unavailable");
        checkNotBoycotted(goods.getType());
        checkConfirmedTerms(offered, quote);
        verifyAvailibilityOFGold(quote.getTotal());
        setGold(getGold() - quote.getTotal());
        model.getEurope().applyTrade(getEuropeanNation(), goods.getType(),
                -goods.getAmount());
    }

    public void sellGoods(final Goods goods) {
        sellGoods(goods, quoteEuropeTrade(goods, false));
    }

    /**
     * Sell at the terms the player confirmed. Gold is settled first; the
     * national market moves only after the complete price is credited.
     *
     * @param goods   required goods to sell
     * @param offered required sale quote the player confirmed
     * @throws EuropePriceChangedException when the live price no longer matches
     */
    public void sellGoods(final Goods goods, final EuropeTradeQuote offered) {
        checkTurnPreparationComplete();
        final EuropeTradeQuote quote = quoteEuropeTrade(goods, false);
        Preconditions.checkState(canTradeInEurope(),
                "European trade is unavailable");
        checkNotBoycotted(goods.getType());
        checkConfirmedTerms(offered, quote);
        setGold(Math.addExact(getGold(), quote.getTotal()));
        model.getEurope().applyTrade(getEuropeanNation(), goods.getType(),
                goods.getAmount());
    }

    private static void checkConfirmedTerms(final EuropeTradeQuote offered,
            final EuropeTradeQuote current) {
        Preconditions.checkNotNull(offered, "Offered quote is null");
        if (offered.isPurchase() != current.isPurchase()
                || !offered.getGoods().getType()
                        .equals(current.getGoods().getType())
                || offered.getUnitPrice() != current.getUnitPrice()
                || offered.getTaxPercentage() != current.getTaxPercentage()) {
            throw new EuropePriceChangedException(
                    "European price changed; nothing was traded");
        }
    }

    private void checkNotBoycotted(final GoodsType type) {
        Preconditions.checkState(!isBoycotted(type),
                "%s is boycotted until its arrears are cleared", type);
    }

    /** @return whether this player still has access to the home market */
    public boolean canTradeInEurope() {
        return !isDeclaredIndependence();
    }

    /**
     * @return the national European market this player trades in; a Crown reads
     *         the market of the nation it rules
     */
    public EuropeanNation getEuropeanNation() {
        final Player nation = isKing()
                ? ((PlayerKing) this).getWhosKingThisPlayerIs()
                : this;
        return EuropeanNation.fromPlayerName(nation.getName());
    }

    /**
     * Quote the same total used by cargo and pier-equipment transactions.
     *
     * @param goods    required goods, including zero for a slider preview
     * @param purchase true for buying, false for selling
     * @return an immutable snapshot of the current price and tax
     */
    public EuropeTradeQuote quoteEuropeTrade(final Goods goods,
            final boolean purchase) {
        Preconditions.checkNotNull(goods);
        final GoodsTrade trade = model.getEurope()
                .getGoodsTradeForType(goods.getType(), getEuropeanNation());
        return new EuropeTradeQuote(goods, purchase,
                purchase ? trade.getBuyPrice() : trade.getSellPrice(),
                model.getKingsTaxForPlayer(this));
    }

    /**
     * @param type required market commodity
     * @return an affordable cargo-slot quantity including purchase tax
     */
    public Goods getMaxBuyableGoods(final GoodsType type) {
        if (!canTradeInEurope() || isBoycotted(type)) {
            return Goods.of(type, 0);
        }
        for (int amount = CargoSlot.MAX_CARGO_SLOT_CAPACITY; amount > 0; amount--) {
            final Goods goods = Goods.of(type, amount);
            try {
                if (quoteEuropeTrade(goods, true).getTotal() <= gold) {
                    return goods;
                }
            } catch (ArithmeticException overflow) {
                // A price beyond integer treasury capacity is unaffordable.
            }
        }
        return Goods.of(type, 0);
    }

    /**
     * @param unitType the offered unit type
     * @return whether this live player can currently afford and acquire it
     */
    public boolean canBuyUnit(final UnitType unitType) {
        if (!isUnitPurchaseAvailable(unitType)) {
            return false;
        }
        try {
            final int price = getUnitPurchasePrice(unitType);
            checkNextUnitPurchasePrice(unitType);
            return gold >= price;
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    private static boolean hasEscalatingPrice(final UnitType type) {
        return type != null && UnitType.UNIT_TYPES.contains(type)
                && (type.isShip() || type == UnitType.ARTILLERY)
                && type.getEuropePrice() > 0;
    }

    private static int unitPurchasePrice(final UnitType type,
            final int previousPurchases) {
        final int base = type.getEuropePrice();
        final long increment = (base + 9L) / 10L;
        return Math.toIntExact(Math.addExact((long) base,
                Math.multiplyExact((long) previousPurchases, increment)));
    }

    private void loadUnitPurchaseCounts(
            final Map<String, Integer> savedCounts) {
        for (final Map.Entry<String, Integer> entry : savedCounts.entrySet()) {
            Preconditions.checkArgument(
                    entry.getKey() != null && entry.getValue() != null
                            && entry.getValue() >= 0,
                    "Invalid unit purchase count");
            final UnitType type = UnitType.valueOf(entry.getKey());
            Preconditions.checkArgument(hasEscalatingPrice(type),
                    "Only purchasable ships track demand");
            try {
                unitPurchasePrice(type, entry.getValue());
            } catch (final ArithmeticException overflow) {
                throw new IllegalArgumentException(
                        "Unit purchase count exceeds supported price",
                        overflow);
            }
            unitPurchaseCounts.put(type, entry.getValue());
        }
    }

    /** Current untaxed European quote for a registered purchasable unit. */
    public int getUnitPurchasePrice(final UnitType unitType) {
        Preconditions.checkNotNull(unitType, "Unit type is null");
        Preconditions.checkArgument(
                UnitType.UNIT_TYPES.contains(unitType)
                        && unitType.getEuropePrice() > 0,
                "This unit type cannot be bought in Europe");
        return hasEscalatingPrice(unitType)
                ? unitPurchasePrice(unitType,
                        unitPurchaseCounts.getOrDefault(unitType, 0))
                : unitType.getEuropePrice();
    }

    private void checkNextUnitPurchasePrice(final UnitType unitType) {
        if (hasEscalatingPrice(unitType)) {
            final int nextCount = Math
                    .addExact(unitPurchaseCounts.getOrDefault(unitType, 0), 1);
            unitPurchasePrice(unitType, nextCount);
        }
    }

    /**
     * Current immutable three-candidate snapshots; reopening does not redraw.
     */
    public List<RecruitmentOffer> getRecruitmentOffers() {
        Preconditions.checkState(recruitment != null,
                "Kings cannot recruit in Europe");
        Preconditions.checkState(
                model.isGameRunning() && model.getCurrentPlayer() == this
                        && !isKing() && canTradeInEurope()
                        && model.getPlayerStore().getPlayers().stream()
                                .anyMatch(player -> player == this),
                "Recruitment is unavailable for this player");
        Preconditions.checkState(
                !recruitment.hasReachedTarget(model.getDifficulty(), name),
                "Earned immigrant is pending");
        return recruitment.offers(model.getDifficulty(), name);
    }

    public boolean canRecruit(final RecruitmentOffer offer) {
        return isRecruitmentOfferCurrent(offer) && offer.getPrice() <= gold;
    }

    private boolean isRecruitmentOfferCurrent(final RecruitmentOffer offer) {
        if (offer == null || recruitment == null || !model.isGameRunning()
                || model.getCurrentPlayer() != this || isKing()
                || !canTradeInEurope() || model.getPlayerStore().getPlayers()
                        .stream().noneMatch(player -> player == this)) {
            return false;
        }
        try {
            return !recruitment.hasReachedTarget(model.getDifficulty(), name)
                    && recruitment.isCurrent(offer, model.getDifficulty(), name)
                    && recruitment.canAdvance(model.getDifficulty(), name)
                    && model.canAddUnitInEurope();
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    public Unit recruit(final RecruitmentOffer offer) {
        Preconditions.checkNotNull(offer, "Recruitment offer is null");
        checkTurnPreparationComplete();
        Preconditions.checkState(isRecruitmentOfferCurrent(offer),
                "Recruitment offer is unavailable or stale");
        verifyAvailibilityOFGold(offer.getPrice());
        final Unit recruited = model.addUnitInEurope(offer.getType(), this);
        recruitment.replace(offer.getSlot(), isBrewster());
        setGold(gold - offer.getPrice());
        return recruited;
    }

    private boolean isUnitPurchaseAvailable(final UnitType unitType) {
        return unitType != null && UnitType.UNIT_TYPES.contains(unitType)
                && unitType.getEuropePrice() > 0 && model.isGameRunning()
                && model.getPlayerStore().getPlayers().stream()
                        .anyMatch(player -> player == this)
                && model.getCurrentPlayer() == this && !isKing()
                && canTradeInEurope() && model.canAddUnitInEurope();
    }

    public void buy(final UnitType unitType) {
        buy(unitType, getUnitPurchasePrice(unitType));
    }

    public void buy(final UnitType unitType, final int expectedPrice) {
        Preconditions.checkNotNull(unitType);
        Preconditions.checkArgument(
                UnitType.UNIT_TYPES.contains(unitType)
                        && unitType.getEuropePrice() > 0,
                "This unit type cannot be bought in Europe");
        Preconditions.checkState(isUnitPurchaseAvailable(unitType),
                "Unit purchase is unavailable for this player");
        final int price;
        try {
            price = getUnitPurchasePrice(unitType);
            Preconditions.checkState(expectedPrice == price,
                    "Unit purchase quote has changed");
            checkNextUnitPurchasePrice(unitType);
        } catch (final ArithmeticException overflow) {
            throw new IllegalStateException(
                    "Next unit purchase quote exceeds supported range",
                    overflow);
        }
        verifyAvailibilityOFGold(price);
        model.addUnitInEurope(unitType, this);
        if (hasEscalatingPrice(unitType)) {
            unitPurchaseCounts.put(unitType,
                    unitPurchaseCounts.getOrDefault(unitType, 0) + 1);
        }
        setGold(getGold() - price);
    }

    private void verifyAvailibilityOFGold(final int price) {
        if (getGold() < price) {
            throw new NotEnoughtGoldException(String.format(
                    "You can't buy this item. You need %s and you have %s",
                    price, getGold()));
        }
    }

    /**
     * @return the declaredIndependence
     */
    public boolean isDeclaredIndependence() {
        return declaredIndependence;
    }

    /**
     * Declare independence once and muster the eligible veterans of every
     * supporting colony. The live rules are checked before the first change,
     * again after the veto callbacks, and the whole result is in place before
     * the after-declaration listeners run. A veto leaves everything unchanged.
     */
    public void declareIndependence() {
        checkTurnPreparationComplete();
        model.checkGameRunning();
        model.checkCurrentPlayer(this);
        Preconditions.checkState(!declaringIndependence,
                "Independence is already being declared");
        checkIndependenceEligible();
        declaringIndependence = true;
        try {
            if (!model.fireBeforeDeclaringIndependence(this)) {
                return;
            }
            // A callback may have changed the world, paused the turn
            // preparation or declared reentrantly.
            checkTurnPreparationComplete();
            model.checkGameRunning();
            model.checkCurrentPlayer(this);
            checkIndependenceEligible();
            final List<UnitFreeColonist> muster = planIndependenceMuster();
            declaredIndependence = true;
            muster.forEach(
                    unit -> unit.assignMilitaryRole(MilitaryRole.CONTINENTAL));
        } finally {
            declaringIndependence = false;
        }
        model.fireIndependenceWasDeclared(this);
    }

    private void checkIndependenceEligible() {
        final Optional<IndependenceProblem> problem = getIndependenceProblem();
        Preconditions.checkState(problem.isEmpty(),
                "It's not possible to declare independence: %s",
                problem.orElse(null));
    }

    /**
     * Live independence rule: a registered colonial nation with exact national
     * support of at least 50 percent, before spring 1800, with at least one
     * owned coastal colony.
     *
     * @return the first failing rule, or empty when declaring is allowed
     */
    public Optional<IndependenceProblem> getIndependenceProblem() {
        if (isKing() || model.getPlayers().stream().noneMatch(p -> p == this)) {
            return Optional.of(IndependenceProblem.UNAVAILABLE);
        }
        if (isDeclaredIndependence()) {
            return Optional.of(IndependenceProblem.ALREADY_DECLARED);
        }
        if (model.getCalendar()
                .getCurrentYear() >= INDEPENDENCE_DEADLINE_YEAR) {
            return Optional.of(IndependenceProblem.TOO_LATE);
        }
        if (model.getColonies(this).stream().noneMatch(colony -> model
                .isOwnedCoastalPort(this, colony.getLocation()))) {
            return Optional.of(IndependenceProblem.NO_COASTAL_COLONY);
        }
        if (!getNationalRebelSupport()
                .isAtLeastPercent(REQUIRED_SUPPORT_PERCENT)) {
            return Optional.of(IndependenceProblem.INSUFFICIENT_SUPPORT);
        }
        return Optional.empty();
    }

    public boolean isPossibleToDecalareIndependence() {
        return getIndependenceProblem().isEmpty();
    }

    /**
     * Read-only forecast of the veterans a declaration would muster now.
     *
     * @return the units in the order they would be converted, empty when the
     *         declaration is not allowed
     */
    public List<Unit> getIndependenceMusterPreview() {
        if (getIndependenceProblem().isPresent()) {
            return List.of();
        }
        return List.copyOf(planIndependenceMuster());
    }

    /**
     * Per owned colony with exact local support of at least 50 percent, the
     * first {@code floor(V * local support)} armed veterans standing on the
     * colony tile by ascending unit ID. Internal workers, cargo and other
     * owners' units are not counted.
     */
    private List<UnitFreeColonist> planIndependenceMuster() {
        final List<UnitFreeColonist> out = new ArrayList<>();
        for (final Colony colony : model.getColonies(this)) {
            final RebelSupport local = colony.getRebelSupport();
            if (!local.isAtLeastPercent(REQUIRED_SUPPORT_PERCENT)) {
                continue;
            }
            final List<UnitFreeColonist> veterans = model
                    .getUnitsAt(colony.getLocation()).stream()
                    .filter(unit -> unit.getOwner() == this
                            && unit instanceof UnitFreeColonist)
                    .map(UnitFreeColonist.class::cast)
                    .filter(unit -> unit.isHoldingGuns() && unit.isVeteran()
                            && unit.getMilitaryRole() == MilitaryRole.COLONIAL)
                    .sorted(Comparator.comparingInt(Unit::getId))
                    .collect(Collectors.toList());
            out.addAll(veterans.subList(0,
                    (int) local.floorShare(veterans.size())));
        }
        return out;
    }

    /**
     * @return the extraData
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public PlayerStatistics getPlayerStatistics() {
        final PlayerStatistics out = new PlayerStatistics(getGold());

        model.getColonies(this)
                .forEach(out.getGoodsStatistics()::addColonyData);
        model.getUnitsOwnedBy(this, true)
                .forEach(out.getGoodsStatistics()::addUnitData);
        model.getUnitsOwnedBy(this, true)
                .forEach(out.getMilitaryStrength()::addUnitData);

        return out;
    }
}
