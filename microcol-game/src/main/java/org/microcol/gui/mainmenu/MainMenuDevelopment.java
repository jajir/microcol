package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.PersistentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Class just logically separate development menu handling from rest of main
 * menu.
 * <p>
 * Development menu is not localized.
 * </p>
 */
public class MainMenuDevelopment {

    private final Logger logger = LoggerFactory.getLogger(MainMenuDevelopment.class);

    private final Menu developmentMenu;

    private final Menu menuNewScenario;

    private final GameController gameController;

    @Inject
    public MainMenuDevelopment(final GameModelController gameModelController,
            final PersistentService persistentSrevice, final GameController gameController) {
        this.gameController = Preconditions.checkNotNull(gameController);
        developmentMenu = new Menu("Development");

        final CheckMenuItem checkBoxStopAi = new CheckMenuItem("Suspend AI");
        checkBoxStopAi.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN));
        checkBoxStopAi.selectedProperty().addListener((on, oldValue, newValue) -> {
            boolean isSelected = newValue;
            if (isSelected) {
                new Thread(() -> gameModelController.suspendAi()).start();
                logger.debug("AI was stopped.");
            } else {
                logger.debug("AI was started.");
                new Thread(() -> gameModelController.resumeAi()).start();
            }
        });

        menuNewScenario = new Menu();
        menuNewScenario.setText("New test scenario");
        persistentSrevice.getScenarios().forEach(scenario -> {
            final MenuItem menuItem = new MenuItem(scenario.getName());
            menuItem.setOnAction(event -> {
                onSelectTestScenario(scenario.getFileName());
            });
            menuNewScenario.getItems().add(menuItem);
        });

        developmentMenu.getItems().addAll(checkBoxStopAi, menuNewScenario);
    }

    public Menu getDevelopmentMenu() {
        return developmentMenu;
    }

    private void onSelectTestScenario(final String scenarioFileName) {
        gameController.startTestScenario(scenarioFileName);
    }
}
