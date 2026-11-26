package org.akropia.model.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.akropia.model.GameOverEvaluator;
import org.akropia.model.GameOverResult;
import org.akropia.model.Model;
import org.akropia.model.event.GameFinishedEvent;
import org.mockito.ArgumentCaptor;

import com.google.common.eventbus.EventBus;

class GameEventsTest {

    private final EventBus eventBus = mock(EventBus.class);

    @Test
    void timeLimitEmitsTimeLimitOutcome() {
        final String result = GameOverProcessors.TIME_IS_UP_PROCESSOR
                .apply(gameOver(
                        org.akropia.model.GameOverEvaluator.REASON_TIME_IS_UP));
        assertEquals("ok", result);
        final ArgumentCaptor<EventGameFinished> event = ArgumentCaptor
                .forClass(EventGameFinished.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(GameMessages.gameOver_timeIsUp),
                event.getValue().getMessages());
        assertFalse(event.getValue().isVictory());
    }

    @Test
    void lostColoniesEmitLostColoniesOutcome() {
        final String result = GameOverProcessors.NO_COLONIES_PROCESSOR
                .apply(gameOver(
                        org.akropia.model.GameOverEvaluator.REASON_NO_COLONIES));
        assertEquals("ok", result);
        final ArgumentCaptor<EventGameFinished> event = ArgumentCaptor
                .forClass(EventGameFinished.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(GameMessages.gameOver_allColoniesAreLost),
                event.getValue().getMessages());
    }

    @Test
    void unrelatedReasonEmitsNoOutcome() {
        assertNull(GameOverProcessors.TIME_IS_UP_PROCESSOR
                .apply(gameOver("other")));
        assertNull(GameOverProcessors.NO_COLONIES_PROCESSOR
                .apply(gameOver("other")));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void eachRebellionLossReasonEmitsItsOwnMessage() {
        // The five losses of R-0192 and R-0193, each with its own message that
        // names why the rebellion failed.
        final Map<String, GameMessages> expected = new LinkedHashMap<>();
        expected.put(GameOverEvaluator.REASON_REBELLION_NO_COLONIES,
                GameMessages.gameOver_rebellionNoColonies);
        expected.put(GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN,
                GameMessages.gameOver_rebellionAllColoniesCrown);
        expected.put(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN,
                GameMessages.gameOver_rebellionCoastalCrown);
        expected.put(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                GameMessages.gameOver_rebellionResidentsCrown);
        expected.put(GameOverEvaluator.REASON_REBELLION_UNRESOLVED,
                GameMessages.gameOver_rebellionUnresolved);
        assertEquals(5, expected.values().stream().distinct().count());
        for (final Map.Entry<String, GameMessages> loss : expected.entrySet()) {
            reset(eventBus);
            assertEquals("ok", GameOverProcessors.REBELLION_LOST_PROCESSOR
                    .apply(gameOver(loss.getKey())));
            final ArgumentCaptor<EventGameFinished> event = ArgumentCaptor
                    .forClass(EventGameFinished.class);
            verify(eventBus).post(event.capture());
            assertEquals(List.of(loss.getValue()),
                    event.getValue().getMessages(), loss.getKey());
            // The victory processor never answers a loss.
            assertNull(GameOverProcessors.REBELLION_WON_PROCESSOR
                    .apply(gameOver(loss.getKey())));
        }
    }

    @Test
    void victoryAndTheOtherFreePlayEndsEmitTheirOwnMessage() {
        final String victory = GameOverEvaluator.REASON_REBELLION_VICTORY;
        assertEquals("ok", GameOverProcessors.REBELLION_WON_PROCESSOR
                .apply(gameOver(victory)));
        // The loss processor never answers a victory.
        assertNull(GameOverProcessors.REBELLION_LOST_PROCESSOR
                .apply(gameOver(victory)));
        assertEquals("ok", GameOverProcessors.FREE_PLAY_ENDED_PROCESSOR
                .apply(gameOver(GameOverEvaluator.REASON_FREE_PLAY_ENDED)));
        assertEquals("ok", GameOverProcessors.RETIRED_PROCESSOR
                .apply(gameOver(GameOverEvaluator.REASON_RETIRED)));
        final ArgumentCaptor<EventGameFinished> ends = ArgumentCaptor
                .forClass(EventGameFinished.class);
        verify(eventBus, times(3)).post(ends.capture());
        assertEquals(List.of(GameMessages.gameOver_rebellionVictory),
                ends.getAllValues().get(0).getMessages());
        assertEquals(List.of(GameMessages.gameOver_freePlayEnded),
                ends.getAllValues().get(1).getMessages());
        assertEquals(List.of(GameMessages.gameOver_retired),
                ends.getAllValues().get(2).getMessages());
        // Only the won rebellion is a success (R-0228).
        assertTrue(ends.getAllValues().get(0).isVictory());
        assertFalse(ends.getAllValues().get(1).isVictory());
        assertFalse(ends.getAllValues().get(2).isVictory());
    }

    @Test
    void everyUnsuccessfulEndIsNoVictory() {
        // Each game end that is not a success states so
        // explicitly; its messages are not inspected (R-0228).
        final List<String> reasons = List.of(
                GameOverEvaluator.REASON_TIME_IS_UP,
                GameOverEvaluator.REASON_FREE_PLAY_ENDED,
                GameOverEvaluator.REASON_RETIRED,
                GameOverEvaluator.REASON_NO_COLONIES,
                GameOverEvaluator.REASON_REBELLION_NO_COLONIES,
                GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN,
                GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN,
                GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                GameOverEvaluator.REASON_REBELLION_UNRESOLVED);
        final GameEvents freePlay = new GameEvents(eventBus);
        for (final String reason : reasons) {
            assertFalse(finish(freePlay, reason).isVictory(), reason);
        }
        assertTrue(finish(freePlay, GameOverEvaluator.REASON_REBELLION_VICTORY)
                .isVictory());
    }

    /**
     * Runs the mission's end processors in their order, as the game over
     * evaluator does, and returns the one end it posts.
     */
    private EventGameFinished finish(final GameEvents game,
            final String reason) {
        reset(eventBus);
        game.onGameFinished(new GameFinishedEvent(mock(Model.class),
                new GameOverResult(reason)));
        final ArgumentCaptor<EventGameFinished> event = ArgumentCaptor
                .forClass(EventGameFinished.class);
        verify(eventBus).post(event.capture());
        return event.getValue();
    }

    @Test
    void freePlayIndependenceEmitsItsOutcome() {
        final GameEvents mission = new GameEvents(eventBus);
        mission.onIndependenceWasDeclared(null);
        final ArgumentCaptor<EventShowMessages> event = ArgumentCaptor
                .forClass(EventShowMessages.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(GameMessages.independenceWasDeclared_caption),
                event.getValue().getMessages());
    }

    private GameOverProcessingContext gameOver(final String reason) {
        return new GameOverProcessingContext(new GameFinishedEvent(
                mock(Model.class), new GameOverResult(reason)), eventBus);
    }

}
