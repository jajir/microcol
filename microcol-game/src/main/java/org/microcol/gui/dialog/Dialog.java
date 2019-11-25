package org.microcol.gui.dialog;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

/**
 * Here will be text constants for dialogs.
 */
public enum Dialog implements MessageKeyResource {

    ok,
    cancel,
    fight_title,
    fight_attacker,
    fight_defender,
    fight_buttonFight,
    fight_buttonCancel,
    fight_hideOption,
    unitCantFightWarning_caption,
    notEnoughGold_caption,
    independenceWasDeclared_caption,
    unitCantMoveHere_caption,
    gameOver_caption,
    gameOver_allColoniesAreLost,
    gameOver_timeIsUp,
    destroyColony_caption,
    destroyColony_question,
    colonyWasCaptured_caption,
    colonyWasCaptured_text1,
    colonyWasCaptured_text2,
    about_caption,
    about_version,
    saveGame_title,
    loadGame_title,
    buildingQueueDialog_caption;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
