package org.microcol.gui.buttonpanel;

import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.mainmenu.AboutGameEvent;
import org.microcol.gui.mainmenu.CenterViewEvent;
import org.microcol.gui.mainmenu.ExitGameEvent;
import org.microcol.gui.mainmenu.ShowGoalsEvent;
import org.microcol.gui.mainmenu.ShowStatisticsEvent;
import org.microcol.gui.mainmenu.ShowTurnReportEvent;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class ButtonsPanel extends AbstractButtonsPanel {

    private final Button buttonCenter;
    private final Button buttonHelp;
    private final Button buttonStatistics;
    private final Button buttonExit;

    private final Button buttonGoals;
    private final Button buttonTurnReport;
    private final Button buttonNextTurn;
    private final Button buttonEurope;

    @Inject
    ButtonsPanel(final ImageProvider imageProvider, final EventBus eventBus, final I18n i18n) {
        super(imageProvider, eventBus, Source.GAME, i18n);

        buttonCenter = makeButon(ImageLoaderButtons.BUTTON_CENTER, Buttons.buttonCenter);
        buttonCenter.setOnAction(event -> eventBus.post(new CenterViewEvent()));
        buttonHelp = makeButon(ImageLoaderButtons.BUTTON_HELP, Buttons.buttonHelp);
        buttonHelp.setOnAction(event -> eventBus.post(new AboutGameEvent()));
        buttonStatistics = makeButon(ImageLoaderButtons.BUTTON_STATISTICS,
                Buttons.buttonStatistics);
        buttonStatistics.setOnAction(event -> eventBus.post(new ShowStatisticsEvent()));
        buttonExit = makeButon(ImageLoaderButtons.BUTTON_EXIT, Buttons.buttonExit);
        buttonExit.setOnAction(event -> eventBus.post(new ExitGameEvent()));
        buttonGoals = makeButon(ImageLoaderButtons.BUTTON_GOALS, Buttons.buttonGoals);
        buttonGoals.setOnAction(event -> eventBus.post(new ShowGoalsEvent()));
        buttonTurnReport = makeButon(ImageLoaderButtons.BUTTON_TURN_REPORT,
                Buttons.buttonTurnReport);
        buttonTurnReport.setOnAction(event -> eventBus.post(new ShowTurnReportEvent()));
        buttonEurope = makeButon(ImageLoaderButtons.BUTTON_EUROPE, Buttons.buttonEurope);
        buttonEurope.setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.EUROPE)));
        buttonNextTurn = makeButon(ImageLoaderButtons.BUTTON_NEXT_TURN, Buttons.buttonNextTurn);
        buttonNextTurn.setOnAction(event -> {
            buttonNextTurn.setDisable(true);
            eventBus.post(new NextTurnEvent());
        });

        getButtonPanel().getChildren().add(buttonStatistics);
        getButtonPanel().getChildren().add(buttonCenter);
        getButtonPanel().getChildren().add(buttonTurnReport);
        getButtonPanel().getChildren().add(buttonGoals);
        getButtonPanel().getChildren().add(buttonHelp);
        getButtonPanel().getChildren().add(buttonEurope);
        getButtonPanel().getChildren().add(buttonExit);
        getButtonPanel().getChildren().add(buttonNextTurn);
    }

    /**
     * @return the buttonCenter
     */
    public Button getButtonCenter() {
        return buttonCenter;
    }

    /**
     * @return the buttonHelp
     */
    public Button getButtonHelp() {
        return buttonHelp;
    }

    /**
     * @return the buttonStatistics
     */
    public Button getButtonStatistics() {
        return buttonStatistics;
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonExit() {
        return buttonExit;
    }

    /**
     * @return the buttonGoals
     */
    public Button getButtonGoals() {
        return buttonGoals;
    }

    /**
     * @return the buttonTurnReport
     */
    public Button getButtonTurnReport() {
        return buttonTurnReport;
    }

    /**
     * @return the buttonNextTurn
     */
    public Button getButtonNextTurn() {
        return buttonNextTurn;
    }

    /**
     * @return the buttonEurope
     */
    public Button getButtonEurope() {
        return buttonEurope;
    }

}
