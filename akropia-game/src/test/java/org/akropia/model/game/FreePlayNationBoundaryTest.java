package org.akropia.model.game;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.game.po.GameModelPoDao;

import com.google.common.eventbus.EventBus;

class FreePlayNationBoundaryTest {

    @Test
    void authoredWorldRejectsOtherNationsAndMissingInputs() {
        final GameModelPoDao files = new GameModelPoDao();
        final GameModelDao games = new GameModelDao(files);
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
