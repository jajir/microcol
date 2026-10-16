package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.store.CampaignsDao;

import com.google.common.eventbus.EventBus;

class FreePlayNationBoundaryTest {

    @Test
    void authoredWorldRejectsOtherNationsAndMissingInputs() {
        final GameModelPoDao files = new GameModelPoDao();
        final GameModelDao games = new GameModelDao(files,
                new CampaignManager(List.of(), mock(CampaignsDao.class)),
                mock(MissionFactoryManager.class));
        for (final FreePlayMap map : new FreePlayMap[] { FreePlayMap.ORIGINAL,
                FreePlayMap.TWO_ISLANDS, FreePlayMap.RIVAL_EXPEDITION }) {
            for (final EuropeanNation nation : EuropeanNation.values()) {
                if (nation != EuropeanNation.DUTCH) {
                    assertThrows(IllegalArgumentException.class,
                            () -> games.createFreePlay(map, "Valid name",
                                    Difficulty.CONQUISTADOR, nation,
                                    mock(EventBus.class)));
                }
            }
        }
        assertThrows(NullPointerException.class,
                () -> games.createFreePlay(null, "Valid name",
                        Difficulty.CONQUISTADOR, EuropeanNation.DUTCH,
                        mock(EventBus.class)));
        assertThrows(NullPointerException.class,
                () -> games.createFreePlay(FreePlayMap.REFERENCE, "Valid name",
                        Difficulty.CONQUISTADOR, null, mock(EventBus.class)));
        assertThrows(NullPointerException.class,
                () -> games.createFreePlay(FreePlayMap.REFERENCE, "Valid name",
                        null, EuropeanNation.DUTCH, mock(EventBus.class)));
        assertThrows(NullPointerException.class,
                () -> games.createFreePlay(FreePlayMap.REFERENCE, "Valid name",
                        Difficulty.CONQUISTADOR, EuropeanNation.DUTCH, null));
    }
}
