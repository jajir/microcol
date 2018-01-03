package org.microcol.gui;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Unit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

public class DialogFigth extends AbstractMessageWindow {

	/**
	 * It's <code>true</code> when user choose to fight.
	 */
	private boolean userChooseFight = false;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param text
	 *            required localization helper class
	 * @param viewUtil
	 *            required show dialog utilities
	 * @param imageProvider
	 *            required image provider
	 * @param localizationHelper
	 *            required localization helper
	 * @param gamePreferences
	 *            required game preferences
	 * @param unitAttacker
	 *            required attacking unit
	 * @param unitDefender
	 *            required defending unit
	 */
	public DialogFigth(final Text text, final ViewUtil viewUtil, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper, final GamePreferences gamePreferences, final Unit unitAttacker,
			final Unit unitDefender) {
		super(viewUtil);
		getDialog().setTitle(text.get("dialogFight.title"));

		final GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));
		init(root);

		// Y=0
		final Label label = new Label(text.get("dialogFight.title"));
		root.add(label, 0, 0, 3, 1);

		/**
		 * Attacker
		 */
		final Label labelAttacker = new Label(text.get("dialogFight.attacker"));
		root.add(labelAttacker, 0, 1);
		describeUnit(root, 0, unitAttacker, imageProvider, localizationHelper);

		/**
		 * Fight image
		 */
		final ImageView swords = new ImageView(imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS));
		root.add(swords, 1, 1, 1, 3);

		/**
		 * Defender
		 */
		final Label labelDefender = new Label(text.get("dialogFight.defender"));
		root.add(labelDefender, 2, 1);
		describeUnit(root, 2, unitDefender, imageProvider, localizationHelper);

		/**
		 * Show next time. Y=3
		 */
		final CheckBox checkBoxShowNextTime = new CheckBox(text.get("dialogFight.hideOption"));
		checkBoxShowNextTime.setWrapText(true);
		checkBoxShowNextTime.setTextAlignment(TextAlignment.JUSTIFY);
		checkBoxShowNextTime.setMaxWidth(300);
		root.add(checkBoxShowNextTime, 0, 9, 3, 1);

		/**
		 * Buttons Y=10
		 */
		final Button buttonCancel = new Button(text.get("dialogFight.buttonCancel"));
		buttonCancel.setOnAction(e -> onClickCancel(checkBoxShowNextTime.isSelected(), gamePreferences));
		root.add(buttonCancel, 0, 10);

		final Button buttonFight = new Button(text.get("dialogFight.buttonFight"));
		buttonFight.requestFocus();
		buttonFight.setOnAction(e -> onClickFight(checkBoxShowNextTime.isSelected(), gamePreferences));
		root.add(buttonFight, 2, 10);

		getDialog().showAndWait();
	}

	private void onClickCancel(final boolean isCheckBoxShowNextTimeSelected, final GamePreferences gamePreferences) {
		userChooseFight = false;
		if (isCheckBoxShowNextTimeSelected) {
			gamePreferences.getShowFightAdvisorProperty().set(false);
		}
		getDialog().close();
	}

	private void onClickFight(final boolean isCheckBoxShowNextTimeSelected, final GamePreferences gamePreferences) {
		userChooseFight = true;
		if (isCheckBoxShowNextTimeSelected) {
			gamePreferences.getShowFightAdvisorProperty().set(false);
		}
		getDialog().close();
	}

	private void describeUnit(final GridPane root, final int column, final Unit unit, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper) {
		final Label labelname = new Label(localizationHelper.getUnitName(unit.getType()));
		root.add(labelname, column, 2);

		final ImageView unitImage = new ImageView(imageProvider.getUnitImage(unit.getType()));
		root.add(unitImage, column, 3);
	}

	public boolean isUserChooseFight() {
		return userChooseFight;
	}

}
