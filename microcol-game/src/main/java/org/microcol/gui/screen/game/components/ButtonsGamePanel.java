package org.microcol.gui.screen.game.components;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.buttonpanel.NextTurnEvent;
import org.microcol.gui.event.ShowHelpEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.DeclareIndependenceEvent;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;

@Singleton
public class ButtonsGamePanel extends AbstractButtonsPanel {

    private final static Logger LOGGER = LoggerFactory.getLogger(ButtonsGamePanel.class);

    public final static String BUTTON_NEXT_TURN_ID = "nextTurn";

    public final static String BUTTON_HELP_ID = "help";

    public final static String BUTTON_CENTER_ID = "center";

    public final static String BUTTON_STATISTICS_ID = "statistics";

    public final static String BUTTON_EXIT_ID = "exit";

    public final static String BUTTON_GOALS_ID = "goals";

    public final static String BUTTON_TURN_REPORT_ID = "turnReport";

    public final static String BUTTON_EUROPE_ID = "europe";

    public final static String BUTTON_MOVE_ID = "move";

    public final static String BUTTON_PLOW_FIELD_ID = "plowField";

    public final static String BUTTON_BUILD_COLONY_ID = "buildColony";

    public final static String BUTTON_DECLARE_INDEPENDENCE_ID = "declareIndependence";

    private final Button buttonCenter;
    private final Button buttonHelp;
    private final Button buttonStatistics;
    private final Button buttonExit;

    private final Button buttonGoals;
    private final Button buttonTurnReport;
    private final Button buttonNextTurn;
    private final Button buttonEurope;

    /**
     * Unit related buttons
     */
    private final Button buttonMove;
    private final Button buttonPlowField;
    private final Button buttonBuildColony;
    private final Button buttonDeclareIndependence;

    @Inject
    ButtonsGamePanel(final ImageProvider imageProvider, final EventBus eventBus, final I18n i18n) {
        super(imageProvider, eventBus, Source.GAME, i18n);

        buttonCenter = makeButon(ImageLoaderButtons.BUTTON_CENTER, Buttons.buttonCenter);
        buttonCenter.setOnAction(event -> eventBus.post(new CenterViewEvent()));
        buttonCenter.setId(BUTTON_CENTER_ID);
        buttonHelp = makeButon(ImageLoaderButtons.BUTTON_HELP, Buttons.buttonHelp);
        buttonHelp.setOnAction(event -> eventBus.post(new ShowHelpEvent()));
        buttonHelp.setId(BUTTON_HELP_ID);
        buttonStatistics = makeButon(ImageLoaderButtons.BUTTON_STATISTICS,
                Buttons.buttonStatistics);
        buttonStatistics
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.STATISTICS)));
        buttonStatistics.setId(BUTTON_STATISTICS_ID);
        buttonExit = makeButon(ImageLoaderButtons.BUTTON_EXIT, Buttons.buttonExit);
        buttonExit.setOnAction(event -> eventBus.post(new ExitGameEvent()));
        buttonExit.setId(BUTTON_EXIT_ID);
        buttonGoals = makeButon(ImageLoaderButtons.BUTTON_GOALS, Buttons.buttonGoals);
        buttonGoals.setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.GOALS)));
        buttonGoals.setId(BUTTON_GOALS_ID);
        buttonTurnReport = makeButon(ImageLoaderButtons.BUTTON_TURN_REPORT,
                Buttons.buttonTurnReport);
        buttonTurnReport
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT)));
        buttonTurnReport.setId(BUTTON_TURN_REPORT_ID);
        buttonEurope = makeButon(ImageLoaderButtons.BUTTON_EUROPE, Buttons.buttonEurope);
        buttonEurope.setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.EUROPE)));
        buttonEurope.setId(BUTTON_EUROPE_ID);
        buttonNextTurn = makeButon(ImageLoaderButtons.BUTTON_NEXT_TURN, Buttons.buttonNextTurn);
        buttonNextTurn.setId(BUTTON_NEXT_TURN_ID);
        buttonNextTurn.setOnAction(event -> {
            buttonNextTurn.setDisable(true);
            eventBus.post(new NextTurnEvent());
        });
        buttonMove = makeButon(ImageLoaderButtons.BUTTON_MOVE, Buttons.buttonMove);
        buttonMove.setOnAction(event -> eventBus.post(new StartMoveEvent()));
        buttonMove.setId(BUTTON_MOVE_ID);
        buttonPlowField = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD, Buttons.buttonPlowField);
        buttonPlowField.setOnAction(event -> eventBus.post(new PlowFieldEvent()));
        buttonPlowField.setId(BUTTON_PLOW_FIELD_ID);
        buttonBuildColony = makeButon(ImageLoaderButtons.BUTTON_BUILD_COLONY,
                Buttons.buttonBuildColony);
        buttonBuildColony.setOnAction(event -> eventBus.post(new BuildColonyEvent()));
        buttonBuildColony.setId(BUTTON_BUILD_COLONY_ID);
        buttonDeclareIndependence = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonDeclareIndependence);
        buttonDeclareIndependence
                .setOnAction(event -> eventBus.post(new DeclareIndependenceEvent()));
        buttonDeclareIndependence.setId(BUTTON_DECLARE_INDEPENDENCE_ID);

        getButtonPanel().getChildren().add(buttonBuildColony);
        getButtonPanel().getChildren().add(buttonPlowField);
        getButtonPanel().getChildren().add(buttonMove);
        getButtonPanel().getChildren().add(buttonDeclareIndependence);
        getButtonPanel().getChildren().add(buttonCenter);
        getButtonPanel().getChildren().add(buttonStatistics);
        getButtonPanel().getChildren().add(buttonTurnReport);
        getButtonPanel().getChildren().add(buttonGoals);
        // getButtonPanel().getChildren().add(buttonHelp);
        getButtonPanel().getChildren().add(buttonEurope);
        getButtonPanel().getChildren().add(buttonExit);
        getButtonPanel().getChildren().add(buttonNextTurn);
    }

    public void disableAllButtons() {
        LOGGER.debug("Disabling buttons bar");
        setDisable(true);
    }

    public void enableAllButtons() {
        LOGGER.debug("Enabling buttons bar");
        setDisable(false);
    }

    private void setDisable(final boolean disable) {
        buttonCenter.setDisable(disable);
        buttonHelp.setDisable(disable);
        buttonStatistics.setDisable(disable);
        buttonExit.setDisable(disable);

        buttonGoals.setDisable(disable);
        buttonTurnReport.setDisable(disable);
        buttonNextTurn.setDisable(disable);
        buttonEurope.setDisable(disable);

        buttonMove.setDisable(disable);
        buttonPlowField.setDisable(disable);
        buttonBuildColony.setDisable(disable);
        buttonDeclareIndependence.setDisable(disable);
    }

    public void setVisibleButtonMove(final boolean isVisible) {
        setVisibleNode(buttonMove, isVisible);
    }

    public void setVisibleButtonPlowField(final boolean isVisible) {
        setVisibleNode(buttonPlowField, isVisible);
    }

    public void setVisibleButtonBuildColony(final boolean isVisible) {
        setVisibleNode(buttonBuildColony, isVisible);
    }

    public void setVisibleButtonDeclareIndependence(final boolean isVisible) {
        setVisibleNode(buttonDeclareIndependence, isVisible);
    }

    protected void setVisibleNode(final Node node, final boolean isVisible) {
        Platform.runLater(() -> {
            if (isContaining(node)) {
                if (!isVisible) {
                    remove(node);
                }
            } else {
                if (isVisible) {
                    getButtonPanel().getChildren().add(0, node);
                }
            }
        });
    }

    /**
     * @return the buttonNextTurn
     */
    public Button getButtonNextTurn() {
        return buttonNextTurn;
    }

}
