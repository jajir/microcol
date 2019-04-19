package org.microcol.gui.util;

import org.microcol.i18n.I18n;

import javafx.scene.layout.VBox;

public abstract class AbstractYesNoDialog extends AbstractMessageWindow {

    private final VBox context = new VBox();

    private boolean isSelectedYes = false;

    public AbstractYesNoDialog(final ViewUtil viewUtil, final I18n i18ns,
            final String dialogCaption) {
        super(viewUtil, i18ns);
        setTitle(dialogCaption);

        /**
         * Buttons
         */
        final ButtonsBarYesNo buttonsBar = new ButtonsBarYesNo(i18ns);
        buttonsBar.getButtonYes().setOnAction(e -> {
            isSelectedYes = true;
            close();
        });
        buttonsBar.getButtonNo().setOnAction(e -> {
            isSelectedYes = false;
            close();
        });
        buttonsBar.getButtonYes().requestFocus();

        final VBox root = new VBox();
        root.getChildren().addAll(context, buttonsBar.getContent());
        init(root);
    }

    public boolean showWaitAndReturnIfYesWasSelected() {
        showAndWait();
        return isSelectedYes;
    }

    /**
     * @return the context
     */
    public VBox getContext() {
        return context;
    }

}
