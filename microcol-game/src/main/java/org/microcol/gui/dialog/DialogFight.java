package org.microcol.gui.dialog;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.preferences.PreferencesProperties;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.AttackOrder;
import org.microcol.model.CombatStrength;
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

    private final I18n i18n;

    private final Label declaration = new Label();

    private final Label attackerStrength = new Label();
    private final Label defenderStrength = new Label();
    private final Label combatChance = new Label();

    private final CheckBox checkBoxShowNextTime;

    private final Label titleLabel;
    private final Label labelAttacker;
    private final Label labelDefender;
    private final Button buttonCancel;
    private final Button buttonFight;

    private final Label defenderLabelName;

    private final ImageView defenderImageView;

    private final Label attackerLabelName;

    private final ImageView attackerImageView;

    private final LocalizationHelper localizationHelper;

    private final ImageProvider imageProvider;

    /**
     * Constructor when parentFrame is not available.
     *
     * @param i18n                  required localization helper class
     * @param viewUtil              required show dialog utilities
     * @param imageProvider         required image provider
     * @param localizationHelper    required localization helper
     * @param preferencesProperties required game preferences
     */
    @Inject
    public DialogFight(final I18n i18n, final ViewUtil viewUtil,
            final ImageProvider imageProvider,
            final LocalizationHelper localizationHelper,
            final PreferencesProperties preferencesProperties) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        setTitle(i18n.get(Dialog.fight_title));

        final GridPane root = new GridPane();
        root.setId("fightReview");
        final ColumnConstraints columnLeft = new ColumnConstraints();
        columnLeft.setHalignment(HPos.LEFT);
        final ColumnConstraints columnMiddle = new ColumnConstraints();
        columnMiddle.setHgrow(Priority.ALWAYS);
        columnMiddle.setHalignment(HPos.CENTER);
        final ColumnConstraints columnRight = new ColumnConstraints();
        columnRight.setHalignment(HPos.RIGHT);
        root.getColumnConstraints().addAll(columnLeft, columnMiddle,
                columnRight);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));
        init(root);

        // Y=0
        // dialog title
        titleLabel = new Label();
        root.add(titleLabel, 0, 0, 3, 1);

        // Y=1
        // attacker and defender titles
        labelAttacker = new Label();
        root.add(labelAttacker, 0, 1);
        labelDefender = new Label();
        root.add(labelDefender, 2, 1);
        // sword image
        final ImageView swords = new ImageView(
                imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS));
        root.add(swords, 1, 1, 1, 3);

        // Y=2
        // Unit names
        attackerLabelName = new Label();
        attackerLabelName.setId("fightAttacker");
        root.add(attackerLabelName, 0, 2);
        defenderLabelName = new Label();
        defenderLabelName.setId("fightDefender");
        root.add(defenderLabelName, 2, 2);

        // Y=3
        // Unit's images
        attackerImageView = new ImageView();
        attackerImageView.setId("fightAttackerImage");
        root.add(attackerImageView, 0, 3);
        defenderImageView = new ImageView();
        defenderImageView.setId("fightDefenderImage");
        root.add(defenderImageView, 2, 3);

        attackerStrength.setId("fightAttackerStrength");
        defenderStrength.setId("fightDefenderStrength");
        combatChance.setId("fightChance");
        attackerStrength.setWrapText(true);
        defenderStrength.setWrapText(true);
        attackerStrength.setMaxWidth(230);
        defenderStrength.setMaxWidth(230);
        combatChance.setWrapText(true);
        combatChance.setMaxWidth(460);
        root.add(attackerStrength, 0, 4);
        root.add(defenderStrength, 2, 4);
        root.add(combatChance, 0, 5, 3, 1);

        // Y=3
        // checkbox with question
        declaration.setId("fightDeclaration");
        declaration.setWrapText(true);
        declaration.setMaxWidth(420);
        root.add(declaration, 0, 8, 3, 1);
        checkBoxShowNextTime = new CheckBox();
        checkBoxShowNextTime.setId("fightHideAdvisor");
        checkBoxShowNextTime.setWrapText(true);
        checkBoxShowNextTime.setTextAlignment(TextAlignment.JUSTIFY);
        // following force dialog to split message into several lines.
        checkBoxShowNextTime.setMaxWidth(300);
        root.add(checkBoxShowNextTime, 0, 9, 3, 1);

        // Y=10
        // Cancel and OK buttons
        buttonCancel = new Button();
        buttonCancel.setId("fightCancel");
        buttonCancel.setCancelButton(true);
        buttonCancel.setOnAction(e -> onClickCancel(
                checkBoxShowNextTime.isSelected(), preferencesProperties));
        root.add(buttonCancel, 0, 10);

        buttonFight = new Button();
        buttonFight.setId("fightConfirm");
        buttonFight.requestFocus();
        buttonFight.setOnAction(e -> onClickFight(
                checkBoxShowNextTime.isSelected(), preferencesProperties));
        root.add(buttonFight, 2, 10);
    }

    public boolean showAndWait(final AttackOrder order) {
        userChooseFight = false;
        checkBoxShowNextTime.setSelected(false);
        checkBoxShowNextTime.setVisible(!order.isDeclarationRequired());
        checkBoxShowNextTime.setManaged(!order.isDeclarationRequired());
        declaration.setVisible(order.isDeclarationRequired());
        declaration.setManaged(order.isDeclarationRequired());
        declaration
                .setText(
                        order.isDeclarationRequired()
                                ? i18n.get(Dialog.fight_declaration,
                                        order.getDefender().getOwner()
                                                .getDisplayName())
                                : "");
        final boolean ordinary = order.getCombatStrengths().isPresent();
        attackerStrength.setVisible(ordinary);
        attackerStrength.setManaged(ordinary);
        defenderStrength.setVisible(ordinary);
        defenderStrength.setManaged(ordinary);
        combatChance.setVisible(ordinary);
        combatChance.setManaged(ordinary);
        order.getCombatStrengths().ifPresent(strengths -> {
            attackerStrength.setText(describe(strengths.getAttacker()));
            defenderStrength.setText(describe(strengths.getDefender()));
            combatChance.setText(i18n.get(Dialog.fight_chance,
                    number(100 * strengths.getAttackerWinProbability()))
                    + "\n"
                    + i18n.get(order
                            .getAttacker() instanceof org.microcol.model.unit.UnitArtillery
                            || order.getDefender() instanceof org.microcol.model.unit.UnitArtillery
                                    ? Dialog.fight_artilleryLoss
                                    : Dialog.fight_equipmentLoss));
        });
        final Unit attacker = order.getAttacker();
        final Unit defender = order.getDefender();
        attackerLabelName.setText(unitName(attacker));
        attackerImageView.setImage(imageProvider.getUnitImage(attacker));
        defenderLabelName.setText(unitName(defender));
        defenderImageView.setImage(imageProvider.getUnitImage(defender));
        showAndWait();
        return userChooseFight;
    }

    private String unitName(final Unit unit) {
        final String name = localizationHelper.getUnitName(unit.getType());
        return unit instanceof org.microcol.model.unit.UnitArtillery
                && ((org.microcol.model.unit.UnitArtillery) unit).isDamaged()
                        ? i18n.get(Dialog.fight_damagedName, name)
                        : name;
    }

    private String describe(final CombatStrength strength) {
        final StringBuilder out = new StringBuilder(
                i18n.get(Dialog.fight_base, number(strength.getBase())));
        appendBonus(out, Dialog.fight_attackBonus, strength.getAttackBonus());
        appendBonus(out, Dialog.fight_terrainBonus, strength.getTerrainBonus());
        appendBonus(out, Dialog.fight_fortifyBonus,
                strength.getFortificationBonus());
        appendBonus(out, Dialog.fight_colonyBonus, strength.getColonyBonus());
        appendBonus(out, Dialog.fight_artilleryBonus,
                strength.getArtilleryBonus());
        return out.append("\n").append(
                i18n.get(Dialog.fight_total, number(strength.getTotal())))
                .toString();
    }

    private void appendBonus(final StringBuilder out, final Dialog label,
            final int percent) {
        if (percent != 0) {
            out.append("\n").append(i18n.get(label, percent));
        }
    }

    private String number(final double value) {
        return java.math.BigDecimal.valueOf(value)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    @Override
    public void updateLanguage(final I18n language) {
        setTitle(language.get(Dialog.fight_title));
        titleLabel.setText(language.get(Dialog.fight_title));
        labelAttacker.setText(language.get(Dialog.fight_attacker));
        labelDefender.setText(language.get(Dialog.fight_defender));
        buttonCancel.setText(language.get(Dialog.fight_buttonCancel));
        buttonFight.setText(language.get(Dialog.fight_buttonFight));
        checkBoxShowNextTime.setText(language.get(Dialog.fight_hideOption));
    }

    private void onClickCancel(final boolean isCheckBoxShowNextTimeSelected,
            final PreferencesProperties preferencesProperties) {
        userChooseFight = false;
        if (isCheckBoxShowNextTimeSelected) {
            preferencesProperties.getShowFightAdvisorProperty().set(false);
        }
        close();
    }

    private void onClickFight(final boolean isCheckBoxShowNextTimeSelected,
            final PreferencesProperties preferencesProperties) {
        userChooseFight = true;
        if (isCheckBoxShowNextTimeSelected) {
            preferencesProperties.getShowFightAdvisorProperty().set(false);
        }
        close();
    }

}
