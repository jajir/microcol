package org.microcol.gui.dialog;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;

public final class DialogFight extends AbstractMessageWindow {

    /**
     * It's <code>true</code> when user choose to fight.
     */
    private boolean userChooseFight = false;

    private final Label defenderLabelName;

    private final ImageView defenderImageView;

    private final Label attackerLabelName;

    private final ImageView attackerImageView;

    private final LocalizationHelper localizationHelper;

    private final ImageProvider imageProvider;

    /**
     * Constructor when parentFrame is not available.
     * 
     * @param i18n
     *            required localization helper class
     * @param viewUtil
     *            required show dialog utilities
     * @param imageProvider
     *            required image provider
     * @param localizationHelper
     *            required localization helper
     * @param gamePreferences
     *            required game preferences
     */
    @Inject
    public DialogFight(final I18n i18n, final ViewUtil viewUtil, final ImageProvider imageProvider,
            final LocalizationHelper localizationHelper, final GamePreferences gamePreferences) {
        super(viewUtil, i18n);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        setTitle(i18n.get(Dialog.fight_title));

        final GridPane root = new GridPane();
        final ColumnConstraints columnLeft = new ColumnConstraints();
        columnLeft.setHalignment(HPos.LEFT);
        final ColumnConstraints columnMiddle = new ColumnConstraints();
        columnMiddle.setHgrow(Priority.ALWAYS);
        columnMiddle.setHalignment(HPos.CENTER);
        final ColumnConstraints columnRight = new ColumnConstraints();
        columnRight.setHalignment(HPos.RIGHT);
        root.getColumnConstraints().addAll(columnLeft, columnMiddle, columnRight);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));
        init(root);

        // Y=0
        // dialog title
        final Label label = new Label(i18n.get(Dialog.fight_title));
        root.add(label, 0, 0, 3, 1);

        // Y=1
        // attacker and defender titles
        final Label labelAttacker = new Label(i18n.get(Dialog.fight_attacker));
        root.add(labelAttacker, 0, 1);
        final Label labelDefender = new Label(i18n.get(Dialog.fight_defender));
        root.add(labelDefender, 2, 1);
        // sword image
        final ImageView swords = new ImageView(
                imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS));
        root.add(swords, 1, 1, 1, 3);

        // Y=2
        // Unit names
        attackerLabelName = new Label();
        root.add(attackerLabelName, 0, 2);
        defenderLabelName = new Label();
        root.add(defenderLabelName, 2, 2);

        // Y=3
        // Unit's images
        attackerImageView = new ImageView();
        root.add(attackerImageView, 0, 3);
        defenderImageView = new ImageView();
        root.add(defenderImageView, 2, 3);

        // Y=3
        // checkbox with question
        final CheckBox checkBoxShowNextTime = new CheckBox(i18n.get(Dialog.fight_hideOption));
        checkBoxShowNextTime.setWrapText(true);
        checkBoxShowNextTime.setTextAlignment(TextAlignment.JUSTIFY);
        // following force dialog to split message into several lines.
        checkBoxShowNextTime.setMaxWidth(300);
        root.add(checkBoxShowNextTime, 0, 9, 3, 1);

        // Y=10
        // Cancel and OK buttons
        final Button buttonCancel = new Button(i18n.get(Dialog.fight_buttonCancel));
        buttonCancel.setOnAction(
                e -> onClickCancel(checkBoxShowNextTime.isSelected(), gamePreferences));
        root.add(buttonCancel, 0, 10);

        final Button buttonFight = new Button(i18n.get(Dialog.fight_buttonFight));
        buttonFight.requestFocus();
        buttonFight
                .setOnAction(e -> onClickFight(checkBoxShowNextTime.isSelected(), gamePreferences));
        root.add(buttonFight, 2, 10);
    }

    /**
     * 
     * @param unitAttacker
     *            required attacking unit
     * @param unitDefender
     *            required defending unit
     * @return return <code>true</code> when user choose to fight.
     */
    public boolean showAndWait(final Unit unitAttacker, final Unit unitDefender) {
        defenderLabelName.setText(localizationHelper.getUnitName(unitDefender.getType()));
        defenderImageView.setImage(imageProvider.getUnitImage(unitDefender));

        attackerLabelName.setText(localizationHelper.getUnitName(unitDefender.getType()));
        attackerImageView.setImage(imageProvider.getUnitImage(unitDefender));

        showAndWait();
        return userChooseFight;
    }

    private void onClickCancel(final boolean isCheckBoxShowNextTimeSelected,
            final GamePreferences gamePreferences) {
        userChooseFight = false;
        if (isCheckBoxShowNextTimeSelected) {
            gamePreferences.getShowFightAdvisorProperty().set(false);
        }
        close();
    }

    private void onClickFight(final boolean isCheckBoxShowNextTimeSelected,
            final GamePreferences gamePreferences) {
        userChooseFight = true;
        if (isCheckBoxShowNextTimeSelected) {
            gamePreferences.getShowFightAdvisorProperty().set(false);
        }
        close();
    }

    public boolean isUserChooseFight() {
        return userChooseFight;
    }

}
