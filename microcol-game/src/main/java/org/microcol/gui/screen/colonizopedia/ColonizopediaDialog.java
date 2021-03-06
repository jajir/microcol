package org.microcol.gui.screen.colonizopedia;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ColonizopediaDialog extends AbstractMessageWindow {

    @Inject
    public ColonizopediaDialog(final I18n i18n, final ViewUtil viewUtil) {
        super(viewUtil, i18n);
        final VBox root = new VBox();

        final ButtonBarOk buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        final HBox main = new HBox();

        root.getChildren().addAll(main, buttonBar.getContent());
        init(root);
    }

    @Override
    public void updateLanguage(I18n i18n) {
        setTitle(i18n.get(Colonizopedia.title));
    }

}
