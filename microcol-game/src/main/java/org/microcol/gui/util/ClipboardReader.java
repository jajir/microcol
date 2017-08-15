package org.microcol.gui.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

	private final String[] parts;

	private final Dragboard db;

	public static ClipboardReader make(final Model model, final Dragboard db) {
		return new ClipboardReader(model, db);
	}

	private ClipboardReader(final Model model, final Dragboard db) {
		this.model = Preconditions.checkNotNull(model);
		this.db = Preconditions.checkNotNull(db);
		Preconditions.checkNotNull(db.getString(), "Clipboard string is null");
		Preconditions.checkState(!db.getString().isEmpty(), "Clipboard string is empty");
		parts = db.getString().split(ClipboardWritter.SEPARATOR);
	}

	public ClipboardReader readUnit(Consumer<Unit> consumer) {
		Preconditions.checkState(ClipboardWritter.KEY_UNIT.equals(parts[0]),
				"It's not content type unit. Current type is (" + parts[0] + ") in string (" + db.getString() + ")");
		Preconditions.checkState(parts.length > 1, "Invalid string (" + db.getString() + ") length");
		final Unit unit = model.getUnitById(read(parts[1]));
		consumer.accept(unit);
		return this;
	}

	//FIXME JJ add some test
	public Optional<Unit> filterUnit(final Predicate<Unit> filter) {
		if (!ClipboardWritter.KEY_UNIT.equals(parts[0])) {
			return Optional.empty();
		}
		if (parts.length <= 1) {
			return Optional.empty();
		}
		if (tryRead(parts[1]) == null) {
			return Optional.empty();
		}
		final Unit unit = model.getUnitById(read(parts[1]));
		if (filter.test(unit)) {
			return Optional.of(unit);
		} else {
			return Optional.empty();
		}
	}

	public ClipboardReader readGood(final Consumer<GoodAmmount> consumer) {
		Preconditions.checkState(ClipboardWritter.KEY_GOODS.equals(parts[0]),
				"It's not content type unit. Current type is (" + parts[0] + ") in string (" + db.getString() + ")");
		Preconditions.checkState(parts.length > 2, "Invalid string (" + db.getString() + ") length");
		final GoodType goodType = GoodType.valueOf(parts[1]);
		final int ammount = read(parts[2]);
		consumer.accept(new GoodAmmount(goodType, ammount));
		return this;
	}

	private int read(final String num) {
		try {
			return Integer.valueOf(num);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("String (" + num + ") is not a number.");
		}
	}

	private Integer tryRead(final String num) {
		try {
			return Integer.valueOf(num);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
