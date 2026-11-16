package org.akropia.gui.screen.goals;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.MissionGoal;
import org.akropia.model.campaign.MissionGoals;

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

    private final VBox goalsPanel = new VBox(12);

    private final VBox mainPanel = new VBox();

    private final Image imageKingsSeal;

    private final Image imageKingsSealShadow;

    @Inject
    GoalsPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController,
            final ImageProvider imageProvider) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.imageKingsSeal = imageProvider.getImage(KING_SEAL_IMAGE_NAME);
        this.imageKingsSealShadow = imageProvider
                .getImage(KING_SEAL_SHADOW_IMAGE_NAME);

        mainPanel.getStyleClass().add("goals-main-panel");
        mainPanel.getChildren().addAll(goalsPanel);
    }

    public void repaint() {
        goalsPanel.getChildren().clear();
        if (gameModelController.getModel().getCalendar().isScoredPlay()) {
            showFreePlayObjectives();
            return;
        }
        final MissionGoals goals = gameModelController.getGameModel()
                .getMissionGoals();
        if (goals.getGoals().isEmpty()) {
            final Label message = new Label(i18n.get(Goals.noGoals));
            message.setId("noGoalsMessage");
            message.setWrapText(true);
            goalsPanel.getChildren().add(message);
        } else {
            goals.getGoals().forEach(this::makeRow);
        }
    }

    private void showFreePlayObjectives() {
        org.akropia.model.Model model = gameModelController.getModel();
        org.akropia.model.Player human = gameModelController.getHumanPlayer();
        org.akropia.model.FreePlayObjectives goals = model
                .getFreePlayObjectives();
        objectiveLine("freePlayObjectiveRule",
                i18n.get(goals.getEnabled().isEmpty()
                        ? org.akropia.gui.FreePlayText.sandbox
                        : org.akropia.gui.FreePlayText.rule));
        for (org.akropia.model.FreePlayGoal goal : goals.getEnabled()) {
            objectiveLine("objective" + goal.name(), i18n
                    .get(org.akropia.gui.FreePlayText.valueOf(goal.name())));
            String progress;
            switch (goal) {
            case INDEPENDENCE:
                progress = human.getRebellionVictoryOrder().isPresent()
                        ? i18n.get(org.akropia.gui.FreePlayText.achieved)
                        : human.isDeclaredIndependence()
                                ? i18n.get(
                                        org.akropia.gui.FreePlayText.invasion,
                                        goals.occupiedRebellionColonies(human),
                                        goals.remainingRoyalGround(human))
                                : i18n.get(org.akropia.gui.FreePlayText.support,
                                        human.getNationalRebelSupport()
                                                .getDisplayPercent());
                break;
            case CONGRESS:
                progress = i18n.get(
                        org.akropia.gui.FreePlayText.congressProgress,
                        human.getCongressMembers().size());
                break;
            case DOMINATION:
                progress = i18n.get(org.akropia.gui.FreePlayText.opponents,
                        goals.remainingOpponents(human),
                        goals.nations().size() - 1);
                break;
            default:
                throw new IllegalStateException("Unknown objective");
            }
            objectiveLine("objectiveProgress" + goal.name(), progress);
        }
    }

    private void objectiveLine(String id, String text) {
        Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMaxWidth(640);
        goalsPanel.getChildren().add(label);
    }

    private void makeRow(final MissionGoal goal) {
        final HBox row = new HBox();
        row.getStyleClass().add("row");
        final Label caption = new Label(i18n.get(goal.getDescriptionKey()));
        caption.getStyleClass().add("caption");
        row.getChildren().add(caption);
        if (goal.isFinished()) {
            row.getChildren().add(new ImageView(imageKingsSeal));
        } else {
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
    }
}
