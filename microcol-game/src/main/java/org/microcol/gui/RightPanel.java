package org.microcol.gui;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

public class RightPanel implements JavaFxComponent, UpdatableLanguage {

    private final RightPanelView rightPanelView;
    
    private final RightPanelPresenter rightPanelPresenter;

    @Inject
    RightPanel(final RightPanelView rightPanelView, final RightPanelPresenter rightPanelPresenter) {
        this.rightPanelView = Preconditions.checkNotNull(rightPanelView);
        this.rightPanelPresenter = Preconditions.checkNotNull(rightPanelPresenter);

    }

    @Override
    public void updateLanguage(I18n i18n) {
        rightPanelPresenter.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return rightPanelView.getContent();
    }

}
