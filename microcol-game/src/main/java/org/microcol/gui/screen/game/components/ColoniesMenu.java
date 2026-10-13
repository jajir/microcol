package org.microcol.gui.screen.game.components;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Player;

import java.util.Comparator;
import java.util.List;

/**
 * GUI component that displays a menu of the current human player's colonies.
 */
@Singleton
@Listener
public final class ColoniesMenu implements JavaFxComponent {

    private final GameModelController gameModelController;
    private final EventBus eventBus;
    private final I18n i18n;

    private final MenuButton button = new MenuButton();

    @Inject
    public ColoniesMenu(GameModelController gameModelController,
            EventBus eventBus, I18n i18n) {
        this.gameModelController = gameModelController;
        this.eventBus = eventBus;
        this.i18n = i18n;
        initButton();
    }

    private void initButton() {
        button.setId("coloniesMenu");
        button.setText(i18n.get(Buttons.buttonColonies));
        button.setOnShowing(e -> rebuildMenu());
    }

    private void rebuildMenu() {
        button.getItems().clear();

        if (!gameModelController.isGameModelReady()) {
            addPlaceholder();
            return;
        }

        Player human = gameModelController.getHumanPlayer();
        List<Colony> colonies = human.getColonies();

        colonies.stream().filter(
                colony -> colony.isValid() && colony.getOwner().equals(human))
                .sorted(Comparator.comparingInt(Colony::getId))
                .forEach(colony -> {
                    MenuItem item = new MenuItem(colony.getName());
                    item.setId("openColony-" + colony.getId());
                    item.setOnAction(ev -> {
                        if (!gameModelController.isGameModelReady()
                                || !colony.isValid()
                                || !colony.getOwner().equals(
                                        gameModelController.getHumanPlayer())) {
                            rebuildMenu();
                        } else {
                            eventBus.post(
                                    new ShowScreenEvent(Screen.COLONY, colony));
                        }
                    });
                    button.getItems().add(item);
                });

        if (button.getItems().isEmpty()) {
            addPlaceholder();
        }
    }

    private void addPlaceholder() {
        MenuItem placeholder = new MenuItem(i18n.get(Buttons.noColonies));
        placeholder.setDisable(true);
        button.getItems().add(placeholder);
    }

    @Override
    public MenuButton getContent() {
        return button;
    }

    public void show() {
        button.requestFocus();
        button.show();
    }

    @Subscribe
    public void onChangeLanguage(ChangeLanguageEvent event) {
        button.setText(i18n.get(Buttons.buttonColonies));
    }
}
