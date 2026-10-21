package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;

/**
 * Live Pocahontas lifecycle on a loaded game with real turns: choosing alone
 * changes nothing, the next real Dutch turn clears the Dutch pairs once and
 * then applies the halved pressure, other Europeans are untouched, and a later
 * direct event survives the next turn and every save and load.
 *
 * <p>
 * Pressure of the Dutch colonist (0.25) and soldier (0.25 resident + 2 armed)
 * beside the Aztec city is 2.5. With Pocahontas it is 1.25, so after the
 * arrival the local alarm is 1.25 and the tribal anger 1.25 / 20 = 0.0625.
 * </p>
 */
class NativeRelationsLifecycleTest {

    private static final double EXACT = 1e-9;

    private static ModelPo lifecycleSave() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .unit(DUTCH, UnitType.COLONIST, at(0, 2), SOLDIER)
                .alarm(EuropeanNation.DUTCH, 20)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .alarm(EuropeanNation.FRENCH, 40)
                .tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 30)
                .dutchCongressReadyFor(Founder.POCAHONTAS).toPo();
    }

    /** Ends turns until the Dutch player is current again: one full round. */
    private static void endRound(final Model model, final Player dutch) {
        do {
            model.endTurn();
        } while (model.getCurrentPlayer() != dutch);
    }

    private static void assertPair(final Model model, final String owner,
            final double alarm, final double anger) {
        final Player player = model.getPlayerByName(owner);
        assertEquals(alarm,
                model.getNativeLocalAlarm(player, settlement(model)), EXACT);
        assertEquals(anger,
                model.getNativeTribalAnger(player, NativeNation.AZTEC), EXACT);
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void choosingIsNotArrivalAndArrivalClearsOnceThenLaterChangesAreKept() {
        final Model model = Model.make(lifecycleSave(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = model.getPlayerByName(DUTCH);
        assertPair(model, DUTCH, 20, 10);
        assertPair(model, FRANCE, 40, 30);

        dutch.chooseFounder(dutch.getFounderOffers().stream()
                .filter(offer -> offer.getFounder() == Founder.POCAHONTAS)
                .findFirst().orElseThrow());
        assertPair(model, DUTCH, 20, 10);
        assertPair(model, FRANCE, 40, 30);
        assertFalse(dutch.getCongressMembers().contains(Founder.POCAHONTAS));

        // The arrival clears the Dutch 20 and 10, then the same update adds
        // the halved pressure. France only decays and adds its own mean once:
        // alarm 40 - 1, anger 30 - 1 + 39 / 20.
        endRound(model, dutch);
        assertTrue(dutch.getCongressMembers().contains(Founder.POCAHONTAS));
        assertPair(model, DUTCH, 1.25, 0.0625);
        assertPair(model, FRANCE, 39, 30.95);
        final ModelPo arrived = model.save();
        arrived.getNativeLocalAlarm().stream()
                .filter(entry -> entry.getEuropean() == EuropeanNation.ENGLISH
                        || entry.getEuropean() == EuropeanNation.SPANISH)
                .forEach(entry -> assertEquals(0, entry.getValue(), EXACT));

        // Loading replays neither the clear nor the update.
        final Model loaded = reload(model);
        assertTrue(loaded.getPlayerByName(DUTCH).getCongressMembers()
                .contains(Founder.POCAHONTAS));
        assertPair(loaded, DUTCH, 1.25, 0.0625);
        assertPair(loaded, FRANCE, 39, 30.95);

        // A legitimate later event on top of the non-zero values: the local
        // part is halved by Pocahontas, the tribal part is not.
        final Player loadedDutch = loaded.getPlayerByName(DUTCH);
        loaded.addNativeTribalAnger(NativeNation.AZTEC, loadedDutch, 10);
        loaded.addNativeLocalAlarm(settlement(loaded), loadedDutch, 10);
        assertPair(loaded, DUTCH, 6.25, 10.0625);

        // The next real turn does not clear again: alarm 6.25 - 1 + 1.25 and
        // anger 10.0625 - 1 + 6.5 / 20.
        endRound(loaded, loadedDutch);
        assertPair(loaded, DUTCH, 6.5, 9.3875);

        final Model reloaded = reload(loaded);
        assertTrue(reloaded.getPlayerByName(DUTCH).getCongressMembers()
                .contains(Founder.POCAHONTAS));
        assertPair(reloaded, DUTCH, 6.5, 9.3875);
    }

}
