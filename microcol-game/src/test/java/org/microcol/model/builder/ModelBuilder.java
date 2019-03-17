package org.microcol.model.builder;

import org.microcol.model.GameOverEvaluator;
import org.microcol.model.IdManager;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.UnitType;
import org.microcol.model.store.CalendarPo;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ModelBuilder {

    private ModelPo modelPo;

    private final IdManager idManager = new IdManager(0);

    @Deprecated
    private EuropeBuilder europeBuilder;

    public ModelBuilder addUnit(final UnitPo unit) {
        Preconditions.checkNotNull(unit);
        if (modelPo.getUnits().contains(unit)) {
            throw new IllegalArgumentException("Unit was already registered. Unit: " + unit);
        }
        modelPo.addUnit(unit);
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
        calendarPo.setEndYear(endYear);
        calendarPo.setNumberOfPlayedTurns(0);
        modelPo.setCalendar(calendarPo);
        return this;
    }

    public ModelBuilder setMap(final String fileName) {
        final ModelDao modelDao = new ModelDao();
        modelPo = modelDao.loadFromClassPath(fileName);
        return this;
    }

    public PlayerBuilder addPlayer(final String name) {
        return new PlayerBuilder(this, name, idManager);
    }

    public ModelBuilder addUnit(final UnitType type, final String ownerName,
            final Location location) {
        UnitPo unit = new UnitPo();
        unit.setId(idManager.nextId());
        unit.setAvailableMoves(0);
        unit.setOwnerId(ownerName);
        unit.setType(type);
        unit.setPlaceMap(new PlaceMapPo());
        unit.getPlaceMap().setLocation(location);
        unit.setAction(new UnitActionNoActionPo());
        modelPo.addUnit(unit);

        return this;
    }
    
    public ModelBuilder startGame(final String currentPlayerName){
        modelPo.getGameManager().setGameStarted(true);
        modelPo.getGameManager().setCurrentPlayer(currentPlayerName);
        return this;
    }

    PlayerPo getPlayer(final String name) {
        return modelPo.getPlayerByName(name);
    }

    public Model build() {
        Preconditions.checkNotNull(europeBuilder == null, "Europe was not builded");
        return Model.make(modelPo,
                Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    public UnitBuilder makeUnitBuilder() {
        return new UnitBuilder(modelPo, idManager);
    }

    /**
     * @return the modelPo
     */
    ModelPo getModelPo() {
        return modelPo;
    }
}
