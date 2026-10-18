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
        this.attacker = attacker;
        this.defender = defender;
        this.colonyDefense = colonyDefense;
        attackingPlayer = attacker.getOwner();
        defendingPlayer = defender.getOwner();
        origin = attacker.getLocation();
        target = colonyDefense.map(ColonyDefense::getBattlefield)
                .orElseGet(defender::getLocation);
        privateerException = attacker.model.isPrivateerNavalException(attacker,
                defender);
        originalRelation = attackingPlayer.isKing() || defendingPlayer.isKing()
                ? null
                : attacker.model.getEuropeanRelation(attackingPlayer,
                        defendingPlayer);
        declarationRequired = !privateerException
                && !attacker.model.areHostile(attackingPlayer, defendingPlayer);
        combatStrengths = colonyDefense.isPresent()
                ? CombatStrengths.forColonyBattle(attacker.model, attacker,
                        colonyDefense.get())
                : CombatStrengths.forBattle(attacker.model, attacker, defender);
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
    }
}
