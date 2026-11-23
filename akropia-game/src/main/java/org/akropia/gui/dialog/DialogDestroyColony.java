package org.akropia.gui.dialog;

import java.util.List;
import java.util.stream.Collectors;
import org.akropia.gui.util.AbstractYesNoDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import com.google.inject.Inject;
import javafx.scene.control.Label;

/** Confirms still-empty cities only when the player ends the turn. */
public class DialogDestroyColony extends AbstractYesNoDialog {
    private final I18n i18n;
    private final Label question = new Label();

    @Inject
    public DialogDestroyColony(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, i18n.get(Dialog.destroyColony_caption),
                i18n.get(Dialog.destroyColony_confirm));
        this.i18n = i18n;
        question.setId("empty-colonies-question");
        question.setWrapText(true);
        question.setMaxWidth(500);
        getContext().getChildren().add(question);
    }

    public boolean confirm(final List<Colony> colonies) {
        question.setText(i18n.get(Dialog.destroyColony_question,
                colonies.stream().map(Colony::getName)
                        .collect(Collectors.joining(", "))));
        return showWaitAndReturnIfYesWasSelected();
    }
}
