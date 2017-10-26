package org.microcol.model;

import org.microcol.model.store.CalendarPo;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

public class ModelBuilder {

	private ModelPo modelPo;

	@Deprecated
	private EuropeBuilder europeBuilder;

	public ModelBuilder addUnit(final UnitPo unit) {
		Preconditions.checkNotNull(unit);
		if (modelPo.getUnits().contains(unit)) {
			throw new IllegalArgumentException("Unit was already registered. Unit: " + unit);
		}
		modelPo.getUnits().add(unit);
		return this;
	}

	public EuropeBuilder getEuropeBuilder() {
		if (europeBuilder == null) {
			europeBuilder = new EuropeBuilder(this);
			return europeBuilder;
		} else {
			throw new IllegalStateException("Europe is already build");
		}
	}

	public ModelBuilder setCalendar(final int startYear, final int endYear) {
		Preconditions.checkNotNull(modelPo, "map was not initialized");
		CalendarPo calendarPo = new CalendarPo();
		calendarPo.setStartYear(startYear);
		calendarPo.setCurrentYear(startYear);
		calendarPo.setEndYear(endYear);
		modelPo.setCalendar(calendarPo);
		return this;
	}

	public ModelBuilder setMap(final String fileName) {
		final ModelDao modelDao = new ModelDao();
		modelPo = modelDao.loadPredefinedModel(fileName);
		return this;
	}

	public PlayerBuilder addPlayer(final String name) {
		return new PlayerBuilder(this, name);
	}

	public ModelBuilder addUnit(final UnitType type, final String ownerName, final Location location) {
		UnitPo unit = new UnitPo();
		unit.setId(IdManager.nextId());
		unit.setAvailableMoves(0);
		unit.setOwnerId(ownerName);
		unit.setType(type);
		unit.setPlaceMap(new PlaceMapPo());
		unit.getPlaceMap().setLocation(location);
		modelPo.getUnits().add(unit);

		return this;
	}

	PlayerPo getPlayer(final String name) {
		return modelPo.getPlayerByName(name);
	}

	public Model build() {
		Preconditions.checkNotNull(europeBuilder == null, "Europe was not builded");
		return Model.make(modelPo);
	}

	public UnitBuilder makeUnitBuilder() {
		return new UnitBuilder(modelPo);
	}

	/**
	 * @return the modelPo
	 */
	ModelPo getModelPo() {
		return modelPo;
	}
}
