package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlayerPo;

/**
 * R-0169 saved state: the own-turn count survives declaration, a rebel count
 * exists only after declaration (zero is the ordinary pre-declaration value),
 * and impossible or negative counts are rejected at load.
 */
class MercenarySaveTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static GameModelPo data(final boolean declared) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        final PlayerPo dutch = dutchPo(data);
        RebellionFixtures.set(data.getModel(), dutch, declared);
        return data;
    }

    private static PlayerPo dutchPo(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    private static Player dutch(final Model model) {
        return model.getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void preDeclarationCountersRoundTrip() {
        final GameModelPo data = data(false);
        dutchPo(data).setMercenaryTurns(9);
        dutchPo(data).setMercenaryRebelUpdates(0);
        final PlayerPo saved = dutch(load(data)).save();
        assertEquals(9, saved.getMercenaryTurns());
        assertEquals(0, saved.getMercenaryRebelUpdates());
    }

    @Test
    void historicalTurnCountPersistsAfterDeclaration() {
        final GameModelPo data = data(true);
        dutchPo(data).setMercenaryTurns(9);
        dutchPo(data).setMercenaryRebelUpdates(3);
        final PlayerPo saved = dutch(load(data)).save();
        assertEquals(9, saved.getMercenaryTurns());
        assertEquals(3, saved.getMercenaryRebelUpdates());
    }

    @Test
    void absentCountersLoadAsNothingCounted() {
        final GameModelPo data = data(false);
        dutchPo(data).setMercenaryTurns(null);
        dutchPo(data).setMercenaryRebelUpdates(null);
        final PlayerPo saved = dutch(load(data)).save();
        assertEquals(0, saved.getMercenaryTurns());
        assertEquals(0, saved.getMercenaryRebelUpdates());
        assertNull(saved.getMercenaryOffer());
    }

    @Test
    void rebelCountBeforeDeclarationIsRejected() {
        final GameModelPo data = data(false);
        dutchPo(data).setMercenaryRebelUpdates(3);
        assertThrows(IllegalArgumentException.class, () -> load(data));
    }

    @Test
    void negativeCountersAreRejected() {
        final GameModelPo turns = data(false);
        dutchPo(turns).setMercenaryTurns(-1);
        assertThrows(IllegalArgumentException.class, () -> load(turns));
        final GameModelPo rebel = data(true);
        dutchPo(rebel).setMercenaryRebelUpdates(-1);
        assertThrows(IllegalArgumentException.class, () -> load(rebel));
    }
}
