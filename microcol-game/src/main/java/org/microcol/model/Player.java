package org.microcol.model;

import java.util.List;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Player {
	private Model model;

	private final String name;
	private final boolean computer;

	Player(final String name, final boolean computer) {
		this.name = Preconditions.checkNotNull(name);
		this.computer = computer;
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	public String getName() {
		return name;
	}

	public boolean isComputer() {
		return computer;
	}

	public boolean isHuman() {
		return !computer;
	}

	public List<Unit> getUnits() {
		return model.getUnits(this);
	}

	public Map<Location, List<Unit>> getUnitsAt() {
		return model.getUnitsAt(this);
	}

	public List<Unit> getUnitsAt(final Location location) {
		return model.getUnitsAt(this, location);
	}

	public List<Unit> getEnemyUnits() {
		return model.getEnemyUnits(this);
	}

	public Map<Location, List<Unit>> getEnemyUnitsAt() {
		return model.getEnemyUnitsAt(this);
	}

	public List<Unit> getEnemyUnitsAt(final Location location) {
		return model.getEnemyUnitsAt(this, location);
	}

	void startTurn() {
		getUnits().forEach(unit -> unit.startTurn());
	}

	public void endTurn() {
		model.checkGameRunning();
		model.checkCurrentPlayer(this);
		model.endTurn();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (!(object instanceof Player)) {
			return false;
		}

		Player player = (Player) object;

		return name.equals(player.name) && computer == player.computer;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name)
			.add("computer", computer)
			.toString();
	}

	void save(final JsonGenerator generator) {
		generator.writeStartObject()
			.write("name", name)
			.write("computer", computer)
			.writeEnd();
	}

	static Player load(final JsonParser parser) {
		// START_OBJECT or END_ARRAY
		if (parser.next() == JsonParser.Event.END_ARRAY) {
			return null;
		}
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final String name = parser.getString();
		parser.next(); // KEY_NAME
		final boolean computer = parser.next() == JsonParser.Event.VALUE_TRUE; // VALUE_TRUE or VALUE_FALSE
		parser.next(); // END_OBJECT

		return new Player(name, computer);
	}
}
