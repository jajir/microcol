package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.microcol.model.EuropeanNation;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Player;

/**
 * Native relations of a colonial European after real turns and Congress
 * choices. Values are read from the model; no action here changes them.
 */
public final class NativeRelationsPage extends AbstractScreen {

    private List<Double> remembered = List.of();

    public NativeRelationsPage(TestContext context) {
        super(context);
    }

    private Player colonial(EuropeanNation european) {
        return getModel().getPlayerByName(european.getPlayerName());
    }

    /** Settlement the player has learned by sight at the given square. */
    private NativeSettlement settlementAt(EuropeanNation european,
            Location location) {
        return getModel()
                .getNativeSettlementKnownTo(colonial(european), location)
                .orElseThrow();
    }

    public void rememberNativeValues(EuropeanNation european,
            NativeNation tribe, Location location) {
        remembered = List.of(
                getModel().getNativeTribalAnger(colonial(european), tribe),
                getModel().getNativeLocalAlarm(colonial(european),
                        settlementAt(european, location)));
    }

    public void verifyNativeValuesUnchangedSinceRemembered(
            EuropeanNation european, NativeNation tribe, Location location) {
        assertEquals(remembered,
                List.of(getModel().getNativeTribalAnger(colonial(european),
                        tribe),
                        getModel().getNativeLocalAlarm(colonial(european),
                                settlementAt(european, location))));
    }

    public void verifyTribalAnger(EuropeanNation european, NativeNation tribe,
            double expected) {
        assertEquals(expected,
                getModel().getNativeTribalAnger(colonial(european), tribe),
                1e-9);
    }

    public void verifyLocalAlarm(EuropeanNation european, Location location,
            double expected) {
        assertEquals(expected, getModel().getNativeLocalAlarm(
                colonial(european), settlementAt(european, location)), 1e-9);
    }

    /**
     * Relations are readable only for a settlement the player has learned by
     * sight, so a hidden one must not be known at all.
     */
    public void verifyRelationsHidden(EuropeanNation european,
            Location location) {
        assertTrue(getModel()
                .getNativeSettlementKnownTo(colonial(european), location)
                .isEmpty());
    }

    public void verifyHostility(EuropeanNation european, Location location,
            NativeHostility expected) {
        assertEquals(expected, getModel().getNativeHostility(colonial(european),
                settlementAt(european, location)));
    }

    public void verifyFounderMember(EuropeanNation european, Founder founder,
            boolean expected) {
        assertEquals(expected,
                colonial(european).getCongressMembers().contains(founder));
    }

    public void verifySelectedFounder(EuropeanNation european,
            Founder founder) {
        assertEquals(Optional.of(founder),
                colonial(european).getSelectedFounder());
    }

    public void verifyNoSelectedFounder(EuropeanNation european) {
        assertEquals(Optional.empty(), colonial(european).getSelectedFounder());
    }
}
