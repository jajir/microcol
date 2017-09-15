package org.microcol.gui.util;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
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

	final static String KEY_FROM_EUROPE_SHOP = "FromEuropeShop";

	final static String KEY_FROM_COLONY_FIELD = "FromColonyField";

	final static String KEY_FROM_OUTSIDE_COLONY = "FromOutsideColony";

	final static String KEY_FROM_CONSTRUCTION_SLOT = "FromConstructionSlot";

	final static String KEY_FROM_COLONY_WAREHOUSE = "FromColonyWarehouse";

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
		final int amount = read(parts[2]);
		final GoodType goodType = GoodType.valueOf(parts[1]);
		final GoodAmount goodAmount = new GoodAmount(goodType, amount);
		TransferFrom transferFrom = tryReadTransferFromCargoSlot(3);
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromEuropeShop(3);
		}
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromColonyWarehouse(3);
		}
		return new GoodTransfer(goodAmount, transferFrom);
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
		TransferFrom transferFrom = tryReadTransferFromCargoSlot(2);
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromEuropePortPier(2);
		}
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromColonyField(2);
		}
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromOutsideColony(2);
		}
		if (transferFrom == null) {
			transferFrom = tryReadTransferFromConstructionSlot(2);
		}
		return new UnitTransfer(unit, transferFrom);
	}

	private TransferFromCargoSlot tryReadTransferFromCargoSlot(int fromIndex) {
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

	private TransferFromEuropePier tryReadTransferFromEuropePortPier(int fromIndex) {
		if (parts.length < fromIndex + 1) {
			return null;
		}
		if (!KEY_FROM_EUROPE_PORT_PIER.equals(parts[fromIndex + 0])) {
			return null;
		}
		return new TransferFromEuropePier();
	}

	private TransferFromOutsideColony tryReadTransferFromOutsideColony(int fromIndex) {
		if (parts.length < fromIndex + 1) {
			return null;
		}
		if (!KEY_FROM_OUTSIDE_COLONY.equals(parts[fromIndex + 0])) {
			return null;
		}
		return new TransferFromOutsideColony();
	}

	private TransferFromColonyWarehouse tryReadTransferFromColonyWarehouse(int fromIndex) {
		if (parts.length < fromIndex + 1) {
			return null;
		}
		if (!KEY_FROM_COLONY_WAREHOUSE.equals(parts[fromIndex + 0])) {
			return null;
		}
		return new TransferFromColonyWarehouse();
	}

	private TransferFromConstructionSlot tryReadTransferFromConstructionSlot(int fromIndex) {
		if (parts.length < fromIndex + 1) {
			return null;
		}
		if (!KEY_FROM_CONSTRUCTION_SLOT.equals(parts[fromIndex + 0])) {
			return null;
		}
		return new TransferFromConstructionSlot();
	}

	private TransferFromColonyField tryReadTransferFromColonyField(int fromIndex) {
		if (parts.length < fromIndex + 3) {
			return null;
		}
		if (!KEY_FROM_CONSTRUCTION_SLOT.equals(parts[fromIndex + 0])) {
			return null;
		}
		if (tryRead(parts[fromIndex + 1]) == null) {

		}
		if (tryRead(parts[fromIndex + 2]) == null) {

		}
		return new TransferFromColonyField(Location.of(tryRead(parts[fromIndex + 1]), tryRead(parts[fromIndex + 2])));
	}

	private TransferFromEuropeShop tryReadTransferFromEuropeShop(int fromIndex) {
		if (parts.length < fromIndex + 1) {
			return null;
		}
		if (!KEY_FROM_EUROPE_SHOP.equals(parts[fromIndex + 0])) {
			return null;
		}
		return new TransferFromEuropeShop();
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

		public ParsingResult filterGoods(final Predicate<GoodAmount> filter) {
			if (goodTransfer == null) {
				return this;
			} else {
				if (filter.test(goodTransfer.goodAmount)) {
					return this;
				} else {
					return new ParsingResult(unitTransfer, null);
				}
			}
		}

		public ParsingResult filterTransferFrom(final Predicate<Optional<TransferFrom>> filter) {
			if (isEmpty()) {
				return this;
			} else {
				if (filter.test(getTransferFrom())) {
					return this;
				} else {
					return new ParsingResult(null, null);
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

		public Optional<GoodAmount> getGoods() {
			if (goodTransfer == null) {
				return Optional.empty();
			} else {
				return Optional.of(goodTransfer.goodAmount);
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

		public ParsingResult tryReadGood(final BiConsumer<GoodAmount, Optional<TransferFrom>> consumer) {
			if (goodTransfer != null) {
				consumer.accept(goodTransfer.goodAmount, goodTransfer.getTransferFrom());
			}
			return this;
		}

		public ParsingResult readGood(final BiConsumer<GoodAmount, Optional<TransferFrom>> consumer) {
			if (goodTransfer == null) {
				throw new IllegalStateException("Unable to read good from string '" + originalString + "'");
			} else {
				consumer.accept(goodTransfer.goodAmount, goodTransfer.getTransferFrom());
				return this;
			}
		}

		public boolean isEmpty() {
			return unitTransfer == null && goodTransfer == null;
		}
		
		private Optional<TransferFrom> getTransferFrom() {
			if (unitTransfer == null) {
				if (goodTransfer == null) {
					throw new IllegalArgumentException();
				} else {
					return goodTransfer.getTransferFrom();
				}
			} else {
				return unitTransfer.getTransferFrom();
			}
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

		private final GoodAmount goodAmount;

		GoodTransfer(final GoodAmount goodAmount, final TransferFrom transferFrom) {
			super(transferFrom);
			this.goodAmount = goodAmount;
		}

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(KEY_GOODS);
			buff.append(SEPARATOR);
			buff.append(goodAmount.getGoodType().name());
			buff.append(SEPARATOR);
			buff.append(goodAmount.getAmount());
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

	public static class TransferFromColonyField implements TransferFrom {

		private final Location fieldDirection;
		
		TransferFromColonyField(final Location fieldDirection) {
			this.fieldDirection = Preconditions.checkNotNull(fieldDirection);
		}

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_COLONY_FIELD);
			buff.append(SEPARATOR);
			buff.append(fieldDirection.getX());
			buff.append(SEPARATOR);
			buff.append(fieldDirection.getY());
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

	/**
	 * Unit was taken from Europe shop.
	 */
	public static class TransferFromEuropeShop implements TransferFrom {

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_EUROPE_SHOP);
		}

	}

	/**
	 * Unit was taken from outside colony.
	 */
	public static class TransferFromOutsideColony implements TransferFrom {

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_OUTSIDE_COLONY);
		}

	}

	/**
	 * Unit was taken from construction slot.
	 */
	public static class TransferFromConstructionSlot implements TransferFrom {

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_CONSTRUCTION_SLOT);
		}

	}

	/**
	 * Unit was taken from colony warehouse.
	 */
	public static class TransferFromColonyWarehouse implements TransferFrom {

		@Override
		public void writeTo(final StringBuilder buff) {
			buff.append(SEPARATOR);
			buff.append(KEY_FROM_COLONY_WAREHOUSE);
		}

	}
}
