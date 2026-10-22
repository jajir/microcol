package org.microcol.model.unit;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.Goods;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.model.EducationSkills;
import org.microcol.model.Founder;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.MilitaryPromotion;
import org.microcol.model.MilitaryRole;
import org.microcol.model.NativeConversion;
import org.microcol.model.NativeNation;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Unit Free Colonist.
 */
public class UnitFreeColonist extends CanMount {

    public final static int REQUIRED_HORSES_FOR_MOUNTED_UNIT = 50;

    public final static int REQUIRED_MUSKETS_FOR_ARMED_UNIT = 50;

    private final static int MAXIMAL_NUMBER_OF_TOOLS = CargoSlot.MAX_CARGO_SLOT_CAPACITY;
    private final static int MINIMAL_NUMBER_OF_TOOLS = 20;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    private boolean veteran;

    private MilitaryRole militaryRole = MilitaryRole.COLONIAL;

    /** Missionary role, separate from military role and civilian specialty. */
    private boolean missionary;

    private UnitType learnedType;

    private UnitType workSkill;

    private int workUpdates;

    /** Native affiliation of a convert; null for every other colonist. */
    private NativeNation convertNation;

    /** Owner updates spent unassigned over the whole life of this convert. */
    private int convertUnassignedUpdates;

    public UnitFreeColonist(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction unitAction,
            final int tools, final boolean holdingGuns, final boolean mounted) {
        super(model, id, placeBuilder, owner, availableMoves, unitAction);
        this.tools = tools;
        this.holdingGuns = holdingGuns;
        this.mounted = mounted;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setTools(tools);
        out.setMounted(mounted);
        out.setHoldingGuns(holdingGuns);
        out.setVeteran(isVeteran());
        out.setMilitaryRole(militaryRole);
        out.setMissionary(missionary);
        out.setWorkSkill(workSkill);
        out.setWorkUpdates(workUpdates);
        if (convertNation != null) {
            out.setNativeConvert(true);
            out.setNativeConvertNation(convertNation.name());
            out.setNativeConvertUnassignedUpdates(convertUnassignedUpdates);
        }
        return out;
    }

    /** Restore a saved missionary role; the loader validates its presence. */
    void restoreMissionary(final boolean value) {
        missionary = value;
    }

    /**
     * @return whether this person is a native convert, a colonist with a native
     *         affiliation who keeps it through Save and Continue (R-0149)
     */
    public boolean isNativeConvert() {
        return convertNation != null;
    }

    /**
     * @return the native nation the convert belongs to, empty for any other
     *         colonist
     */
    public Optional<NativeNation> getConvertNation() {
        return Optional.ofNullable(convertNation);
    }

    /**
     * @return owner updates this convert has spent unassigned, lifetime total
     */
    public int getConvertUnassignedUpdates() {
        return convertUnassignedUpdates;
    }

    /**
     * Turn a delivered ordinary colonist into a convert of a native nation. The
     * caller validates the mission and the placement before the unit exists;
     * the count starts at zero, after the current unit-update phase.
     *
     * @param nation required native nation of the settlement
     */
    public void becomeNativeConvert(final NativeNation nation) {
        Preconditions.checkNotNull(nation, "Native nation is required");
        Preconditions.checkState(
                !isNativeConvert() && !isMissionary()
                        && getType() == UnitType.COLONIST,
                "Only an ordinary colonist becomes a convert");
        convertNation = nation;
        convertUnassignedUpdates = 0;
    }

    /**
     * Restore a saved convert. The loader validates the nation and the count.
     *
     * @param nation  required saved native nation
     * @param updates saved lifetime unassigned updates, below the expiry
     */
    void restoreNativeConvert(final NativeNation nation, final int updates) {
        convertNation = nation;
        convertUnassignedUpdates = updates;
    }

    /**
     * Count one owner update spent unassigned. Assigned work never calls this,
     * so it pauses the count without resetting it.
     *
     * @return whether this update reached the expiry of the convert
     */
    public boolean addConvertUnassignedUpdate() {
        Preconditions.checkState(isNativeConvert(),
                "Only a native convert counts unassigned updates");
        convertUnassignedUpdates++;
        return convertUnassignedUpdates >= NativeConversion.RETURN_AFTER_UNASSIGNED_UPDATES;
    }

    /**
     * End the native affiliation of a convert after Las Casas joins (R-0182).
     * The person, place, cargo, job, equipment and resources stay; the
     * restrictions and the lifetime count go with the affiliation, so the saved
     * unit carries no convert fields.
     */
    public void releaseNativeConvert() {
        Preconditions.checkState(isNativeConvert(),
                "Only a native convert is released");
        convertNation = null;
        convertUnassignedUpdates = 0;
    }

    /**
     * @return whether this colonist holds the missionary role, by ordination or
     *         as a Jesuit
     */
    public boolean isMissionary() {
        return missionary || getType() == UnitType.JESUIT_MISSIONARY;
    }

    /**
     * Shared missionary expertise for mission, contest, incitement and
     * conversion: a Jesuit, or any missionary while its live owner holds Jean
     * de Brebeuf. The membership applies once; it is never a second multiplier.
     *
     * @return whether this missionary acts as an expert
     */
    public boolean hasEffectiveMissionaryExpertise() {
        return isMissionary() && (getType() == UnitType.JESUIT_MISSIONARY
                || getOwner().getCongressMembers()
                        .contains(Founder.JEAN_DE_BREBEUF));
    }

    /**
     * Give this colonist the missionary role. Identity, specialty, equipment,
     * military role and movement stay unchanged. The caller validates place,
     * owner, phase and assignment before this mutation.
     */
    public void becomeMissionary() {
        Preconditions.checkState(!isMissionary(),
                "Colonist is already a missionary");
        missionary = true;
    }

    /** @return whether this person's military rank is veteran */
    public boolean isVeteran() {
        return veteran || getType() == UnitType.VETERAN_SOLDIER;
    }

    void restoreVeteran(final boolean value) {
        veteran = value;
    }

    /** @return the persistent combat role; never null */
    public MilitaryRole getMilitaryRole() {
        return militaryRole;
    }

    /**
     * Check that a role fits the owner: Continental service belongs to a
     * colonial nation that declared independence, a royal role to a Crown.
     *
     * @param role  required role
     * @param owner required owner of the unit
     */
    static void validateRoleOwner(final MilitaryRole role, final Player owner) {
        Preconditions.checkArgument(role != null, "Missing military role");
        Preconditions.checkArgument(
                role != MilitaryRole.CONTINENTAL
                        || !owner.isKing() && owner.isDeclaredIndependence(),
                "Continental role needs a rebel nation");
        Preconditions.checkArgument(
                role != MilitaryRole.ROYAL || owner.isKing(),
                "Royal role needs a Crown owner");
    }

    /** Restore a saved role; the loader validates its placement. */
    void restoreMilitaryRole(final MilitaryRole role) {
        validateRoleOwner(role, getOwner());
        militaryRole = role;
    }

    /**
     * Give an ordinary armed colonist a special combat role without replacing
     * the registered unit. Identity, equipment, people, expertise and veteran
     * status stay; only the combat base changes.
     *
     * @param role required special role that fits the owner
     */
    public void assignMilitaryRole(final MilitaryRole role) {
        Preconditions.checkArgument(role != null && role.isSpecial(),
                "A special role is required");
        validateRoleOwner(role, getOwner());
        Preconditions.checkState(
                militaryRole == MilitaryRole.COLONIAL && isHoldingGuns()
                        && isAtPlaceLocation(),
                "Only an armed ordinary colonist on the map takes a role");
        militaryRole = role;
    }

    @Override
    public void takeOver(final Player player) {
        // A captured person no longer serves the former force.
        // The new owner is validated first, so a rejected call keeps the role.
        final boolean changesOwner = player != getOwner();
        super.takeOver(player);
        if (changesOwner) {
            militaryRole = MilitaryRole.COLONIAL;
        }
    }

    @Override
    protected boolean isDestroyedWhenDefeated() {
        return militaryRole.isSpecial();
    }

    /** @return a newly learned specialty, if this instance started unskilled */
    protected Optional<UnitType> getLearnedType() {
        return Optional.ofNullable(learnedType);
    }

    /** Change expertise without replacing the registered unit or equipment. */
    public void completeEducation(final UnitType skill) {
        Preconditions.checkArgument(
                skill != null && EducationSkills.isTeachingSkill(skill));
        if (skill == UnitType.VETERAN_SOLDIER) {
            Preconditions.checkState(!isVeteran());
            veteran = true;
        } else {
            // A native course also teaches an indentured servant directly; a
            // school course advances the servant one social stage first.
            Preconditions.checkState(getType() == UnitType.COLONIST
                    || getType() == UnitType.INDENTURED_SERVANT);
            learnedType = skill;
        }
        workSkill = null;
        workUpdates = 0;
    }

    /**
     * @return whether the current role is a civilian specialty; military-only
     *         veteran status is not one
     */
    public boolean hasCivilianSpecialty() {
        final UnitType type = getType();
        return type != UnitType.VETERAN_SOLDIER
                && (UnitType.EUROPEAN_SPECIALISTS.contains(type)
                        || UnitType.SCHOOL_ONLY_SPECIALISTS.contains(type));
    }

    /**
     * Turn this person into a free colonist without replacing the registered
     * unit. Military rank, equipment and placement stay; civilian work progress
     * is cleared.
     *
     * @param colony the colony that validates turn, ownership, identity and
     *               teaching or school assignments before anything changes
     */
    public void clearCivilianSpecialty(final Colony colony) {
        Preconditions.checkNotNull(colony, "Colony is required");
        colony.validateClearSpecialty(this);
        Preconditions.checkState(hasCivilianSpecialty(),
                "Colonist has no civilian specialty");
        veteran = isVeteran();
        learnedType = UnitType.COLONIST;
        workSkill = null;
        workUpdates = 0;
    }

    public void restoreWorkExperience(final UnitType skill,
            final Integer updates) {
        Preconditions.checkArgument(updates != null && updates >= 0
                && updates < 20 && (skill != null || updates == 0)
                && (skill == null || EducationSkills.isWorkSkill(skill))
                && (updates == 0 || getType() == UnitType.COLONIST));
        workSkill = skill;
        workUpdates = updates;
    }

    /** @return current productive-job progress, without advancing it */
    public int getWorkUpdates() {
        return workUpdates;
    }

    /** @return the tracked productive specialty, when a job has begun */
    public Optional<UnitType> getWorkSkill() {
        return Optional.ofNullable(workSkill);
    }

    /** Credit one committed productive owner update. */
    public boolean recordWork(final UnitType job, final boolean productive,
            final boolean schoolCompleted) {
        Preconditions
                .checkArgument(job == null || EducationSkills.isWorkSkill(job));
        if (getType() != UnitType.COLONIST || job == null) {
            workSkill = null;
            workUpdates = 0;
            return false;
        }
        if (workSkill != job) {
            workSkill = job;
            workUpdates = 0;
        }
        if (productive && !schoolCompleted) {
            workUpdates++;
            if (workUpdates == 20) {
                completeEducation(job);
                return true;
            }
        }
        return false;
    }

    /**
     * Advance one earned military step after a resolved, eligible victory. The
     * registered unit and its equipment remain unchanged.
     *
     * @return immutable facts of the actual step
     */
    public MilitaryPromotion promoteAfterCombatVictory() {
        // A defender fighting from its colony job is promoted like a unit on
        // the map; the registered unit and its place stay.
        Preconditions.checkState(
                isHoldingGuns() && !isVeteran() && !militaryRole.isSpecial()
                        && (isAtPlaceLocation() || isAtPlaceColonyField()
                                || isAtPlaceConstructionSlot()),
                "Unit cannot gain military rank");
        final UnitType previous = getType();
        final UnitType next = advanceMilitaryRank();
        return new MilitaryPromotion(getId(), previous, next, false,
                isVeteran());
    }

    protected UnitType advanceMilitaryRank() {
        veteran = true;
        return getType();
    }

    @Override
    public void placeToColonyField(final ColonyField colonyField,
            final GoodsType producedGoodsType) {
        validateColonyField(colonyField, producedGoodsType);
        unequipAll();
        becomeCivilianWhile(
                () -> super.placeToColonyField(colonyField, producedGoodsType),
                this::isAtPlaceColonyField);
    }

    /**
     * A resident worker is a civilian again. The role is cleared before the
     * placement can announce itself, so a listener never sees a special role in
     * a job; if the placement itself fails before the unit moved, the role
     * returns.
     */
    private void becomeCivilianWhile(final Runnable placement,
            final BooleanSupplier placed) {
        final MilitaryRole previous = militaryRole;
        militaryRole = MilitaryRole.COLONIAL;
        try {
            placement.run();
        } catch (final RuntimeException | Error failure) {
            if (!placed.getAsBoolean()) {
                militaryRole = previous;
            }
            throw failure;
        }
    }

    @Override
    public void placeToColonyStructureSlot(
            final ConstructionSlot structureSlot) {
        validateColonyStructureSlot(structureSlot);
        unequipAll();
        becomeCivilianWhile(
                () -> super.placeToColonyStructureSlot(structureSlot),
                this::isAtPlaceConstructionSlot);
    }

    @Override
    public UnitType getType() {
        return learnedType == null ? UnitType.COLONIST : learnedType;
    }

    /**
     * Every free colonist is a passenger. Answering without the unit type keeps
     * cargo restoration valid while a subclass is still constructed.
     */
    @Override
    public boolean isStorable() {
        return true;
    }

    /**
     * @param type muskets, horses or tools
     * @param buy  true for a purchase, false for sale of carried equipment
     * @return current market quote without changing this colonist
     */
    public EuropeTradeQuote getEuropeEquipmentQuote(GoodsType type,
            boolean buy) {
        Preconditions
                .checkArgument(
                        type == GoodsType.MUSKET || type == GoodsType.HORSE
                                || type == GoodsType.TOOLS,
                        "Unsupported equipment");
        int amount;
        if (type == GoodsType.MUSKET || type == GoodsType.HORSE) {
            amount = 50;
        } else { // TOOLS
            amount = buy ? 100 : tools;
        }
        return getOwner().quoteEuropeTrade(Goods.of(type, amount), buy);
    }

    /**
     * @param type muskets, horses or tools
     * @param buy  true for a purchase, false for a sale
     * @return a rejection reason, or empty when the current trade is allowed
     */
    public Optional<EquipmentProblem> getEuropeEquipmentProblem(GoodsType type,
            boolean buy) {
        Preconditions.checkArgument(
                type == GoodsType.MUSKET || type == GoodsType.HORSE
                        || type == GoodsType.TOOLS,
                "Unsupported equipment type: %s", type);

        if (!getModel().isGameRunning()
                || getAction().getType() == UnitActionType.fortify
                || getModel().getCurrentPlayer() != getOwner()
                || !isAtEuropePier() || getOwner().getAllUnits().stream()
                        .noneMatch(u -> u == this)) {
            return Optional.of(EquipmentProblem.UNIT_UNAVAILABLE);
        }

        if (!getOwner().canTradeInEurope()) {
            return Optional.of(EquipmentProblem.TRADE_RESTRICTED);
        }

        if (getOwner().isBoycotted(type)) {
            return Optional.of(EquipmentProblem.BOYCOTTED);
        }

        boolean present = false;
        if (type == GoodsType.MUSKET) {
            present = holdingGuns;
        } else if (type == GoodsType.HORSE) {
            present = mounted;
        } else if (type == GoodsType.TOOLS) {
            present = tools > 0;
        }

        if (buy == present) {
            return Optional.of(EquipmentProblem.ALREADY_SET);
        }

        if (buy && ((type == GoodsType.TOOLS && (holdingGuns || mounted))
                || (type != GoodsType.TOOLS && tools > 0))) {
            return Optional.of(EquipmentProblem.INCOMPATIBLE_EQUIPMENT);
        }

        int price;
        try {
            price = getEuropeEquipmentQuote(type, buy).getTotal();
        } catch (ArithmeticException e) {
            return Optional.of(EquipmentProblem.GOLD_LIMIT);
        }

        if (buy) {
            if (price > getOwner().getGold()) {
                return Optional.of(EquipmentProblem.NOT_ENOUGH_GOLD);
            }
        } else {
            if (price > Integer.MAX_VALUE - getOwner().getGold()) {
                return Optional.of(EquipmentProblem.GOLD_LIMIT);
            }
        }

        return Optional.empty();
    }

    /**
     * Validate the live pier, equipment and offer before any treasury mutation.
     *
     * @param type    muskets, horses or tools
     * @param buy     true for a purchase, false for a sale
     * @param offered the exact quote the caller confirmed
     * @throws IllegalStateException when eligibility or the quote changed
     */
    public void tradeEquipmentInEurope(GoodsType type, boolean buy,
            EuropeTradeQuote offered) {
        Preconditions.checkNotNull(offered, "offered quote must not be null");
        Optional<EquipmentProblem> problem = getEuropeEquipmentProblem(type,
                buy);
        Preconditions.checkState(problem.isEmpty(),
                "Equipment trade rejected: %s", problem.orElse(null));

        EuropeTradeQuote fresh = getEuropeEquipmentQuote(type, buy);
        Preconditions.checkState(fresh.equals(offered),
                "Equipment offer changed: %s vs %s", offered, fresh);

        if (buy) {
            getOwner().buyGoods(fresh.getGoods());
        } else {
            getOwner().sellGoods(fresh.getGoods());
        }

        if (type == GoodsType.MUSKET) {
            holdingGuns = buy;
        } else if (type == GoodsType.HORSE) {
            mounted = buy;
        } else if (type == GoodsType.TOOLS) {
            tools = buy ? 100 : 0;
        }
    }

    private void unequipAll() {
        // Validate every return before the first inventory mutation.
        if (isHoldingGuns())
            checkEquipmentChange(GoodsType.MUSKET, false);
        if (isHoldingTools())
            checkEquipmentChange(GoodsType.TOOLS, false);
        if (isMounted())
            checkEquipmentChange(GoodsType.HORSE, false);
        if (isMounted()) {
            unequipWithHorses();
        }
        if (isHoldingGuns()) {
            unequipWithMuskets();
        }
        if (isHoldingTools()) {
            unequipWithTools();
        }
    }

    @Override
    public boolean canAttack() {
        return isHoldingGuns();
    }

    @Override
    public int getSpeed() {
        if (isMounted()) {
            return 5;
        } else {
            return 1;
        }
    }

    /**
     * Ordinary scouts are mounted colonists without muskets; seasoned scouts
     * are the specialist of that name. Both see two squares further and enter
     * lost-city rumors.
     *
     * @return whether this unit is an ordinary or seasoned scout
     */
    public boolean isScout() {
        return getType() == UnitType.SEASONED_SCOUT || mounted && !holdingGuns;
    }

    @Override
    public int getSightRadius() {
        return withFounderSight(isScout() ? 2 : 1);
    }

    private void revealExpandedSight(final int previousRadius) {
        if (isAtPlaceLocation() && getSightRadius() > previousRadius) {
            getOwner().revealMapForUnit(this);
        }
    }

    @Override
    public double getMilitaryStrenght() {
        if (militaryRole.isSpecial() && isHoldingGuns()) {
            return militaryRole.getBase(isMounted());
        }
        if (isMounted()) {
            if (isHoldingGuns()) {
                return isVeteran() ? 4.5 : 2;
            } else {
                return 1.5;
            }
        } else {
            if (isHoldingGuns()) {
                return isVeteran() ? 3 : 1.5;
            } else {
                return 1;
            }
        }
    }

    @Override
    protected void loseCombatEquipment() {
        Preconditions.checkState(!militaryRole.isSpecial(),
                "A defeated special role is destroyed, not downgraded");
        if (mounted) {
            mounted = false;
        } else if (holdingGuns) {
            holdingGuns = false;
            clearFortificationAfterDefeat();
        } else {
            throw new IllegalStateException("Unarmed ordinary loser");
        }
    }

    public void equipWithHorses() {
        final int previousRadius = getSightRadius();
        checkEquipmentChange(GoodsType.HORSE, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int horses = colony.getWarehouse().getGoods(GoodsType.HORSE)
                .getAmount();
        Preconditions.checkState(horses >= REQUIRED_HORSES_FOR_MOUNTED_UNIT,
                "In colony (%s) is not enought horses to mount unit on them",
                colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getWarehouse().removeGoods(
                Goods.of(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT));
        mounted = true;
        revealExpandedSight(previousRadius);
    }

    public void unequipWithHorses() {
        checkEquipmentChange(GoodsType.HORSE, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(
                Goods.of(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT));
        mounted = false;
    }

    public void equipWithMuskets() {
        checkEquipmentChange(GoodsType.MUSKET, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int muskets = colony.getWarehouse().getGoods(GoodsType.MUSKET)
                .getAmount();
        Preconditions.checkState(muskets >= REQUIRED_MUSKETS_FOR_ARMED_UNIT,
                "In colony (%s) is not enought muskets to arm unit with them",
                colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getWarehouse().removeGoods(
                Goods.of(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT));
        holdingGuns = true;
    }

    public void unequipWithMuskets() {
        final int previousRadius = getSightRadius();
        checkEquipmentChange(GoodsType.MUSKET, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(
                Goods.of(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT));
        holdingGuns = false;
        revealExpandedSight(previousRadius);
    }

    public void equipWithTools() {
        checkEquipmentChange(GoodsType.TOOLS, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int totalTools = colony.getWarehouse().getGoods(GoodsType.TOOLS)
                .getAmount();
        Preconditions.checkState(totalTools >= MINIMAL_NUMBER_OF_TOOLS,
                "In colony (%s) are not enough tools", colony);
        if (isMounted()) {
            unequipWithHorses();
        }
        if (isHoldingGuns()) {
            unequipWithMuskets();
        }
        final int removeTools = Math.min(totalTools, MAXIMAL_NUMBER_OF_TOOLS);
        colony.getWarehouse()
                .removeGoods(Goods.of(GoodsType.TOOLS, removeTools));
        tools = removeTools;
    }

    public void unequipWithTools() {
        checkEquipmentChange(GoodsType.TOOLS, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, tools));
        tools = 0;
    }

    public enum EquipmentProblem {
        UNIT_UNAVAILABLE, ALREADY_SET, MISSING_GOODS, STORAGE_FULL,
        INCOMPATIBLE_EQUIPMENT, NOT_ENOUGH_GOLD, TRADE_RESTRICTED, GOLD_LIMIT,
        BOYCOTTED
    }

    public java.util.Optional<EquipmentProblem> getEquipmentProblem(
            GoodsType type, boolean equip) {
        com.google.common.base.Preconditions.checkArgument(
                type == GoodsType.MUSKET || type == GoodsType.HORSE
                        || type == GoodsType.TOOLS,
                "Unsupported GoodsType: %s", type);

        if (!getModel().isGameRunning()
                || getAction().getType() == UnitActionType.fortify
                || !getModel().getCurrentPlayer().equals(getOwner())
                || !isAtPlaceLocation()
                || !getOwner().getAllUnits().stream().anyMatch(u -> u == this)
                || getModel().getColonyAt(getLocation()).isEmpty()
                || !getModel().getColonyAt(getLocation()).get().isValid()
                || !getModel().getColonyAt(getLocation()).get().getOwner()
                        .equals(getOwner())) {
            return java.util.Optional.of(EquipmentProblem.UNIT_UNAVAILABLE);
        }

        if (equip) {
            if (type == GoodsType.MUSKET && holdingGuns)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.HORSE && mounted)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.TOOLS && tools > 0)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
        } else {
            if (type == GoodsType.MUSKET && !holdingGuns)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.HORSE && !mounted)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.TOOLS && tools == 0)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
        }

        if (equip && type == GoodsType.TOOLS) {
            Colony colony = getModel().getColonyAt(getLocation()).get();
            int availableTools = colony.getWarehouse().getGoods(GoodsType.TOOLS)
                    .getAmount();
            if (availableTools < MINIMAL_NUMBER_OF_TOOLS)
                return java.util.Optional.of(EquipmentProblem.MISSING_GOODS);
        }

        java.util.Map<GoodsType, Integer> delta = getEquipmentGoodsChange(type,
                equip);
        Colony colony = getModel().getColonyAt(getLocation()).get();
        for (GoodsType t : delta.keySet()) {
            int stock = colony.getWarehouse().getGoods(t).getAmount();
            int newStock = stock + delta.get(t);
            if (newStock < 0)
                return java.util.Optional.of(EquipmentProblem.MISSING_GOODS);
            int capacity = colony.getWarehouse().getStorageCapacity(t)
                    .getAmount();
            if (newStock > capacity)
                return java.util.Optional.of(EquipmentProblem.STORAGE_FULL);
        }

        return java.util.Optional.empty();
    }

    public java.util.Map<GoodsType, Integer> getEquipmentGoodsChange(
            GoodsType type, boolean equip) {
        com.google.common.base.Preconditions.checkArgument(
                type == GoodsType.MUSKET || type == GoodsType.HORSE
                        || type == GoodsType.TOOLS,
                "Unsupported GoodsType: %s", type);
        Colony colony = verifyThatUnitIsOutsideColony();

        java.util.Map<GoodsType, Integer> changes = new java.util.LinkedHashMap<>();

        final int gunCost = REQUIRED_MUSKETS_FOR_ARMED_UNIT;
        final int horseCost = REQUIRED_HORSES_FOR_MOUNTED_UNIT;
        final int toolsMax = MAXIMAL_NUMBER_OF_TOOLS;

        if (type == GoodsType.MUSKET) {
            changes.put(GoodsType.MUSKET, equip ? -gunCost : gunCost);
            if (equip && tools > 0)
                changes.put(GoodsType.TOOLS, tools);
        } else if (type == GoodsType.HORSE) {
            changes.put(GoodsType.HORSE, equip ? -horseCost : horseCost);
            if (equip && tools > 0)
                changes.put(GoodsType.TOOLS, tools);
        } else if (type == GoodsType.TOOLS) {
            int consume = Math.min(
                    colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount(),
                    toolsMax);
            changes.put(GoodsType.TOOLS, equip ? -consume : tools);
            if (equip && holdingGuns)
                changes.put(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT);
            if (equip && mounted)
                changes.put(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT);
        }

        return java.util.Collections.unmodifiableMap(changes);
    }

    private void checkEquipmentChange(GoodsType type, boolean equip) {
        java.util.Optional<EquipmentProblem> problem = getEquipmentProblem(type,
                equip);
        com.google.common.base.Preconditions.checkState(problem.isEmpty(),
                "Equipment change rejected: %s", problem.orElse(null));
    }

    private Colony verifyThatUnitIsOutsideColony() {
        final Optional<Colony> oColony = getModel().getColonyAt(getLocation());
        Preconditions.checkState(oColony.isPresent(),
                "Unit at (%s) is not outside colony", getLocation());
        final Colony colony = oColony.get();
        Preconditions.checkState(getOwner().equals(colony.getOwner()),
                "Colony at (%s) doesn't belong to same owner as unit (%s)",
                getLocation(), this);
        return colony;
    }

    public boolean isHoldingTools() {
        return tools > 0;
    }

    /**
     * @return the tools
     */
    public int getTools() {
        return tools;
    }

    /**
     * Equipment this colonist carries, as the goods it would return to a
     * warehouse: muskets and horses at their fixed cost, tools at the remaining
     * amount.
     *
     * @return immutable amounts of muskets, horses and tools
     */
    public java.util.Map<GoodsType, Integer> getCarriedEquipment() {
        return ImmutableMap.of(GoodsType.MUSKET,
                holdingGuns ? REQUIRED_MUSKETS_FOR_ARMED_UNIT : 0,
                GoodsType.HORSE, mounted ? REQUIRED_HORSES_FOR_MOUNTED_UNIT : 0,
                GoodsType.TOOLS, tools);
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(int tools) {
        Preconditions.checkState(
                getAction().getType() != UnitActionType.fortify
                        || this.tools == tools,
                "Cancel fortification before equipping");
        this.tools = tools;
    }

    /**
     * Spend the pioneer work load; only the plow and forest clearing lifecycles
     * call this.
     */
    void consumeWorkTools(final int amount) {
        Preconditions
                .checkState(
                        getAction().getType() == UnitActionType.plowField
                                || getAction()
                                        .getType() == UnitActionType.clearForest
                                || getAction()
                                        .getType() == UnitActionType.buildRoad,
                        "Tools are consumed only by pioneer work");
        Preconditions.checkArgument(amount > 0 && amount <= tools,
                "Cannot consume %s of %s tools", amount, tools);
        tools -= amount;
    }

    /**
     * @return the holdingGuns
     */
    public boolean isHoldingGuns() {
        return holdingGuns;
    }

    /**
     * @param holdingGuns the holdingGuns to set
     */
    public void setHoldingGuns(boolean holdingGuns) {
        Preconditions.checkState(
                getAction().getType() != UnitActionType.fortify
                        || this.holdingGuns == holdingGuns,
                "Cancel fortification before equipping");
        final int previousRadius = getSightRadius();
        this.holdingGuns = holdingGuns;
        revealExpandedSight(previousRadius);
    }

    /**
     * @return the mounted
     */
    public boolean isMounted() {
        return mounted;
    }

    /**
     * @param mounted the mounted to set
     */
    public void setMounted(boolean mounted) {
        Preconditions.checkState(
                getAction().getType() != UnitActionType.fortify
                        || this.mounted == mounted,
                "Cancel fortification before equipping");
        final int previousRadius = getSightRadius();
        this.mounted = mounted;
        revealExpandedSight(previousRadius);
    }

}
