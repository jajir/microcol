package org.microcol.gui.screen.game.components;

import java.util.function.Supplier;

import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.Node;

public class StatusBar implements JavaFxComponent, UpdatableLanguage {

    private final StatusBarPresenter statusBarPresenter;

    private final StatusBarView statusBarView;

    private Runnable refreshHint = () -> {
    };

    @Inject
    public StatusBar(final StatusBarView statusBarView,
            final StatusBarPresenter statusBarPresenter) {
        this.statusBarView = Preconditions.checkNotNull(statusBarView);
        this.statusBarPresenter = Preconditions
                .checkNotNull(statusBarPresenter);
        statusBarPresenter.setStatusBarView(statusBarView);
    }

    @Override
    public Region getContent() {
        return statusBarView.getContent();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        statusBarPresenter.updateLanguage(i18n);
        refreshHint.run();
    }

    /** Temporarily explain a heading without losing the previous context. */
    public void bindHint(final Node heading, final Supplier<String> text) {
        refreshHint = () -> statusBarPresenter.setHint(
                heading.isHover() || heading.isFocused() ? text.get() : null);
        heading.hoverProperty()
                .addListener((value, before, after) -> refreshHint.run());
        heading.focusedProperty()
                .addListener((value, before, after) -> refreshHint.run());
    }

    /**
     * @param showEventsFromSource the showEventsFromSource to set
     */
    public void setShowEventsFromSource(final Source showEventsFromSource) {
        statusBarPresenter.setShowEventsFromSource(showEventsFromSource);
    }

}
