package org.microcol.gui.screen.turnreport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventColonyWasLost;
import org.microcol.model.turnevent.TurnEventShipComeToEuropePort;
import org.microcol.model.turnevent.TurnEventShipComeToHighSeas;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ShowTurnEvensOnTurnStartedEventTest {

    private final Player human = mock(Player.class);

    private final Model model = mock(Model.class);

    private final GameModelController gameModelController = mock(
            GameModelController.class);

    private final GamePreferences gamePreferences = mock(GamePreferences.class);

    private final EuropeOpeningRequest europeOpeningRequest = new EuropeOpeningRequest();

    private final EventBus eventBus = new EventBus();

    private final List<Screen> shownScreens = new ArrayList<>();

    @BeforeEach
    public void setup() {
        when(gameModelController.getModel()).thenReturn(model);
        when(gameModelController.getHumanPlayer()).thenReturn(human);
        eventBus.register(new ShowTurnEvensOnTurnStartedEvent(eventBus,
                gameModelController, gamePreferences, europeOpeningRequest));
        eventBus.register(this);
    }

    @Subscribe
    public void recordShownScreen(final ShowScreenEvent event) {
        shownScreens.add(event.getScreen());
    }

    @Test
    public void verify_routine_notice_opens_report_when_summary_is_on() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        givenSavedNotices(routineNotice());

        startTurn(true);

        verifyReportOpened(true);
    }

    @Test
    public void verify_routine_notice_stays_closed_when_summary_is_off() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(false);
        givenSavedNotices(routineNotice());

        startTurn(true);

        verifyReportOpened(false);
    }

    @Test
    public void verify_critical_notice_opens_report_when_summary_is_off() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(false);
        givenSavedNotices(mock(TurnEventColonyWasLost.class));

        startTurn(true);

        verifyReportOpened(true);
    }

    @Test
    public void verify_empty_notices_never_open_report() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        givenSavedNotices();

        startTurn(true);

        verifyReportOpened(false);
    }

    @Test
    public void verify_continuing_turn_does_not_reopen_report() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        givenSavedNotices(routineNotice());

        startTurn(false);

        verifyReportOpened(false);
    }

    @Test
    public void verify_other_player_turn_does_not_open_report() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        givenSavedNotices(routineNotice());

        eventBus.post(new TurnStartedEvent(model, mock(Player.class), true));

        verifyReportOpened(false);
    }

    @Test
    public void verify_arrival_waits_behind_report_when_auto_europe_is_on() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        when(gamePreferences.isOpenEuropeOnArrival()).thenReturn(true);
        givenSavedNotices(arrivalNotice());

        startTurn(true);

        verifyReportOpened(true);
        verifyEuropeOpened(false);
        assertEquals(Screen.EUROPE,
                europeOpeningRequest.takeScreenAfterReport());
    }

    @Test
    public void verify_arrival_opens_europe_directly_when_no_report_is_due() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(false);
        when(gamePreferences.isOpenEuropeOnArrival()).thenReturn(true);
        givenSavedNotices(arrivalNotice());

        startTurn(true);

        verifyReportOpened(false);
        verifyEuropeOpened(true);
    }

    @Test
    public void verify_arrival_keeps_report_when_auto_europe_is_off() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        when(gamePreferences.isOpenEuropeOnArrival()).thenReturn(false);
        givenSavedNotices(arrivalNotice());

        startTurn(true);

        verifyReportOpened(true);
        verifyEuropeOpened(false);
        assertEquals(Screen.GAME, europeOpeningRequest.takeScreenAfterReport());
    }

    @Test
    public void verify_critical_notice_is_shown_before_auto_europe() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(false);
        when(gamePreferences.isOpenEuropeOnArrival()).thenReturn(true);
        givenSavedNotices(mock(TurnEventColonyWasLost.class), arrivalNotice());

        startTurn(true);

        verifyReportOpened(true);
        verifyEuropeOpened(false);
        assertEquals(Screen.EUROPE,
                europeOpeningRequest.takeScreenAfterReport());
    }

    @Test
    public void verify_continuing_turn_clears_pending_europe_request() {
        when(gamePreferences.isShowTurnSummary()).thenReturn(true);
        when(gamePreferences.isOpenEuropeOnArrival()).thenReturn(true);
        givenSavedNotices(arrivalNotice());
        startTurn(true);

        startTurn(false);

        assertEquals(Screen.GAME, europeOpeningRequest.takeScreenAfterReport());
    }

    private TurnEvent routineNotice() {
        return mock(TurnEventShipComeToHighSeas.class);
    }

    private TurnEvent arrivalNotice() {
        return mock(TurnEventShipComeToEuropePort.class);
    }

    private void givenSavedNotices(final TurnEvent... notices) {
        when(model.isTurnEventsMessagesEmpty(human))
                .thenReturn(notices.length == 0);
        when(model.getTurnEventsLocalizedMessages(human))
                .thenReturn(List.of(notices));
    }

    private void startTurn(final boolean isFreshStart) {
        eventBus.post(new TurnStartedEvent(model, human, isFreshStart));
    }

    private void verifyReportOpened(final boolean expected) {
        assertEquals(expected, shownScreens.contains(Screen.TURN_REPORT));
    }

    private void verifyEuropeOpened(final boolean expected) {
        assertEquals(expected, shownScreens.contains(Screen.EUROPE));
    }

}
