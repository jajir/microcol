package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.util.Text;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class StatusBarPresenterProvider_GamePanel implements Provider<StatusBarPresenter> {

    private final StatusBarPresenter statusBarPresenter;

    @Inject
    StatusBarPresenterProvider_GamePanel(final @Named("GamePanel") StatusBarView display,
            final StatusBarMessageController statusBarMessageController,
            final ChangeLanguageController changeLanguangeController, final Text text,
            final GameModelController gameModelController) {
        statusBarPresenter = new StatusBarPresenter(display, statusBarMessageController,
                changeLanguangeController, text, gameModelController);
    }

    @Override
    public StatusBarPresenter get() {
        return statusBarPresenter;
    }

}
