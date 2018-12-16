package org.microcol.gui.buttonpanel;

import org.microcol.gui.event.AboutGameEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.DeclareIndependenceEvent;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.ShowGoalsEvent;
import org.microcol.gui.event.ShowStatisticsEvent;
import org.microcol.gui.event.ShowTurnReportEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.Node;
import javafx.scene.control.Button;

@Singleton
public class ButtonsGamePanel extends AbstractButtonsPanel {

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
        buttonMove = makeButon(ImageLoaderButtons.BUTTON_MOVE, Buttons.buttonMove);
        buttonMove.setOnAction(event -> eventBus.post(new StartMoveEvent()));
        buttonPlowField = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD, Buttons.buttonPlowField);
        buttonPlowField.setOnAction(event -> eventBus.post(new PlowFieldEvent()));
        buttonBuildColony = makeButon(ImageLoaderButtons.BUTTON_BUILD_COLONY,
                Buttons.buttonBuildColony);
        buttonBuildColony.setOnAction(event -> eventBus.post(new BuildColonyEvent()));
        buttonDeclareIndependence = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonDeclareIndependence);
        buttonDeclareIndependence
                .setOnAction(event -> eventBus.post(new DeclareIndependenceEvent()));

        getButtonPanel().getChildren().add(buttonStatistics);
        getButtonPanel().getChildren().add(buttonCenter);
        getButtonPanel().getChildren().add(buttonTurnReport);
        getButtonPanel().getChildren().add(buttonGoals);
        getButtonPanel().getChildren().add(buttonHelp);
        getButtonPanel().getChildren().add(buttonEurope);
        getButtonPanel().getChildren().add(buttonExit);
        getButtonPanel().getChildren().add(buttonNextTurn);
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
        if (isContaining(node)) {
            if (!isVisible) {
                remove(node);
            }
        } else {
            if (isVisible) {
                getButtonPanel().getChildren().add(0, node);
            }
        }
    }

    /**
     * @return the buttonNextTurn
     */
    public Button getButtonNextTurn() {
        return buttonNextTurn;
    }

}
