package org.microcol.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.Ship;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Player model class. Class doesn't perform itself it just provide methods to
 * implement players behavior somewhere else.
 */
public abstract class Player {

    public static final int CANT_DECLARE_INDEPENDENCE_BEFORE_YEAR = 1700;

    public static final int REQUIRED_NUMBER_OF_COLONYES = 3;

    private final Model model;

    private final String name;

    private final boolean computer;

    private boolean declaredIndependence;

    private int gold;

    private final Map<String, Object> extraData = new HashMap<>();

    private final Visibility visibility;

    private final RecruitmentPool recruitment;

    private final Congress congress;

    private final Map<UnitType, Integer> unitPurchaseCounts = new HashMap<>();

    protected Player(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible,
            final RecruitmentPo recruitmentPo, final CongressPo congressPo) {
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
        if (recruitment != null) {
            try {
                recruitment.price(model.getDifficulty(), name);
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
            final Player player = new PlayerHuman(playerPo.getName(),
                    playerPo.isComputer(), playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(),
                    playerPo.getRecruitment(), playerPo.getCongress());
            player.loadUnitPurchaseCounts(playerPo.getUnitPurchaseCounts());
            return player;
        } else {
            Preconditions.checkArgument(playerPo.getRecruitment() == null,
                    "Kings cannot have recruitment pools");
            Preconditions.checkArgument(playerPo.getCongress() == null,
                    "Kings cannot have Congress");
            Preconditions.checkArgument(
                    playerPo.getUnitPurchaseCounts().isEmpty(),
                    "Kings cannot have purchase counts");
            final Player subdued = playerStore
                    .getPlayerByName(playerPo.getWhosKingThisPlayerIs());
            return new PlayerKing(playerPo.getName(), playerPo.isComputer(),
                    playerPo.getGold(), model,
                    playerPo.isDeclaredIndependence(), subdued,
                    playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(),
                    playerPo.getKingsTaxPercentage());
        }
    }

    public boolean isVisible(final Location location) {
        model.isValid(location);
        return visibility.isVisible(location);
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
        final Map<String, Integer> counts = new HashMap<>();
        unitPurchaseCounts
                .forEach((type, count) -> counts.put(type.name(), count));
        out.setUnitPurchaseCounts(counts);
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

    void startTurn() {
        final boolean hadFranklin = getCongressMembers()
                .contains(Founder.BENJAMIN_FRANKLIN);
        getAllUnits().stream().filter(Unit::isShip).map(Ship.class::cast)
                .collect(java.util.stream.Collectors.toList()).forEach(ship -> {
                    ship.advanceVoyage();
                    ship.advanceRepair();
                });
        settleEarnedImmigrants();
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
        }
        getColonies().forEach(colony -> colony.startTurn());
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

    private void settleEarnedImmigrants() {
        if (recruitment == null || !canTradeInEurope()) {
            return;
        }
        while (hasPendingImmigrant()) {
            final int threshold = recruitment
                    .checkCanSettleEarned(model.getDifficulty(), name);
            Preconditions.checkState(model.canAddUnitInEurope(),
                    "No unit IDs remain for an earned immigrant");
            final int slot = recruitment.chooseEarnedSlot();
            final UnitType type = recruitment.getCandidateType(slot);
            model.addUnitInEurope(type, this);
            recruitment.settleEarned(slot, threshold);
            model.getTurnEventStore()
                    .add(TurnEventProvider.getNewUnitInEurope(this));
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
        if (t == TerrainType.OCEAN) {
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
        checkTurnPreparationComplete();
        final int price = quoteEuropeTrade(goods, true).getTotal();
        Preconditions.checkState(canTradeInEurope(),
                "European trade is unavailable");
        verifyAvailibilityOFGold(price);
        setGold(getGold() - price);
    }

    public void sellGoods(final Goods goods) {
        checkTurnPreparationComplete();
        final int price = quoteEuropeTrade(goods, false).getTotal();
        Preconditions.checkState(canTradeInEurope(),
                "European trade is unavailable");
        setGold(Math.addExact(getGold(), price));
    }

    /** @return whether this player still has access to the home market */
    public boolean canTradeInEurope() {
        return !isDeclaredIndependence();
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
                .getGoodsTradeForType(goods.getType());
        return new EuropeTradeQuote(goods, purchase,
                purchase ? trade.getBuyPrice() : trade.getSellPrice(),
                model.getKingsTaxForPlayer(this));
    }

    /**
     * @param type required market commodity
     * @return an affordable cargo-slot quantity including purchase tax
     */
    public Goods getMaxBuyableGoods(final GoodsType type) {
        if (!canTradeInEurope()) {
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
        recruitment.replace(offer.getSlot());
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

    public void declareIndependence() {
        checkTurnPreparationComplete();
        Preconditions.checkState(isPossibleToDecalareIndependence(),
                "It's not possible to declare independence");
        if (model.fireBeforeDeclaringIndependence(this)) {
            declaredIndependence = true;
            model.fireIndependenceWasDeclared(this);
        }
    }

    public boolean isPossibleToDecalareIndependence() {
        if (isDeclaredIndependence()) {
            // Already was declared.
            return false;
        }
        if (model.getCalendar()
                .getCurrentYear() < CANT_DECLARE_INDEPENDENCE_BEFORE_YEAR) {
            // Too early to declare independence.
            return false;
        }
        if (model.getColonies(this).size() < REQUIRED_NUMBER_OF_COLONYES) {
            // Not enough colonies.
            return false;
        }
        return true;
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
