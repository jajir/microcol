package org.akropia.gui.screen.market;

import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import com.google.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared modal frame for a quoted purchase or sale. */
public final class MarketView extends AbstractMessageWindow
        implements JavaFxComponent {
    private final Label title = new Label();
    private final VBox content = new VBox(12, title);

    @Inject
    MarketView(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        content.setId("marketDialog");
        content.getStyleClass().add("market-dialog");
        content.setPrefWidth(700);
        title.setWrapText(true);
        title.setId("marketTitle");
        init(content);
    }

    public void setMenuPanel(final JavaFxComponent panel) {
        content.getChildren().add(panel.getContent());
    }

    public void setToolbar(final Region toolbar) {
        content.getChildren().add(toolbar);
    }

    @Override
    public void setTitle(final String text) {
        super.setTitle(text);
        title.setText(text);
    }

    @Override
    public Region getContent() {
        return content;
    }
}
