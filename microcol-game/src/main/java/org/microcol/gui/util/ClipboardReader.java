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
			parts = db.getString().split(ClipboardWritter.SEPARATOR);
		}
	}

	private GoodTransfer tryReadGoodTransfer() {
		if (parts.length <= 2) {
			return null;
		}
		if (!ClipboardWritter.KEY_GOODS.equals(parts[0])) {
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
		if (!ClipboardWritter.KEY_UNIT.equals(parts[0])) {
			return null;
		}
		if (tryRead(parts[1]) == null) {
			return null;
		}
		final Unit unit = model.getUnitById(read(parts[1]));
		return new UnitTransfer(unit, tryReadTransferFrom(2));

	}

	private TransferFrom tryReadTransferFrom(int fromIndex) {
		if (parts.length < fromIndex + 3) {
			return null;
		}
		if (!ClipboardWritter.KEY_FROM_UNIT.equals(parts[fromIndex + 0])) {
			return null;
		}
		if (tryRead(parts[fromIndex + 1]) == null) {
			return null;
		}
		if (tryRead(parts[fromIndex + 2]) == null) {
			return null;
		}
		return new TransferFrom(model.getUnitById(read(parts[fromIndex + 1])), read(parts[fromIndex + 2]));
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

	class GoodTransfer {

		private final GoodAmmount goodAmmount;

		private final TransferFrom transferFrom;

		GoodTransfer(final GoodAmmount goodAmmount, final TransferFrom sourceUnit) {
			this.goodAmmount = goodAmmount;
			this.transferFrom = sourceUnit;
		}

		Optional<TransferFrom> getTransferFrom() {
			if (transferFrom == null) {
				return Optional.empty();
			} else {
				return Optional.of(transferFrom);
			}
		}

	}

	class UnitTransfer {

		private final Unit unit;

		private final TransferFrom transferFrom;

		UnitTransfer(final Unit unit, TransferFrom sourceUnit) {
			this.unit = unit;
			this.transferFrom = sourceUnit;
		}

		Optional<TransferFrom> getTransferFrom() {
			if (transferFrom == null) {
				return Optional.empty();
			} else {
				return Optional.of(transferFrom);
			}
		}

	}

	public class TransferFrom {

		private final Unit sourceUnit;

		private final CargoSlot cargoSlot;

		TransferFrom(final Unit unit, final int cargoSlotIndex) {
			this.sourceUnit = unit;
			cargoSlot = unit.getCargo().getSlotByIndex(cargoSlotIndex);
		}

		public Unit getSourceUnit() {
			return sourceUnit;
		}

		public CargoSlot getCargoSlot() {
			return cargoSlot;
		}

	}

}
