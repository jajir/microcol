package org.microcol.gui.buttonpanel;

import org.microcol.gui.event.StatusBarMessageEvent;
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
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class ButtonPanel implements JavaFxComponent {

    private final ImageProvider imageProvider;
    private final EventBus eventBus;
    private final I18n i18n;

    private final VBox mainPanel;
    private final HBox buttonPanel;

    private final Button buttonCenter;
    private final Button buttonHelp;
    private final Button buttonStatistics;
    private final Button buttonExit;

    private final Button buttonGoals;
    private final Button buttonTurnReport;
    private final Button buttonNextTurn;
    private final Button buttonEurope;

    @Inject
    ButtonPanel(final ImageProvider imageProvider, final EventBus eventBus, final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);

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

        buttonPanel = new HBox();
        buttonPanel.getStyleClass().add("buttonPanel");
        buttonPanel.getChildren().add(buttonStatistics);
        buttonPanel.getChildren().add(buttonCenter);
        buttonPanel.getChildren().add(buttonTurnReport);
        buttonPanel.getChildren().add(buttonGoals);
        buttonPanel.getChildren().add(buttonHelp);
        buttonPanel.getChildren().add(buttonEurope);
        buttonPanel.getChildren().add(buttonExit);
        buttonPanel.getChildren().add(buttonNextTurn);

        mainPanel = new VBox();
        mainPanel.getChildren().add(buttonPanel);
        mainPanel.setPickOnBounds(false);
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
    }

    private Button makeButon(final String imgName, final Buttons buttonsKey) {
        final BackgroundImage nextButtonImage = new BackgroundImage(imageProvider.getImage(imgName),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.RIGHT, 0.5, true, Side.TOP, 0.5, true),
                BackgroundSize.DEFAULT);
        final Button button = new Button();
        button.setOnMouseEntered(event -> eventBus
                .post(new StatusBarMessageEvent(i18n.get(buttonsKey), Source.GAME)));
        button.setBackground(new Background(nextButtonImage));
        return button;
    }

    @Override
    public Region getContent() {
        return mainPanel;
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
