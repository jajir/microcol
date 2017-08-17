package org.microcol.gui.util;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmmount;
import org.microcol.model.GoodType;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.input.Dragboard;

/**
 * Helps to read informations about dragged object.
 */
public class ClipboardReader {

	final static String KEY_UNIT = "Unit";

	final static String KEY_GOODS = "Goods";

	final static String KEY_FROM_UNIT = "FromUnit";

	final static String KEY_FROM_EUROPE_PORT_PIER = "FromEuropePortPier";

	final static String SEPARATOR = ",";

	private final Model model;

	private final String originalString;

	private final String[] parts;

	public static ParsingResult make(final Model model, final Dragboard db) {
		return new ClipboardReader(model, db).makeParsing();
	}

	private ClipboardReader(final Model model, final Dragboard db) {
		this.model = Preconditions.checkNotNull(model);
		this.originalString = db.getString();
		Preconditions.checkNotNull(db);
		if (db.getString() == null || db.getString().isEmpty()) {
			parts = new String[0];
		} else {
			parts = db.getString().split(SEPARATOR);
		}
	}

	private GoodTransfer tryReadGoodTransfer() {
		if (parts.length <= 2) {
			return null;
		}
		if (!KEY_GOODS.equals(parts[0])) {
			return null;
		}
		if (tryReadGoodType(parts[1]) == null) {
			return null;
		}
		if (tryRead(parts[2]) == null) {
			return null;
		}
		final int ammount = read(parts[2]);
		final GoodType goodType = GoodType.valueOf(parts[1]);
		final GoodAmmount goodAmmount = new GoodAmmount(goodType, ammount);
		return new GoodTransfer(goodAmmount, tryReadTransferFrom(3));
	}

	private UnitTransfer tryReadUnitTransfer() {
		if (parts.length <= 1) {
			return null;
		}
		if (!KEY_UNIT.equals(parts[0])) {
			return null;
		}
		if (tryRead(parts[1]) == null) {
			return null;
		}
		final Unit unit = model.getUnitById(read(parts[1]));
		return new UnitTransfer(unit, tryReadTransferFrom(2));

	}

	private TransferFromCargoSlot tryReadTransferFrom(int fromIndex) {
		if (parts.length < fromIndex + 3) {
			return null;
		}
		if (!KEY_FROM_UNIT.equals(parts[fromIndex + 0])) {
			return null;
		}
		if (tryRead(parts[fromIndex + 1]) == null) {
			return null;
		}
		if (tryRead(parts[fromIndex + 2]) == null) {
			return null;
		}
		return new TransferFromCargoSlot(model.getUnitById(read(parts[fromIndex + 1])), read(parts[fromIndex + 2]));
	}

	private int read(final String num) {
		try {
			return Integer.valueOf(num);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("String (" + num + ") is not a number.");
		}
	}

	private ParsingResult makeParsing() {
		return new ParsingResult(tryReadUnitTransfer(), tryReadGoodTransfer());
	}

	private Integer tryRead(final String num) {
		try {
			return Integer.valueOf(num);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private GoodType tryReadGoodType(final String str) {
		try {
			return GoodType.valueOf(str);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public class ParsingResult {

		private final UnitTransfer unitTransfer;

		private final GoodTransfer goodTransfer;

		ParsingResult(final UnitTransfer unitTransfer, final GoodTransfer goodTransfer) {
			this.unitTransfer = unitTransfer;
			this.goodTransfer = goodTransfer;
		}

		public ParsingResult filterUnit(final Predicate<Unit> filter) {
			if (unitTransfer == null) {
				return this;
			} else {
				if (filter.test(unitTransfer.unit)) {
					return this;
				} else {
					return new ParsingResult(null, goodTransfer);
				}
			}
		}

		public ParsingResult filterGoods(final Predicate<GoodAmmount> filter) {
			if (goodTransfer == null) {
				return this;
			} else {
				if (filter.test(goodTransfer.goodAmmount)) {
					return this;
				} else {
					return new ParsingResult(unitTransfer, null);
				}
			}
		}

		public Optional<Unit> getUnit() {
			if (unitTransfer == null) {
				return Optional.empty();
			} else {
				return Optional.of(unitTransfer.unit);
			}
		}

		public Optional<GoodAmmount> getGoods() {
			if (goodTransfer == null) {
				return Optional.empty();
			} else {
				return Optional.of(goodTransfer.goodAmmount);
			}
		}

		public ParsingResult tryReadUnit(final BiConsumer<Unit, Optional<TransferFrom>> consumer) {
			if (unitTransfer != null) {
				consumer.accept(unitTransfer.unit, unitTransfer.getTransferFrom());
			}
			return this;
		}

		public ParsingResult readUnit(final BiConsumer<Unit, Optional<TransferFrom>> consumer) {
			if (unitTransfer == null) {
				throw new IllegalStateException("Unable to read unit from string '" + originalString + "'");
			} else {
				consumer.accept(unitTransfer.unit, unitTransfer.getTransferFrom());
				return this;
			}
		}

		public ParsingResult tryReadGood(final BiConsumer<GoodAmmount, Optional<TransferFrom>> consumer) {
			if (goodTransfer != null) {
				consumer.accept(goodTransfer.goodAmmount, goodTransfer.getTransferFrom());
			}
			return this;
		}

		public ParsingResult readGood(final BiConsumer<GoodAmmount, Optional<TransferFrom>> consumer) {
			if (goodTransfer == null) {
				throw new IllegalStateException("Unable to read good from string '" + originalString + "'");
			} else {
				consumer.accept(goodTransfer.goodAmmount, goodTransfer.getTransferFrom());
				return this;
			}
		}

		public boolean isEmpty() {
			return unitTransfer == null && goodTransfer == null;
		}

	}

	public static interface Transfer {

		Optional<TransferFrom> getTransferFrom();

		void writeTo(StringBuilder buff);

	}

	public static abstract class AbstractTransfer implements Transfer {

		private final TransferFrom transferFrom;

		AbstractTransfer(final TransferFrom transferFrom) {
			this.transferFrom = transferFrom;
		}

		@Override
		public Optional<TransferFrom> getTransferFrom() {
			if (transferFrom == null) {
				return Optional.empty();
			} else {
				return Optional.of(transferFrom);
			}
		}

	}

	public static class GoodTransfer extends AbstractTransfer {

		private final GoodAmmount goodAmmount;

		GoodTransfer(final GoodAmmount goodAmmount, final TransferFrom transferFrom) {
			super(transferFrom);
			this.goodAmmount = goodAmmount;
		}

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(KEY_GOODS);
			buff.append(SEPARATOR);
			buff.append(goodAmmount.getGoodType().name());
			buff.append(SEPARATOR);
			buff.append(goodAmmount.getAmmount());
			getTransferFrom().ifPresent(transferFrom -> transferFrom.writeTo(buff));
		}

	}

	public static class UnitTransfer extends AbstractTransfer {

		private final Unit unit;

		UnitTransfer(final Unit unit, TransferFrom transferFrom) {
			super(transferFrom);
			this.unit = unit;
		}

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(KEY_UNIT);
			buff.append(SEPARATOR);
			buff.append(unit.getId());
			getTransferFrom().ifPresent(transferFrom -> transferFrom.writeTo(buff));
		}

	}

	/**
	 * Interface unify all description of places from which could be object
	 * transfered.
	 */
	public static interface TransferFrom {

		void writeTo(StringBuilder buff);

	}

	public static class TransferFromCargoSlot implements TransferFrom {

		private final Unit sourceUnit;

		private final CargoSlot cargoSlot;

		TransferFromCargoSlot(final Unit unit, final int cargoSlotIndex) {
			this.sourceUnit = unit;
			cargoSlot = unit.getCargo().getSlotByIndex(cargoSlotIndex);
		}

		public Unit getSourceUnit() {
			return sourceUnit;
		}

		public CargoSlot getCargoSlot() {
			return cargoSlot;
		}

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_UNIT);
			buff.append(SEPARATOR);
			buff.append(sourceUnit.getId());
			buff.append(SEPARATOR);
			buff.append(cargoSlot.getIndex());
		}

	}

	/**
	 * Unit was taken from Europe port pier.
	 */
	public static class TransferFromEuropePier implements TransferFrom {

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_EUROPE_PORT_PIER);
		}

	}

}
