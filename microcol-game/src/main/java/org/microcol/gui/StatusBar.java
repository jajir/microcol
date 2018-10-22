package org.microcol.gui;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

public class StatusBar implements JavaFxComponent, UpdatableLanguage {

    private final StatusBarPresenter statusBarPresenter;

    private final StatusBarView statusBarView;

    @Inject
    public StatusBar(final StatusBarView statusBarView,
            final StatusBarPresenter statusBarPresenter) {
        this.statusBarView = Preconditions.checkNotNull(statusBarView);
        this.statusBarPresenter = Preconditions.checkNotNull(statusBarPresenter);
        statusBarPresenter.setStatusBarView(statusBarView);
    }

    @Override
    public Region getContent() {
        return statusBarView.getContent();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        statusBarPresenter.updateLanguage(i18n);
    }

}
