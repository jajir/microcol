package org.microcol.gui.screen.game.components;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.screen.congress.CongressDialog;
import org.microcol.gui.screen.congress.CongressText;
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
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.gui.event.SelectNextUnitEvent;
import org.microcol.gui.event.SkipUnitTurnEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;

@Singleton
public class ButtonsGamePanel extends AbstractButtonsPanel
        implements UpdatableLanguage {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ButtonsGamePanel.class);

    public final static String BUTTON_NEXT_TURN_ID = "nextTurn";

    private final static String BUTTON_HELP_ID = "help";

    private final static String BUTTON_CENTER_ID = "center";

    private final static String BUTTON_STATISTICS_ID = "statistics";

    private final static String BUTTON_EXIT_ID = "exit";

    private final static String BUTTON_GOALS_ID = "goals";

    public final static String BUTTON_TURN_REPORT_ID = "turnReport";

    public final static String BUTTON_EUROPE_ID = "europe";

    private final static String BUTTON_MOVE_ID = "move";

    private final static String BUTTON_PLOW_FIELD_ID = "plowField";

    private final static String BUTTON_BUILD_COLONY_ID = "buildColony";

    public final static String BUTTON_DECLARE_INDEPENDENCE_ID = "declareIndependence";

    private final Button buttonSentry = new Button();
    private final Tooltip sentryTooltip = new Tooltip();
    private boolean sentryActive;
    private final Button buttonFortify = new Button();
    private final Tooltip fortifyTooltip = new Tooltip();
    private boolean fortifying;
    private boolean fortified;
    private I18n currentI18n;

    private final Button buttonCongress = new Button();

    private final Button buttonWait = new Button();
    private final Button buttonSkipUnitTurn = new Button();
    private final Tooltip waitTooltip = new Tooltip();
    private final Tooltip skipTooltip = new Tooltip();

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
    ButtonsGamePanel(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n, final ColoniesMenu coloniesMenu,
            final CongressDialog congressDialog) {
        super(imageProvider, eventBus, Source.GAME, i18n);

        buttonCongress.setId("congress");
        buttonCongress.setText(i18n.get(CongressText.title));
        buttonCongress.setOnAction(event -> congressDialog.showCongress());
        buttonFortify.setId("fortifyUnit");
        buttonFortify.setTooltip(fortifyTooltip);
        buttonFortify.setDisable(true);
        buttonSentry.setId("sentryUnit");
        buttonSentry.setTooltip(sentryTooltip);
        buttonSentry.setDisable(true);
        buttonWait.setId("waitUnit");
        buttonWait.setTooltip(waitTooltip);
        buttonWait
                .setOnAction(event -> eventBus.post(new SelectNextUnitEvent()));
        buttonSkipUnitTurn.setId("skipUnitTurn");
        buttonSkipUnitTurn.setTooltip(skipTooltip);
        buttonSkipUnitTurn
                .setOnAction(event -> eventBus.post(new SkipUnitTurnEvent()));
        setUnitOrdersEnabled(false, false);
        updateLanguage(i18n);

        buttonCenter = makeButon(ImageLoaderButtons.BUTTON_CENTER,
                Buttons.buttonCenter);
        buttonCenter.setOnAction(event -> eventBus.post(new CenterViewEvent()));
        buttonCenter.setId(BUTTON_CENTER_ID);
        buttonHelp = makeButon(ImageLoaderButtons.BUTTON_HELP,
                Buttons.buttonHelp);
        buttonHelp.setOnAction(event -> eventBus.post(new ShowHelpEvent()));
        buttonHelp.setId(BUTTON_HELP_ID);
        buttonStatistics = makeButon(ImageLoaderButtons.BUTTON_STATISTICS,
                Buttons.buttonStatistics);
        buttonStatistics.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.STATISTICS)));
        buttonStatistics.setId(BUTTON_STATISTICS_ID);
        buttonExit = makeButon(ImageLoaderButtons.BUTTON_EXIT,
                Buttons.buttonExit);
        buttonExit.setOnAction(event -> eventBus.post(new ExitGameEvent()));
        buttonExit.setId(BUTTON_EXIT_ID);
        buttonGoals = makeButon(ImageLoaderButtons.BUTTON_GOALS,
                Buttons.buttonGoals);
        buttonGoals.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.GOALS)));
        buttonGoals.setId(BUTTON_GOALS_ID);
        buttonTurnReport = makeButon(ImageLoaderButtons.BUTTON_TURN_REPORT,
                Buttons.buttonTurnReport);
        buttonTurnReport.setOnAction(event -> eventBus
                .post(new ShowScreenEvent(Screen.TURN_REPORT)));
        buttonTurnReport.setId(BUTTON_TURN_REPORT_ID);
        buttonEurope = makeButon(ImageLoaderButtons.BUTTON_EUROPE,
                Buttons.buttonEurope);
        buttonEurope.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.EUROPE)));
        buttonEurope.setId(BUTTON_EUROPE_ID);
        buttonNextTurn = makeButon(ImageLoaderButtons.BUTTON_NEXT_TURN,
                Buttons.buttonNextTurn);
        buttonNextTurn.setId(BUTTON_NEXT_TURN_ID);
        buttonNextTurn.setOnAction(event -> {
            buttonNextTurn.setDisable(true);
            eventBus.post(new NextTurnEvent());
        });
        buttonMove = makeButon(ImageLoaderButtons.BUTTON_MOVE,
                Buttons.buttonMove);
        buttonMove.setOnAction(event -> eventBus.post(new StartMoveEvent()));
        buttonMove.setId(BUTTON_MOVE_ID);
        buttonPlowField = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonPlowField);
        buttonPlowField
                .setOnAction(event -> eventBus.post(new PlowFieldEvent()));
        buttonPlowField.setId(BUTTON_PLOW_FIELD_ID);
        buttonBuildColony = makeButon(ImageLoaderButtons.BUTTON_BUILD_COLONY,
                Buttons.buttonBuildColony);
        buttonBuildColony
                .setOnAction(event -> eventBus.post(new BuildColonyEvent()));
        buttonBuildColony.setId(BUTTON_BUILD_COLONY_ID);
        buttonDeclareIndependence = makeButon(
                ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonDeclareIndependence);
        buttonDeclareIndependence.setOnAction(
                event -> eventBus.post(new DeclareIndependenceEvent()));
        buttonDeclareIndependence.setId(BUTTON_DECLARE_INDEPENDENCE_ID);

        getButtonPanel().getChildren().add(buttonBuildColony);
        getButtonPanel().getChildren().add(buttonPlowField);
        getButtonPanel().getChildren().add(buttonMove);
        getButtonPanel().getChildren().add(buttonWait);
        getButtonPanel().getChildren().add(buttonSkipUnitTurn);
        getButtonPanel().getChildren().add(buttonSentry);
        getButtonPanel().getChildren().add(buttonFortify);
        getButtonPanel().getChildren().add(buttonDeclareIndependence);
        getButtonPanel().getChildren().add(coloniesMenu.getContent());
        getButtonPanel().getChildren().add(buttonCongress);
        getButtonPanel().getChildren().add(buttonCenter);
        getButtonPanel().getChildren().add(buttonStatistics);
        getButtonPanel().getChildren().add(buttonTurnReport);
        getButtonPanel().getChildren().add(buttonGoals);
        // getButtonPanel().getChildren().add(buttonHelp);
        getButtonPanel().getChildren().add(buttonEurope);
        getButtonPanel().getChildren().add(buttonNextTurn);
        getButtonPanel().getChildren().add(buttonExit);
    }

    void disableAllButtons() {
        LOGGER.debug("Disabling buttons bar");
        setDisable(true);
    }

    void disableTurnCommands() {
        disableAllButtons();
        // Recovery must remain reachable if turn preparation fails.
        buttonExit.setDisable(false);
    }

    void enableAllButtons() {
        LOGGER.debug("Enabling buttons bar");
        setDisable(false);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        currentI18n = i18n;
        buttonCongress.setText(i18n.get(CongressText.title));
        updateSentryText();
        updateFortifyText();
        buttonWait.setText(i18n.get(Buttons.buttonWait));
        buttonSkipUnitTurn.setText(i18n.get(Buttons.buttonSkipUnitTurn));
        waitTooltip.setText(i18n.get(Buttons.waitExplanation));
        skipTooltip.setText(i18n.get(Buttons.skipUnitTurnExplanation));
    }

    void setUnitOrdersEnabled(final boolean wait, final boolean skip) {
        buttonWait.setDisable(!wait);
        buttonSkipUnitTurn.setDisable(!skip);
    }

    void setSentryAction(final Runnable action) {
        buttonSentry.setOnAction(event -> action.run());
    }

    void setSentryOrder(final boolean active, final boolean enabled) {
        sentryActive = active;
        buttonSentry.setDisable(!enabled);
        updateSentryText();
    }

    private void updateSentryText() {
        buttonSentry.setText(
                currentI18n.get(sentryActive ? Buttons.buttonActivateSentry
                        : Buttons.buttonSentry));
        sentryTooltip.setText(
                currentI18n.get(sentryActive ? Buttons.activateSentryExplanation
                        : Buttons.sentryExplanation));
    }

    void setFortifyAction(final Runnable action) {
        buttonFortify.setOnAction(event -> action.run());
    }

    void setFortifyOrder(final boolean ordered, final boolean effective,
            final boolean enabled) {
        fortifying = ordered;
        fortified = effective;
        buttonFortify.setDisable(!enabled);
        updateFortifyText();
    }

    private void updateFortifyText() {
        buttonFortify.setText(currentI18n.get(fortifying
                ? (fortified ? Buttons.buttonCancelFortified
                        : Buttons.buttonCancelFortifying)
                : Buttons.buttonFortify));
        fortifyTooltip.setText(currentI18n.get(fortifying
                ? (fortified ? Buttons.fortifiedExplanation
                        : Buttons.fortifyingExplanation)
                : Buttons.fortifyExplanation));
    }

    private void setDisable(final boolean disable) {
        buttonCongress.setDisable(disable);
        buttonSentry.setDisable(disable);
        buttonFortify.setDisable(disable);
        buttonWait.setDisable(disable);
        buttonSkipUnitTurn.setDisable(disable);
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

    private void setVisibleNode(final Node node, final boolean isVisible) {
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
