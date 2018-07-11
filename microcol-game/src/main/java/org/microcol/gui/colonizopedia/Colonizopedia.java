package org.microcol.gui.colonizopedia;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class Colonizopedia extends AbstractMessageWindow {

    @Inject
    public Colonizopedia(final Text text, final ViewUtil viewUtil) {
        super(viewUtil);
        setTitle(text.get("colonizopedia.title"));
        final VBox root = new VBox();

        final ButtonsBar buttonBar = new ButtonsBar(text);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        final HBox main = new HBox();

        root.getChildren().addAll(main, buttonBar);
        init(root);
    }

}
