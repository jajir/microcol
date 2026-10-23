package org.microcol.model;

import java.util.Optional;

import org.microcol.model.unit.UnitFreeColonist;

/**
 * A proposed battle bound to the participants and state shown for confirmation.
 */
public final class AttackOrder {
    private final AbstractUnit attacker;
    private final Unit defender;
    private final Player attackingPlayer;
    private final Player defendingPlayer;
    private final Location origin;
    private final Location target;
    private final boolean declarationRequired;
    private final boolean privateerException;
    private final EuropeanRelationState originalRelation;
    private final Optional<CombatStrengths> combatStrengths;
    private final Optional<NavalCombatPreview> navalPreview;

    private final String promotionInputs;

    /**
     * Frozen colony defense when the target tile is a colony fought by a land
     * unit; the battlefield is then the colony center, not the defender's
     * place.
     */
    private final Optional<ColonyDefense> colonyDefense;

    AttackOrder(final AbstractUnit attacker, final Unit defender) {
        this(attacker, defender, Optional.empty());
    }

    AttackOrder(final AbstractUnit attacker, final Unit defender,
            final Optional<ColonyDefense> colonyDefense) {
        this(attacker, defender, colonyDefense, attacker.getLocation());
    }

    /**
     * A quote of the same battle fought from another square (R-0170). Its
     * strengths use that square; executing it from anywhere else is rejected by
     * the ordinary participant check.
     */
    AttackOrder(final AbstractUnit attacker, final Unit defender,
            final Optional<ColonyDefense> colonyDefense,
            final Location origin) {
        this.attacker = attacker;
        this.defender = defender;
        this.colonyDefense = colonyDefense;
        attackingPlayer = attacker.getOwner();
        defendingPlayer = defender.getOwner();
        this.origin = origin;
        target = colonyDefense.map(ColonyDefense::getBattlefield)
                .orElseGet(defender::getLocation);
        privateerException = attacker.model.isPrivateerNavalException(attacker,
                defender);
        originalRelation = isEuropeanPair(attackingPlayer, defendingPlayer)
                ? attacker.model.getEuropeanRelation(attackingPlayer,
                        defendingPlayer)
                : null;
        declarationRequired = isDeclarationNeeded(attacker.model,
                attackingPlayer, defendingPlayer, privateerException);
        combatStrengths = colonyDefense.isPresent()
                ? CombatStrengths.forColonyBattleFrom(attacker.model, attacker,
                        origin, colonyDefense.get())
                : CombatStrengths.forBattleFrom(attacker.model, attacker,
                        origin, defender);
        navalPreview = attacker.isShip() && defender.isShip()
                ? Optional.of(new NavalCombatPreview(attacker.model,
                        (org.microcol.model.unit.Ship) attacker,
                        (org.microcol.model.unit.Ship) defender))
                : Optional.empty();
        promotionInputs = promotionInputs(attacker, defender);
    }

    public Unit getAttacker() {
        return attacker;
    }

    public Unit getDefender() {
        return defender;
    }

    public Model getModel() {
        return attacker.model;
    }

    public boolean isDeclarationRequired() {
        return declarationRequired;
    }

    /** @return whether this battle preserves the relation by privateer rule */
    public boolean isPrivateerException() {
        return privateerException;
    }

    EuropeanRelationState getOriginalRelation() {
        return originalRelation;
    }

    public Optional<CombatStrengths> getCombatStrengths() {
        return combatStrengths;
    }

    public Optional<NavalCombatPreview> getNavalPreview() {
        return navalPreview;
    }

    /**
     * @return the frozen defense of the colony at the battlefield, present when
     *         a land unit fights for a colony tile
     */
    public Optional<ColonyDefense> getColonyDefense() {
        return colonyDefense;
    }

    /** @return the battlefield tile; the colony center for a colony battle */
    public Location getBattlefield() {
        return target;
    }

    Location getTarget() {
        return target;
    }

    boolean matchesParticipants() {
        // A resident defends from its job; the colony defense quote then
        // verifies the actual place of every roster member instead.
        return attacker.getOwner() == attackingPlayer
                && defender.getOwner() == defendingPlayer
                && attacker.isAtPlaceLocation()
                && origin.equals(attacker.getLocation())
                && (colonyDefense.isPresent() || defender.isAtPlaceLocation()
                        && target.equals(defender.getLocation()));
    }

    boolean matchesPromotionInputs() {
        return promotionInputs.equals(promotionInputs(attacker, defender));
    }

    /**
     * @return whether both players are colonial nations, the only pair with a
     *         European treaty state, declaration or aggression rules
     */
    static boolean isEuropeanPair(final Player first, final Player second) {
        return first.isColonialNation() && second.isColonialNation();
    }

    /**
     * A European declaration is needed only between colonial nations at peace.
     * Natives take no declaration: the attack itself is their legal act.
     */
    static boolean isDeclarationNeeded(final Model model, final Player attacker,
            final Player defender, final boolean privateerException) {
        return isEuropeanPair(attacker, defender) && !privateerException
                && !model.areHostile(attacker, defender);
    }

    private static String promotionInputs(final Unit attacker,
            final Unit defender) {
        return promotionInput(attacker) + '/' + promotionInput(defender);
    }

    private static String promotionInput(final Unit unit) {
        if (!(unit instanceof UnitFreeColonist)) {
            return unit.getType().name();
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return unit.getType().name() + ':' + colonist.isVeteran() + ':'
                + colonist.getMilitaryRole() + ':' + colonist.isHoldingGuns()
                + ':' + colonist.isMounted() + ':'
                + unit.getOwner().getCongressMembers()
                        .contains(Founder.GEORGE_WASHINGTON);
    }

    /**
     * Revalidates the proposal before changing diplomacy, movement or units.
     */
    public void execute() {
        attacker.executeAttack(this);
        // The battle, its capture and its losses are complete (R-0192).
        attacker.checkRebellionLoss();
    }
}
