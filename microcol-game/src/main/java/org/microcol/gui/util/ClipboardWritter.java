package org.microcol.gui.util;

import org.microcol.gui.util.ClipboardReader.Transfer;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;

/**
 * Helps to store informations about dragged object and recover them back.
 */
public class ClipboardWritter {

	private final ClipboardContent content;

	private final Dragboard db;

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

	private Transfer transfer;

	private TransferFrom transferFrom;

	public ClipboardWritter addUnit(final Unit unit) {
		Preconditions.checkState(transfer == null, "Clipboard was already set.");
		transfer = new ClipboardReader.UnitTransfer(unit, transferFrom);
		return this;
	}

	public ClipboardWritter addGoodAmount(final GoodAmount goodAmount) {
		Preconditions.checkState(transfer == null, "Clipboard was already set.");
		Preconditions.checkState(
				transferFrom == null || !(transferFrom instanceof ClipboardReader.TransferFromEuropePier),
				"Can't move good from Europe port pier. Europe pier could contain just unit.");
		transfer = new ClipboardReader.GoodTransfer(goodAmount, transferFrom);
		return this;
	}

	private void transferFromCheck(){
		Preconditions.checkState(transfer == null, "TransferFrom should be called before setting transferring object");
		Preconditions.checkState(transferFrom == null, "Transfer from was already set");
	}
	
	public ClipboardWritter addTransferFromUnit(final Unit unit, final CargoSlot cargoSlot) {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromCargoSlot(unit, cargoSlot.getIndex());
		return this;
	}

	public ClipboardWritter addTransferFromEuropePortPier() {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromEuropePier();
		return this;
	}

	public ClipboardWritter addTransferFromEuropeShop() {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromEuropeShop();
		return this;
	}

	public ClipboardWritter addTransferFromOutsideColony() {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromOutsideColony();
		return this;
	}
	
	public ClipboardWritter addTransferFromColonyWarehouse() {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromColonyWarehouse();
		return this;
	}

	public ClipboardWritter addTransferFromColonyField(final Location fieldDirection) {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromColonyField(fieldDirection);
		return this;
	}

	public ClipboardWritter addTransferFromConstructionSlot() {
		transferFromCheck();
		transferFrom = new ClipboardReader.TransferFromConstructionSlot();
		return this;
	}

	public void build() {
		Preconditions.checkState(transfer != null, "No object to transfer was defined.");
		final StringBuilder buff = new StringBuilder();
		transfer.writeTo(buff);
		content.putString(buff.toString());
		db.setContent(content);
	}

}
