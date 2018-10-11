package org.microcol.gui.europe;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Region;

@Singleton
public class EuropeScrollPanel implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final EuropePanel europePanel;

    private final ScrollPane scrollPane;

    @Inject
    EuropeScrollPanel(final EuropePanel europePanel) {
        this.europePanel = Preconditions.checkNotNull(europePanel);
        scrollPane = new ScrollPane(europePanel.getContent());
        scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }

    @Override
    public void repaint() {
        europePanel.repaint();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        europePanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return scrollPane;
    }

}
