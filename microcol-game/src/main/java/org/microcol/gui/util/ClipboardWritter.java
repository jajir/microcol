package org.microcol.gui.util;

import org.microcol.model.GoodAmmount;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;

/**
 * Helps to store informations about dragged object and recover them back.
 */
public class ClipboardWritter {

	final static String KEY_UNIT = "Unit";

	final static String KEY_GOODS = "Goods";

	final static String SEPARATOR = ",";

	private final ClipboardContent content;

	private final Dragboard db;

	private boolean isEmpty = true;

	public static ClipboardWritter make(final Dragboard db) {
		return new ClipboardWritter(db);
	}

	private ClipboardWritter(final Dragboard db) {
		this.db = Preconditions.checkNotNull(db);
		content = new ClipboardContent();
	}

	public ClipboardWritter addImage(final Image image) {
		content.putImage(image);
		return this;
	}

	public ClipboardWritter addUnit(final Unit unit) {
		Preconditions.checkState(isEmpty, "Clipboard was already set.");
		content.putString(KEY_UNIT + SEPARATOR + unit.getId());
		isEmpty = false;
		return this;
	}

	public ClipboardWritter addGoodAmmount(final GoodAmmount goodAmmount) {
		Preconditions.checkState(isEmpty, "Clipboard was already set.");
		content.putString(
				KEY_GOODS + SEPARATOR + goodAmmount.getGoodType().name() + SEPARATOR + goodAmmount.getAmmount());
		isEmpty = false;
		return this;
	}

	public void build() {
		Preconditions.checkState(!isEmpty, "Clipboard is empty.");
		db.setContent(content);
	}

}
