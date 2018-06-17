package org.microcol.gui.util;

import javafx.scene.layout.VBox;

public abstract class AbstractYesNoDialog extends AbstractMessageWindow {

    private final VBox context = new VBox();

    private boolean isSelectedYes = false;

    public AbstractYesNoDialog(final ViewUtil viewUtil, final Text text,
            final String dialogCaption) {
        super(viewUtil);
        setTitle(dialogCaption);

        /**
         * Buttons
         */
        final ButtonsBarYesNo buttonsBar = new ButtonsBarYesNo(text);
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
        root.getChildren().addAll(context, buttonsBar);
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
