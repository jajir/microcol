package org.akropia.gui.dialog;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.akropia.gui.buttonpanel.Buttons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.ButtonsBarYesNo;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.Unit;
import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** One confirmation for all warnings at the requested turn boundary. */
public final class EndTurnWarningsDialog extends AbstractMessageWindow {
    private final I18n i18n;
    private boolean proceed;
    private Colony selectedColony;

    @Inject
    EndTurnWarningsDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    public boolean confirm(final Map<Colony, List<Unit>> warnings,
            final Consumer<Colony> openColony) {
        proceed = false;
        selectedColony = null;
        final VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        final Label caption = new Label(
                i18n.get(Buttons.endTurnWarningCaption));
        caption.setWrapText(true);
        caption.setMaxWidth(440);
        content.getChildren().add(caption);
        final VBox rows = new VBox(8);
        warnings.forEach((colony, units) -> {
            final Button row = ActionButtons.button(ButtonIcon.COLONIES,
                    colony.getName() + " (" + units.size() + ")");
            row.setId("end-turn-colony-" + colony.getId());
            row.setOnAction(event -> {
                selectedColony = colony;
                close();
            });
            rows.getChildren().add(row);
        });
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(Math.min(260, warnings.size() * 55));
        content.getChildren().add(scroll);
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Buttons.endTurnProceed),
                i18n.get(Buttons.endTurnBack));
        buttons.getButtonYes().setId("end-turn-proceed");
        buttons.getButtonYes().setOnAction(event -> {
            proceed = true;
            close();
        });
        buttons.getButtonNo().setId("end-turn-back");
        buttons.getButtonNo().setDefaultButton(true);
        buttons.getButtonNo().setCancelButton(true);
        buttons.getButtonNo().setOnAction(event -> close());
        content.getChildren().add(buttons.getContent());
        content.setId("end-turn-warnings");
        init(content);
        setTitle(i18n.get(Buttons.endTurnWarningTitle));
        buttons.getButtonNo().requestFocus();
        showAndWait();
        if (selectedColony != null) {
            openColony.accept(selectedColony);
        }
        return proceed;
    }
}
