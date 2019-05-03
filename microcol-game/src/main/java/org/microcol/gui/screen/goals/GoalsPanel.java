package org.microcol.gui.screen.goals;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.MissionGoal;
import org.microcol.model.campaign.MissionGoals;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class GoalsPanel implements GameScreen {

    private final static String KING_SEAL_IMAGE_NAME = "kings-seal.png";

    private final static String KING_SEAL_SHADOW_IMAGE_NAME = "kings-seal-shadow.png";

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private final VBox goalsPanel = new VBox();

    private final VBox mainPanel = new VBox();

    private final ButtonBarOk buttonsBar = new ButtonBarOk();

    private final Image imageKingsSeal;

    private final Image imageKingsSealShadow;

    @Inject
    GoalsPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController, final ImageProvider imageProvider) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.imageKingsSeal = imageProvider.getImage(KING_SEAL_IMAGE_NAME);
        this.imageKingsSealShadow = imageProvider.getImage(KING_SEAL_SHADOW_IMAGE_NAME);

        buttonsBar.getButtonOk().setOnAction(this::onClose);
        mainPanel.getChildren().addAll(goalsPanel, buttonsBar.getContent());
    }

    public void repaint() {
        goalsPanel.getChildren().clear();
        final MissionGoals goals = gameModelController.getGameModel().getMissionGoals();
        goals.getGoals().forEach(this::makeRow);
    }

    private void makeRow(final MissionGoal goal) {
        final HBox row = new HBox();
        row.getStyleClass().add("row");
        final Label caption = new Label(i18n.get(goal.getDescriptionKey()));
        caption.getStyleClass().add("caption");
        row.getChildren().add(caption);
        if (goal.isFinished()) {
            row.getChildren().add(new ImageView(imageKingsSeal));
        }else {
            row.getChildren().add(new ImageView(imageKingsSealShadow));
        }
        goalsPanel.getChildren().add(row);
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    @Override
    public void beforeShow() {
        repaint();
    }

    @Override
    public void beforeHide() {

    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        buttonsBar.setButtonText(i18n.get(Goals.buttonBack));
    }
}
