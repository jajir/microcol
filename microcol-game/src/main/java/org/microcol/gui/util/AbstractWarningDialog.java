package org.microcol.gui.util;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class AbstractWarningDialog<T extends Enum<T> & MessageKeyResource>
        extends AbstractMessageWindow {

    /**
     * Vertical box where user can place it's context.
     */
    private final VBox context = new VBox();

    private AbstractWarningDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);

        /**
         * Buttons
         */
        final ButtonsBar buttonsBar = new ButtonsBar(i18n.get(Loc.ok));
        buttonsBar.getButtonOk().setOnAction(e -> {
            close();
        });
        buttonsBar.getButtonOk().requestFocus();

        final VBox root = new VBox();
        root.getChildren().addAll(context, buttonsBar);
        init(root);
    }

    /**
     * Default constructor.
     * 
     * @param viewUtil
     *            required utility class for showing dialog
     * @param i18n
     *            required localization tool
     * @param messageKey
     *            required message key
     * @param <T>
     *            required enumeration class extended from base message resource
     *            class
     */
    public <T extends Enum<T> & MessageKeyResource> AbstractWarningDialog(final ViewUtil viewUtil,
            final I18n i18n, final T messageKey) {
        this(viewUtil, i18n);
        final String dialogCaption = i18n.get(messageKey);
        setTitle(dialogCaption);
        context.getChildren().add(new Label(dialogCaption));
    }

    /**
     * @return the context
     */
    public VBox getContext() {
        return context;
    }

}
