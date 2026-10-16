package org.microcol.model.event;

import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.NavalCombatOutcome;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitAttackedEvent extends AbstractModelEvent {
    private final Unit attacker;
    private final Unit defender;
    private final Unit destroyed;
    private final Unit defeated;
    private final NavalCombatOutcome navalOutcome;

    public UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed) {
        this(model, attacker, defender, defeated, wasDestroyed, null);
    }

    public UnitAttackedEvent(final Model model, final Unit attacker,
            final Unit defender, final Unit defeated,
            final boolean wasDestroyed, final NavalCombatOutcome navalOutcome) {
        super(model);
        this.navalOutcome = navalOutcome;
        this.attacker = Preconditions.checkNotNull(attacker);
        this.defender = Preconditions.checkNotNull(defender);
        this.defeated = Preconditions.checkNotNull(defeated);
        this.destroyed = wasDestroyed ? defeated : null;
    }

    public Optional<NavalCombatOutcome> getNavalOutcome() {
        return Optional.ofNullable(navalOutcome);
    }

    public Optional<Location> getOriginalAttackerLocation() {
        return getNavalOutcome().map(NavalCombatOutcome::getAttackerLocation);
    }

    public Optional<Location> getOriginalDefenderLocation() {
        return getNavalOutcome().map(NavalCombatOutcome::getDefenderLocation);
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
