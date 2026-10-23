package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.RivalExpansionObjectivePo;
import org.microcol.model.store.RivalTradeObjectivePo;
import org.microcol.model.store.ColonySightingPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.InterventionPo;
import org.microcol.model.store.MercenaryOfferPo;
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
     * Foreign-intervention target of a declared colonial nation (R-0189). Null
     * until the first owner update after declaration starts it.
     */
    private InterventionTarget intervention;

    /** Own updates counted before declaration for mercenary offers (R-0169). */
    private int mercenaryTurns;

    /**
     * Rebel updates counted after declaration for mercenary offers (R-0169).
     */
    private int mercenaryRebelUpdates;

    /** Offer waiting for the owner's answer; it lasts through this turn. */
    private MercenaryOffer mercenaryOffer;

    /** Paid mercenary groups of this owner, pending or delivered (R-0169). */
    private final List<MercenaryDelivery> mercenaryDeliveries = new ArrayList<>();

    /**
     * Colonies of a declared rebellion (R-0192): the declaration snapshot plus
     * later foundings and captures, minus destroyed colonies. A capture by the
     * Crown keeps the member. Empty for a nation that has not declared.
     */
    private final Set<Integer> rebellionColonyIds = new TreeSet<>();

    /**
     * Reason code under which this declared rebellion was lost (R-0192), kept
     * per rebel so a computer rebel's loss is durable without ending the human
     * game. Null while the rebellion holds.
     */
    private String rebellionLossReason;

    /**
     * Completion order of this declared rebellion's victory (R-0191), counted
     * from the first rebel to win. Null while the rebellion is not won.
     */
    private Integer rebellionVictoryOrder;

    /** Null unless a computer colony is running a supply cycle. */
    private RivalTradeObjective rivalTradeObjective;

    /** Null unless a computer colony is expanding to a second colony. */
    private RivalExpansionObjective rivalExpansionObjective;

    /** Null before a computer colony completes a supply round trip. */
    private Integer rivalLastServedColonyId;

    /** Carriers waiting in their colony ports between supply round trips. */
    private final Set<Integer> rivalParkedCarrierIds = new TreeSet<>();

    /**
     * Colonies remembered from actual observation, by location. Concurrent
     * because the interface paints while the model thread observes.
     */
    private final Map<Location, ColonySighting> colonySightings = new java.util.concurrent.ConcurrentHashMap<>();

    /** Colonial powers actually seen at some point; only ever grows. */
    private final java.util.List<String> knownPowers = new java.util.concurrent.CopyOnWriteArrayList<>();

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
        Preconditions.checkArgument(playerPo.getRivalParkedCarrierIds() != null,
                "Rival parked carriers are missing");
        if (playerPo.getNativeNation() != null) {
            Preconditions.checkArgument(playerPo.isComputer(),
                    "Native players are computer players");
            Preconditions.checkArgument(
                    playerPo.getWhosKingThisPlayerIs() == null
                            && playerPo.getRecruitment() == null
                            && playerPo.getCongress() == null
                            && playerPo.getTax() == null
                            && playerPo.getUnitPurchaseCounts().isEmpty()
                            && playerPo.getRivalTradeObjective() == null
                            && playerPo.getRivalExpansionObjective() == null
                            && playerPo.getRivalLastServedColonyId() == null
                            && playerPo.getRivalParkedCarrierIds().isEmpty()
                            && playerPo.getRoyalRoster() == null
                            && playerPo.getIntervention() == null
                            && isNoRebellion(playerPo),
                    "Native players have no European state");
            return new PlayerNative(playerPo.getName(),
                    playerPo.getNativeNation(), playerPo.getGold(), model,
                    playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet());
        }
        if (playerPo.getWhosKingThisPlayerIs() == null) {
            Preconditions.checkArgument(playerPo.getRecruitment() != null,
                    "Missing colonial recruitment pool");
            Preconditions.checkArgument(playerPo.getCongress() != null,
                    "Missing colonial Congress");
            Preconditions.checkArgument(playerPo.getTax() != null,
                    "Missing colonial tax state");
            Preconditions.checkArgument(playerPo.getRoyalRoster() == null,
                    "Colonial nations have no Crown roster");
            final Player player = new PlayerHuman(playerPo.getName(),
                    playerPo.isComputer(), playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(),
                    playerPo.getRecruitment(), playerPo.getCongress(),
                    playerPo.getTax());
            player.loadUnitPurchaseCounts(playerPo.getUnitPurchaseCounts());
            Preconditions.checkArgument(
                    playerPo.getRivalTradeObjective() == null
                            || playerPo.isComputer(),
                    "Only computer colonies keep a trade objective");
            Preconditions.checkArgument(
                    playerPo.getRivalExpansionObjective() == null
                            || playerPo.isComputer(),
                    "Only computer colonies keep an expansion objective");
            player.loadRivalTradeObjective(playerPo.getRivalTradeObjective());
            player.loadRivalExpansionObjective(
                    playerPo.getRivalExpansionObjective());
            Preconditions.checkArgument(
                    (playerPo.getRivalLastServedColonyId() == null
                            && playerPo.getRivalParkedCarrierIds().isEmpty())
                            || playerPo.isComputer(),
                    "Only computer colonies keep a supply rotation");
            player.loadRivalRotation(playerPo.getRivalLastServedColonyId(),
                    playerPo.getRivalParkedCarrierIds());
            player.loadIntervention(playerPo.getIntervention());
            player.loadMercenaries(playerPo);
            player.loadRebellionColonyIds(playerPo.getRebellionColonyIds());
            player.loadRebellionLossReason(playerPo.getRebellionLossReason());
            player.loadRebellionVictoryOrder(
                    playerPo.getRebellionVictoryOrder());
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
            Preconditions.checkArgument(playerPo.getRoyalRoster() != null,
                    "Missing Crown roster");
            Preconditions.checkArgument(
                    playerPo.getRivalTradeObjective() == null
                            && playerPo.getRivalExpansionObjective() == null
                            && playerPo.getRivalLastServedColonyId() == null
                            && playerPo.getRivalParkedCarrierIds().isEmpty(),
                    "Kings cannot have trade objectives");
            Preconditions.checkArgument(playerPo.getIntervention() == null,
                    "Kings cannot have an intervention target");
            Preconditions.checkArgument(isNoRebellion(playerPo),
                    "Kings cannot have rebellion colonies");
            final Player subdued = playerStore
                    .getPlayerByName(playerPo.getWhosKingThisPlayerIs());
            return new PlayerKing(playerPo.getName(), playerPo.isComputer(),
                    playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), subdued,
                    playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(), new RoyalRoster(
                            playerPo.getRoyalRoster(), model.getDifficulty()));
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
                visibility.reveal(colonyArea(colony));
            } else if (isCurrentlyVisible(colony.getLocation())) {
                colonySightings.put(colony.getLocation(), sightingOf(colony));
            }
        }
        colonySightings.keySet()
                .removeIf(location -> isCurrentlyVisible(location)
                        && model.getColonyAt(location).isEmpty());
    }

    /**
     * Remember every other colonial power with a unit or colony inside present
     * sight right now (R-0168). Ships and colonists count alike; units that are
     * not seen, such as ships on the high seas, reveal nothing. Contact is only
     * ever added, so it outlives the sighting, and reading the report never
     * calls this.
     */
    void observeContacts() {
        if (!isColonialNation()) {
            return;
        }
        for (final Unit unit : model.getAllUnits()) {
            final Player other = unit.getOwner();
            if (other != this && other.isColonialNation()
                    && !knownPowers.contains(other.getName())
                    && unit.isAtPlaceLocation()
                    && isCurrentlyVisible(unit.getLocation())) {
                knownPowers.add(other.getName());
            }
        }
        for (final Colony colony : model.getColonies()) {
            final Player other = colony.getOwner();
            if (other != this && other.isColonialNation()
                    && !knownPowers.contains(other.getName())
                    && isCurrentlyVisible(colony.getLocation())) {
                knownPowers.add(other.getName());
            }
        }
    }

    /**
     * @param power colonial power to ask about
     * @return whether this player has earned contact with it: it was seen in
     *         present sight at some point, or one of its colonies is known
     */
    public boolean hasContactWith(final Player power) {
        Preconditions.checkNotNull(power);
        return knownPowers.contains(power.getName()) || getKnownColonies()
                .stream().anyMatch(sighting -> sighting.getOwner() == power);
    }

    /**
     * Restores saved contacts verbatim. Every name must be a registered
     * colonial nation other than this player, listed once; nothing is reset or
     * guessed.
     */
    void restoreContacts(final List<String> saved) {
        Preconditions.checkArgument(saved != null, "Missing known powers");
        final Set<String> restored = new java.util.HashSet<>();
        for (final String powerName : saved) {
            Preconditions.checkArgument(
                    powerName != null && !powerName.equals(name)
                            && restored.add(powerName)
                            && model.getPlayers().stream().anyMatch(
                                    player -> player.getName().equals(powerName)
                                            && player.isColonialNation()),
                    "Invalid known power");
        }
        knownPowers.clear();
        knownPowers.addAll(restored);
    }

    /** Remember a colony the player was directly told about. */
    void recordColony(final Colony colony) {
        colonySightings.put(colony.getLocation(), sightingOf(colony));
    }

    /**
     * Explore the land within two squares of a native settlement, the chief's
     * information (R-0158). Only the accumulated exploration grows: present
     * sight, foreign units and colonies are not granted, and nothing beyond the
     * two squares is marked.
     *
     * @param center required native settlement square
     */
    void revealLandAround(final Location center) {
        Preconditions.checkNotNull(center);
        final List<Location> area = new java.util.ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                final Location at = Location.of(center.getX() + dx,
                        center.getY() + dy);
                if (model.isValid(at)) {
                    area.add(at);
                }
            }
        }
        visibility.reveal(area);
    }

    /** Colony tile and its eight neighbors, clipped to the map. */
    private List<Location> colonyArea(final Colony colony) {
        final List<Location> around = new java.util.ArrayList<>(
                colony.getLocation().getNeighbors());
        around.add(colony.getLocation());
        return around.stream().filter(model::isValid)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Remember a colony as it is now without having it in sight, for a Crown
     * that ruled it until the nation declared independence.
     */
    void rememberColony(final Colony colony) {
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
        // The move may bring a rival into this player's sight, or this player
        // into a rival's, so every player observes contact.
        model.refreshContacts();
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
        if (rivalTradeObjective != null) {
            out.setRivalTradeObjective(rivalTradeObjective.save());
        }
        if (rivalExpansionObjective != null) {
            out.setRivalExpansionObjective(rivalExpansionObjective.save());
        }
        if (intervention != null) {
            out.setIntervention(intervention.save());
        }
        if (isColonialNation()) {
            out.setMercenaryTurns(mercenaryTurns);
            out.setMercenaryRebelUpdates(mercenaryRebelUpdates);
        }
        if (mercenaryOffer != null) {
            final MercenaryOfferPo offerPo = new MercenaryOfferPo();
            offerPo.setIssuer(mercenaryOffer.getIssuer().getName());
            offerPo.setPrice(mercenaryOffer.getPrice());
            out.setMercenaryOffer(offerPo);
        }
        mercenaryDeliveries.forEach(
                group -> out.getMercenaryDeliveries().add(group.save()));
        out.setRebellionColonyIds(new ArrayList<>(rebellionColonyIds));
        out.setRebellionLossReason(rebellionLossReason);
        out.setRebellionVictoryOrder(rebellionVictoryOrder);
        out.setRivalLastServedColonyId(rivalLastServedColonyId);
        out.setRivalParkedCarrierIds(new ArrayList<>(rivalParkedCarrierIds));
        knownPowers.stream().sorted().forEach(out.getKnownPowers()::add);
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

    /**
     * @return whether this is a native nation's participant rather than a
     *         European colonial nation or Crown
     */
    public boolean isNative() {
        return false;
    }

    /**
     * @return whether this is a European colonial nation, the only kind of
     *         participant in the European treaty table, Europe trade, Congress
     *         and tax; neither a Crown nor a native nation
     */
    public boolean isColonialNation() {
        return !isKing() && !isNative();
    }

    public List<Unit> getUnits() {
        return model.getUnitsOwnedBy(this, false);
    }

    public List<Unit> getAllUnits() {
        return model.getUnitsOwnedBy(this, true);
    }

    public int getNumberOfMilitaryUnits() {
        // Crown-seized units are not active resources of the nation (R-0187).
        return (int) getAllUnits().stream()
                .filter(unit -> !unit.isCrownSeized())
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
        // Once per owner turn, before any sale of this update. A Crown has no
        // turn of its own for its ruled nation's market.
        if (!isKing()) {
            EuropeanNation.findByPlayerName(getName()).ifPresent(
                    nation -> model.getEurope().advanceSaleWindow(nation));
        }
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
        // Before colony production, so the bells of this update count.
        startIntervention();
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
        // After colony production, so an earning of this update can deliver. A
        // rebellion lost by now sends nothing; its end is published only once
        // the whole update is complete (R-0192).
        deliverIntervention();
        // Paid groups retry first, so an offer never waits behind a placed one.
        deliverMercenaryGroups();
        advanceMercenaryOffers();
        if (!isKing()) {
            // After colony production, so this update's custom-house sales
            // count as trades affecting the national markets. A scripted
            // actor that is not a real colonial nation has no market.
            EuropeanNation.findByPlayerName(getName()).ifPresent(
                    nation -> model.getEurope().recoverMarkets(nation));
        }
        // Before congress: an arrival that clears native alarm must read as a
        // fall.
        model.observeNativeRelations(this);
        settleCongress();
        model.collectRoyalTreasure(this);
        getAllUnits().forEach(unit -> unit.startTurn());
        // Before missions, so a convert delivered by this update starts at zero
        // unassigned updates.
        model.advanceNativeConverts(this);
        // Before relations: the mission clock reads the attitude as it entered
        // this update. Each fifth nonhostile update is one conversion event.
        model.resolveNativeConversions(this, model.advanceNativeMissions(this));
        // Once per completed own update; load and Continue never reach here.
        model.updateNativeRelations(this);
        // After relations, so a tense settlement cancels on its current
        // attitude.
        model.advanceNativeTeaching(this);
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

    /**
     * @return the prewar roster of the Crown ruling this colonial nation; empty
     *         for a Crown or a player without a Crown
     */
    public Optional<RoyalRoster> getCrownRoster() {
        return model.getKingOf(this).map(PlayerKing::getRoyalRoster);
    }

    /**
     * Add a colony that this nation has founded or captured since its
     * declaration (R-0192). A nation that has not declared, and a Crown, keep
     * no membership.
     *
     * @param colony required colony that this nation now owns
     */
    void recordRebellionColony(final Colony colony) {
        Preconditions.checkNotNull(colony);
        if (declaredIndependence && !isKing()) {
            rebellionColonyIds.add(colony.getId());
        }
    }

    /**
     * Remove a destroyed colony from this rebellion (R-0192). Captures never
     * remove membership, so only destruction calls this.
     *
     * @param colonyId identity of the destroyed colony
     */
    void forgetRebellionColony(final int colonyId) {
        rebellionColonyIds.remove(colonyId);
    }

    /**
     * @return the colony IDs of this declared rebellion, stable and sorted;
     *         empty for a nation that has not declared
     */
    public Set<Integer> getRebellionColonyIds() {
        return Collections.unmodifiableSet(rebellionColonyIds);
    }

    /**
     * @return the foreign-intervention target of this declared nation; empty
     *         before declaration and before its first owner update after it
     */
    public Optional<InterventionTarget> getIntervention() {
        return Optional.ofNullable(intervention);
    }

    /**
     * Load a saved target. Only a declared colonial nation can have one; a
     * declared nation without one is still waiting for its first update.
     */
    private void loadIntervention(final InterventionPo po) {
        if (po == null) {
            return;
        }
        Preconditions.checkArgument(declaredIndependence,
                "Only a declared nation has an intervention target");
        intervention = new InterventionTarget(po);
    }

    private static boolean isNoRebellion(final PlayerPo playerPo) {
        return (playerPo.getRebellionColonyIds() == null
                || playerPo.getRebellionColonyIds().isEmpty())
                && playerPo.getRebellionLossReason() == null
                && playerPo.getRebellionVictoryOrder() == null;
    }

    /**
     * Load the saved loss of this rebellion (R-0192). Only a declared colonial
     * nation can have lost, and only under one of the four loss reasons.
     */
    private void loadRebellionLossReason(final String saved) {
        if (saved == null) {
            return;
        }
        Preconditions.checkArgument(declaredIndependence,
                "Only a declared nation can have lost its rebellion");
        Preconditions.checkArgument(GameOverEvaluator.isRebellionLoss(saved),
                "Unknown rebellion loss reason '%s'", saved);
        rebellionLossReason = saved;
    }

    /**
     * @return the reason code under which this rebellion was lost, or empty
     *         while it holds; R-0191 victory never applies once this is present
     */
    public Optional<String> getRebellionLossReason() {
        return Optional.ofNullable(rebellionLossReason);
    }

    /**
     * Whether this declared rebellion has ended: recorded as lost or won, or
     * lost by the live state although the end is not yet published because an
     * update is still in progress (R-0192). Read-only; it records and announces
     * nothing. Every help of the nation stops at this one gate (R-0191).
     */
    boolean isRebellionEnded() {
        return isRebellionDecided() || declaredIndependence && !isKing()
                && RebellionLoss.evaluate(model, this).isPresent();
    }

    /**
     * Record the loss of this rebellion once (R-0192). The record is durable
     * and stops every later intervention; it does not itself end any game.
     *
     * @param reason required rebellion loss reason code
     */
    void markRebellionLost(final String reason) {
        Preconditions.checkState(declaredIndependence && !isKing(),
                "Only a declared nation can lose its rebellion");
        Preconditions.checkState(rebellionLossReason == null,
                "The rebellion was already lost");
        Preconditions.checkArgument(GameOverEvaluator.isRebellionLoss(reason),
                "Unknown rebellion loss reason '%s'", reason);
        rebellionLossReason = reason;
    }

    /**
     * @return the completion order of this rebellion's victory, one for the
     *         first rebel to win, or empty while it is not won
     */
    public Optional<Integer> getRebellionVictoryOrder() {
        return Optional.ofNullable(rebellionVictoryOrder);
    }

    /**
     * Load the saved victory of this rebellion (R-0191). Only a declared
     * colonial nation can have won, and never after a loss.
     */
    private void loadRebellionVictoryOrder(final Integer saved) {
        if (saved == null) {
            return;
        }
        Preconditions.checkArgument(declaredIndependence,
                "Only a declared nation can have won its rebellion");
        Preconditions.checkArgument(saved >= 1,
                "Invalid rebellion victory order");
        Preconditions.checkArgument(rebellionLossReason == null,
                "A lost rebellion cannot also be won");
        rebellionVictoryOrder = saved;
    }

    /**
     * @return whether this declared rebellion already has its end recorded,
     *         either as a loss or as a victory, so it is never judged again
     */
    boolean isRebellionDecided() {
        return rebellionLossReason != null || rebellionVictoryOrder != null;
    }

    /**
     * Record the victory of this rebellion once (R-0191). The record is durable
     * and never changes; it does not itself end any game.
     *
     * @param order required completion order, starting at one
     */
    void markRebellionWon(final int order) {
        Preconditions.checkState(declaredIndependence && !isKing(),
                "Only a declared nation can win its rebellion");
        Preconditions.checkState(!isRebellionDecided(),
                "The rebellion was already decided");
        Preconditions.checkArgument(order >= 1,
                "Invalid rebellion victory order");
        rebellionVictoryOrder = order;
    }

    /**
     * Load the saved rebellion membership (R-0192). A declared colonial nation
     * must name its members, because they cannot be rebuilt from today's
     * ownership; a player that never declared keeps none.
     */
    private void loadRebellionColonyIds(final List<Integer> saved) {
        if (saved == null) {
            Preconditions.checkArgument(!declaredIndependence,
                    "Declared nation '%s' has no rebellion colonies", name);
            return;
        }
        Preconditions.checkArgument(declaredIndependence || saved.isEmpty(),
                "Only a declared nation keeps rebellion colonies");
        Preconditions.checkArgument(
                saved.stream().allMatch(id -> id != null && id >= 0)
                        && new HashSet<>(saved).size() == saved.size(),
                "Invalid rebellion colony identity");
        rebellionColonyIds.addAll(saved);
    }

    /**
     * Fix the intervention target at the first owner update after declaration
     * and announce it once. A preparation that finds the target set changes
     * nothing, so an interrupted turn cannot start it twice.
     */
    private void startIntervention() {
        if (!declaredIndependence || isKing() || intervention != null
                || isRebellionEnded()) {
            return;
        }
        intervention = InterventionTarget.start(model.getDifficulty());
        model.addTurnEvent(TurnEventProvider.getIntervention(this,
                intervention.getTarget(), false));
    }

    /**
     * Deliver the earned intervention once (R-0190). Every owner update retries
     * a pending delivery; the model commits the units, the delivered flag and
     * the arrival notice together, or leaves all of them unchanged. Loading
     * never reaches here.
     */
    private void deliverIntervention() {
        if (intervention == null || !intervention.isEarned()
                || intervention.isDelivered() || isRebellionEnded()
                || model.isGameFinished()) {
            return;
        }
        model.deliverInterventionForces(this);
    }

    /**
     * Retry every paid group that is still pending (R-0169). Each attempt
     * either places the whole group or changes nothing; a rebellion that has
     * ended sends no more help, and a finished game places nothing.
     */
    private void deliverMercenaryGroups() {
        if (isRebellionEnded() || model.isGameFinished()) {
            return;
        }
        mercenaryDeliveries.stream().filter(group -> !group.isDelivered())
                .forEach(group -> model.deliverMercenaryGroup(this, group));
    }

    /**
     * Count one owner update and open an offer on every tenth one (R-0169).
     * Every colonial nation shares this state; only the interface raises it for
     * a human, and an offer a computer nation does not answer expires with its
     * turn. Before declaration the update needs the treasury threshold and one
     * chance draw; after it no draw is made. An interval without an eligible
     * issuer opens nothing.
     */
    private void advanceMercenaryOffers() {
        if (!isColonialNation() || isRebellionEnded()) {
            return;
        }
        if (declaredIndependence) {
            mercenaryRebelUpdates++;
            if (!MercenaryRules.isIntervalUpdate(mercenaryRebelUpdates)) {
                return;
            }
            mercenaryOffer = model.quoteMercenaryOffer(this).orElse(null);
            return;
        }
        mercenaryTurns++;
        if (!MercenaryRules.isIntervalUpdate(mercenaryTurns)
                || gold < MercenaryRules.TREASURY_THRESHOLD) {
            return;
        }
        // The one saved draw is spent only when an offer could be made, so a
        // skipped interval never moves the shared random state.
        final Optional<MercenaryOffer> quote = model.quoteMercenaryOffer(this);
        if (quote.isPresent()
                && model.nextCombatRandom() < MercenaryRules.PEACETIME_CHANCE) {
            mercenaryOffer = quote.get();
        }
    }

    /** @return the offer waiting for this owner's answer, if any (R-0169) */
    public Optional<MercenaryOffer> getMercenaryOffer() {
        return Optional.ofNullable(mercenaryOffer);
    }

    /** Forget the offer after an answer or at turn end; never charges gold. */
    void clearMercenaryOffer() {
        mercenaryOffer = null;
    }

    /** @return paid mercenary groups of this owner, pending and delivered */
    public List<MercenaryDelivery> getMercenaryDeliveries() {
        return Collections.unmodifiableList(mercenaryDeliveries);
    }

    void addMercenaryDelivery(final MercenaryDelivery delivery) {
        mercenaryDeliveries.add(Preconditions.checkNotNull(delivery));
    }

    /**
     * Restore the pending offer of a colonial nation (R-0169). The issuer must
     * be a registered colonial nation other than this owner; the price must
     * match the saved difficulty, so a loaded offer cannot be cheaper than a
     * new one.
     *
     * @param saved saved offer, or null when none waits
     */
    void restoreMercenaryOffer(final MercenaryOfferPo saved) {
        if (saved == null) {
            return;
        }
        Preconditions.checkArgument(isColonialNation(),
                "Only a colonial nation keeps a mercenary offer");
        Preconditions.checkArgument(
                saved.getIssuer() != null && saved.getPrice() != null,
                "Incomplete mercenary offer");
        final Player issuer = model.getPlayers().stream()
                .filter(player -> player.getName().equals(saved.getIssuer()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Unknown mercenary issuer"));
        Preconditions.checkArgument(
                issuer != this && issuer.isColonialNation()
                        && saved.getPrice() == MercenaryRules
                                .price(model.getDifficulty()),
                "Invalid mercenary offer");
        mercenaryOffer = new MercenaryOffer(this, issuer, saved.getPrice());
    }

    /**
     * Load the saved mercenary counters and paid groups of a colonial nation
     * (R-0169). An absent counter is zero; present values are validated.
     */
    private void loadMercenaries(final PlayerPo playerPo) {
        final Integer turns = playerPo.getMercenaryTurns();
        final Integer rebelUpdates = playerPo.getMercenaryRebelUpdates();
        // The own-turn count is historical: it stays after declaration too.
        Preconditions.checkArgument(turns == null || turns >= 0,
                "Invalid mercenary interval count");
        // A rebel count starts at declaration; zero before it is the ordinary
        // saved state of a colony that has not yet declared.
        Preconditions.checkArgument(
                rebelUpdates == null || rebelUpdates >= 0
                        && (declaredIndependence || rebelUpdates == 0),
                "Invalid rebel mercenary interval count");
        mercenaryTurns = turns == null ? 0 : turns;
        mercenaryRebelUpdates = rebelUpdates == null ? 0 : rebelUpdates;
        playerPo.getMercenaryDeliveries().forEach(
                po -> mercenaryDeliveries.add(new MercenaryDelivery(po)));
    }

    void checkCanCreditProducedBells(final int produced) {
        if (congress != null) {
            congress.checkCredit(produced);
        }
        if (intervention != null) {
            intervention.checkCredit(produced);
        }
        if (!declaredIndependence) {
            getCrownRoster().ifPresent(roster -> roster.checkCredit(produced));
        }
    }

    void creditProducedBells(final int produced) {
        if (congress != null) {
            congress.credit(produced);
        }
        if (intervention != null && !isRebellionEnded()
                && intervention.credit(produced)) {
            model.addTurnEvent(TurnEventProvider.getIntervention(this,
                    intervention.getTarget(), true));
        }
        // Bell growth stops for good at the declaration.
        if (!declaredIndependence) {
            getCrownRoster().ifPresent(roster -> roster.credit(produced));
        }
    }

    /**
     * Coronado's one-time survey at his arrival. Every European colony existing
     * now is explored with its neighbors, and foreign ones are remembered. No
     * current sight is granted and later colonies are not included.
     */
    private void surveyEuropeanColonies() {
        for (final Colony colony : model.getColonies()) {
            visibility.reveal(colonyArea(colony));
            if (!colony.getOwner().equals(this)) {
                recordColony(colony);
            }
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
            if (arrived == Founder.FRANCISCO_DE_CORONADO) {
                surveyEuropeanColonies();
            }
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
            if (arrived == Founder.POCAHONTAS) {
                model.clearNativeRelationsAfterPocahontas(this);
            }
            if (arrived == Founder.HERNANDO_DE_SOTO) {
                getAllUnits().stream().filter(Unit::isAtPlaceLocation)
                        .forEach(this::revealMapForUnit);
            }
            model.getTurnEventStore()
                    .add(TurnEventProvider.getFounderArrived(this, arrived));
            if (arrived == Founder.BARTOLOME_DE_LAS_CASAS) {
                // After the arrival notice, so the freed converts read as its
                // effect (R-0182).
                model.freeNativeConverts(this);
            }
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
        addImmigrantInEurope(type);
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
        // Counted before gold moves, so an overflowing account refuses the
        // trade.
        final EuropeTradeAccount account = model.getEurope()
                .getTradeAccount(getEuropeanNation(), goods.getType())
                .purchased(goods.getAmount(), quote.getTotal());
        setGold(getGold() - quote.getTotal());
        model.getEurope().applyTrade(getEuropeanNation(), goods.getType(),
                -goods.getAmount());
        model.getEurope().saveTradeAccount(getEuropeanNation(), goods.getType(),
                account);
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
        // Counted before gold moves, so an overflowing account refuses the
        // sale.
        final EuropeTradeAccount account = model.getEurope()
                .getTradeAccount(getEuropeanNation(), goods.getType())
                .sold(goods.getAmount(), quote.getTotal(), false);
        final EuropeSaleWindow window = model.getEurope()
                .getSaleWindow(getEuropeanNation()).withSale(quote.getTotal());
        setGold(Math.addExact(getGold(), quote.getTotal()));
        model.getEurope().applyTrade(getEuropeanNation(), goods.getType(),
                goods.getAmount());
        model.getEurope().saveTradeAccount(getEuropeanNation(), goods.getType(),
                account);
        model.getEurope().saveSaleWindow(getEuropeanNation(), window);
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
        return !isNative() && !isDeclaredIndependence();
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

    /**
     * @return the supply cycle this computer colony is running, when one exists
     */
    public Optional<RivalTradeObjective> getRivalTradeObjective() {
        return Optional.ofNullable(rivalTradeObjective);
    }

    /**
     * Store the cycle that the computer colony continues on its owner turns.
     * Its identities are checked against live state before every use.
     *
     * @param objective required cycle
     * @throws IllegalStateException when this player is not a computer colony
     */
    public void setRivalTradeObjective(final RivalTradeObjective objective) {
        Preconditions.checkNotNull(objective, "Trade objective is null");
        Preconditions.checkState(computer && !isKing() && !isNative(),
                "Only computer colonies keep a trade objective");
        rivalTradeObjective = objective;
    }

    /** Forget the cycle, for example when its colony or carrier is gone. */
    public void clearRivalTradeObjective() {
        rivalTradeObjective = null;
    }

    /**
     * @return the colony whose supply round trip finished last, when one did
     */
    public Optional<Integer> getRivalLastServedColonyId() {
        return Optional.ofNullable(rivalLastServedColonyId);
    }

    /**
     * Record the colony whose supply round trip just finished, so the next
     * choice serves the colony after it.
     *
     * @param colonyId required colony of the finished round trip
     * @throws IllegalStateException when this player is not a computer colony
     */
    public void setRivalLastServedColonyId(final int colonyId) {
        Preconditions.checkArgument(colonyId >= 0,
                "Rival rotation colony identity is invalid");
        Preconditions.checkState(computer && !isKing() && !isNative(),
                "Only computer colonies keep a supply rotation");
        rivalLastServedColonyId = colonyId;
    }

    /**
     * @return IDs of the carriers parked in their colony ports, in ID order
     */
    public Set<Integer> getRivalParkedCarrierIds() {
        return new TreeSet<>(rivalParkedCarrierIds);
    }

    /**
     * Keep an idle carrier in its colony port until the supply rotation chooses
     * it or another owner use takes it.
     *
     * @param shipId required identity of the idle carrier
     * @throws IllegalStateException when this player is not a computer colony
     */
    public void parkRivalCarrier(final int shipId) {
        Preconditions.checkArgument(shipId >= 0,
                "Rival parked carrier identity is invalid");
        Preconditions.checkState(computer && !isKing() && !isNative(),
                "Only computer colonies keep a supply rotation");
        rivalParkedCarrierIds.add(shipId);
    }

    /** Release a parked carrier to ordinary orders or to another objective. */
    public void unparkRivalCarrier(final int shipId) {
        rivalParkedCarrierIds.remove(shipId);
    }

    /**
     * Restore the saved rotation. Malformed identities are rejected instead of
     * guessed. A well-formed identity of a unit or colony that no longer
     * exists, belongs to someone else or is not a ship is kept; the owner
     * retires it against live state before use.
     */
    private void loadRivalRotation(final Integer lastServedColonyId,
            final List<Integer> parkedCarrierIds) {
        Preconditions.checkArgument(
                lastServedColonyId == null || lastServedColonyId >= 0,
                "Rival rotation colony identity is invalid");
        Preconditions.checkArgument(parkedCarrierIds != null,
                "Rival parked carriers are missing");
        for (final Integer shipId : parkedCarrierIds) {
            Preconditions.checkArgument(shipId != null && shipId >= 0,
                    "Rival parked carrier identity is invalid");
            Preconditions.checkArgument(rivalParkedCarrierIds.add(shipId),
                    "Rival parked carrier identity is duplicated");
        }
        rivalLastServedColonyId = lastServedColonyId;
    }

    private void loadRivalTradeObjective(final RivalTradeObjectivePo saved) {
        if (saved != null) {
            rivalTradeObjective = RivalTradeObjective.load(saved);
        }
    }

    /**
     * @return the second-colony expansion this computer colony is running, when
     *         one exists
     */
    public Optional<RivalExpansionObjective> getRivalExpansionObjective() {
        return Optional.ofNullable(rivalExpansionObjective);
    }

    /**
     * Store the expansion that the computer colony continues on its owner
     * turns. Its identities are checked against live state before every use.
     *
     * @param objective required expansion
     * @throws IllegalStateException when this player is not a computer colony
     */
    public void setRivalExpansionObjective(
            final RivalExpansionObjective objective) {
        Preconditions.checkNotNull(objective, "Expansion objective is null");
        Preconditions.checkState(computer && !isKing() && !isNative(),
                "Only computer colonies keep an expansion objective");
        rivalExpansionObjective = objective;
    }

    /** Forget the expansion, for example when its founder is gone. */
    public void clearRivalExpansionObjective() {
        rivalExpansionObjective = null;
    }

    private void loadRivalExpansionObjective(
            final RivalExpansionObjectivePo saved) {
        if (saved != null) {
            rivalExpansionObjective = RivalExpansionObjective.load(saved);
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

    /**
     * Add a new immigrant to the European pier. It starts willing to board, so
     * only recruitment and earned arrivals call this helper.
     *
     * @param unitType required recruited unit type
     * @return the new waiting immigrant
     */
    private Unit addImmigrantInEurope(final UnitType unitType) {
        final Unit immigrant = model.addUnitInEurope(unitType, this);
        ((AbstractUnit) immigrant).becomeImmigrant();
        return immigrant;
    }

    public Unit recruit(final RecruitmentOffer offer) {
        Preconditions.checkNotNull(offer, "Recruitment offer is null");
        checkTurnPreparationComplete();
        Preconditions.checkState(isRecruitmentOfferCurrent(offer),
                "Recruitment offer is unavailable or stale");
        verifyAvailibilityOFGold(offer.getPrice());
        final Unit recruited = addImmigrantInEurope(offer.getType());
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
            // Every change is planned first; a refused reversal leaves the
            // world as it was before the declaration.
            final List<Unit> seized = planHomeEuropeSeizure();
            final List<VoyageReversalOffer> reversals = planInboundReversals();
            reversals.forEach(model::checkCurrentVoyageReversal);
            declaredIndependence = true;
            // The declaration fixes the rebellion's first colonies (R-0192).
            model.getColonies(this)
                    .forEach(colony -> rebellionColonyIds.add(colony.getId()));
            muster.forEach(
                    unit -> unit.assignMilitaryRole(MilitaryRole.CONTINENTAL));
            seized.forEach(unit -> ((AbstractUnit) unit).seizeByCrown());
            // Validated above; the ordinary reversal cannot refuse them now.
            model.applyVoyageReversals(reversals);
            // The Crown ruled these colonies until now, so it knows them; it
            // learns of later ones only by sight (R-0186).
            model.getKingOf(this).ifPresent(king -> model.getColonies(this)
                    .forEach(king::rememberColony));
        } finally {
            declaringIndependence = false;
        }
        model.fireIndependenceWasDeclared(this);
    }

    /**
     * Plan the Crown seizure of home Europe (R-0187): units in the port, on its
     * pier or aboard a ship there. Nothing changes here; the list is applied
     * only after the declaration is accepted.
     */
    private List<Unit> planHomeEuropeSeizure() {
        return model.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == this)
                .filter(unit -> ((AbstractUnit) unit).isInHomeEurope())
                .collect(Collectors.toList());
    }

    /**
     * Plan the reversal of every owned ship sailing to Europe (R-0187).
     * Outbound voyages are never offered. Each offer validates the live ship
     * now, so a refusal happens before any change.
     */
    private List<VoyageReversalOffer> planInboundReversals() {
        return model.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == this && unit.isShip())
                .filter(unit -> ((AbstractUnit) unit).isInboundToHomeEurope())
                .map(unit -> model.offerVoyageReversal((Ship) unit))
                .collect(Collectors.toList());
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
