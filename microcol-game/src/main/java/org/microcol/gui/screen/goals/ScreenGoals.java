package org.microcol.gui.screen.goals;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

/**
 * Screen with market in Europe. It allows to buy various goods.
 */
@Singleton
public class ScreenGoals extends AbstractScreenMenu implements GameScreen {

    private final GoalsPanel goalsPanel;

    private final GoalsView goalsView;

    private final I18n i18n;

    public static final String STYLE_SHEET_GOALS = ScreenMenu.class
            .getResource("/gui/Goals.css").toExternalForm();

    @Inject
    ScreenGoals(final I18n i18n, final GoalsPanel goalsPanel, final GoalsView goalsView) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.goalsView = Preconditions.checkNotNull(goalsView);
        goalsView.getContent().getStylesheets().add(STYLE_SHEET_GOALS);
        this.goalsPanel = Preconditions.checkNotNull(goalsPanel);
        goalsView.setMenuPanel(goalsPanel);
    }

    @Override
    public Region getContent() {
        return goalsView.getContent();
    }

    @Override
    public void beforeShow() {
        goalsView.beforeShow();
    }

    @Override
    public void beforeHide() {
        goalsView.beforeHide();
    }

    protected I18n getI18n() {
        return i18n;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        goalsView.setTitle(i18n.get(Goals.title));
        goalsPanel.updateLanguage(i18n);
    }

}
