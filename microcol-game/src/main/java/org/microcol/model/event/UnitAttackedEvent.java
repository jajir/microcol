package org.microcol.model.event;

import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.NavalCombatOutcome;
import org.microcol.model.Model;
import org.microcol.model.MilitaryPromotion;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitAttackedEvent extends AbstractModelEvent {
    private final Unit attacker;
    private final Unit defender;
    private final Unit destroyed;
    private final Unit defeated;
    private final NavalCombatOutcome navalOutcome;

    private final MilitaryPromotion promotion;

    public UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed) {
        this(model, attacker, defender, defeated, wasDestroyed, null, null);
    }

    public UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed, final NavalCombatOutcome navalOutcome) {
        this(model, attacker, defender, defeated, wasDestroyed, navalOutcome,
                null);
    }

    public UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed, final MilitaryPromotion promotion) {
        this(model, attacker, defender, defeated, wasDestroyed, null,
                promotion);
    }

    private UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed, final NavalCombatOutcome navalOutcome,
            final MilitaryPromotion promotion) {
        super(model);
        this.navalOutcome = navalOutcome;
        this.promotion = promotion;
        this.attacker = Preconditions.checkNotNull(attacker);
        this.defender = Preconditions.checkNotNull(defender);
        this.defeated = Preconditions.checkNotNull(defeated);
        this.destroyed = wasDestroyed ? defeated : null;
    }

    public Optional<NavalCombatOutcome> getNavalOutcome() {
        return Optional.ofNullable(navalOutcome);
    }

    public Optional<MilitaryPromotion> getPromotion() {
        return Optional.ofNullable(promotion);
    }

    public Optional<Location> getOriginalAttackerLocation() {
        return getNavalOutcome().map(NavalCombatOutcome::getAttackerLocation);
    }

    public Optional<Location> getOriginalDefenderLocation() {
        return getNavalOutcome().map(NavalCombatOutcome::getDefenderLocation);
    }

    /**
     * A player always knows about its own fights; other fights are visible only
     * when both original fighting locations are in present sight, so a
     * half-visible fight discloses nothing about the hidden participant.
     *
     * @param player required observer
     * @return whether the observer may be shown this fight
     */
    public boolean canPlayerSeeAttack(final Player player) {
        Preconditions.checkNotNull(player);
        return attacker.getOwner().equals(player)
                || defender.getOwner().equals(player)
                || isInSight(player, getOriginalAttackerLocation(), attacker)
                        && isInSight(player, getOriginalDefenderLocation(),
                                defender);
    }

    private static boolean isInSight(final Player player,
            final Optional<Location> original, final Unit unit) {
        final Optional<Location> location = original.isPresent() ? original
                : unit.isAtPlaceLocation() ? Optional.of(unit.getLocation())
                        : Optional.empty();
        return location.filter(player::isCurrentlyVisible).isPresent();
    }

    public Unit getAttacker() {
        return attacker;
    }

    public Unit getDefender() {
        return defender;
    }

    public Unit getDestroyed() {
        return destroyed;
    }

    public Unit getDefeated() {
        return defeated;
    }

    public boolean wasDestroyed() {
        return destroyed != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("attacker", attacker)
                .add("defender", defender).add("destroyed", destroyed)
                .toString();
    }
}
