package org.akropia.gui.screen.goals;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.GameScreen;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class GoalsPanel implements GameScreen {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final VBox goalsPanel = new VBox(12);

    private final VBox mainPanel = new VBox();

    @Inject
    GoalsPanel(final I18n i18n, final GameModelController gameModelController) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);

        mainPanel.getStyleClass().add("goals-main-panel");
        mainPanel.getChildren().addAll(goalsPanel);
    }

    public void repaint() {
        goalsPanel.getChildren().clear();
        showFreePlayObjectives();
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
