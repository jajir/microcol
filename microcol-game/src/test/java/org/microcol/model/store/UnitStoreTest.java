package org.microcol.model.store;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class verify storing of UnitPo to json.
 */
public class UnitStoreTest {

    private final Logger logger = LoggerFactory.getLogger(UnitStoreTest.class);

    private final Gson gson = new GsonBuilder()
	    .registerTypeAdapter(ConstructionType.class, new GsonConstructionTypeAdapter())
	    .registerTypeAdapter(GoodType.class, new GsonGoodTypeAdapter())
	    .registerTypeAdapter(TerrainType.class, new GsonTerrainTypeAdapter())
	    .registerTypeAdapter(UnitType.class, new GsonUnitTypeAdapter())
	    .registerTypeAdapter(UnitActionPo.class, new UnitActionPoAdapter()).setPrettyPrinting().create();

    @Test
    public void test_storeUnit_noAction() throws Exception {
        final String json = gson.toJson(makeUnit());

        logger.info(json);

        UnitPo u = gson.fromJson(json, UnitPo.class);

        assertEquals(UnitActionType.noAction, u.getAction().getType());
    }

    @Test
    public void test_storeUnit_plowField() throws Exception {
        final UnitActionPlowFieldPo pf = new UnitActionPlowFieldPo();
        pf.setRemainingTurns(3);
        final UnitPo o = makeUnit();
        o.setAction(pf);
        final String json = gson.toJson(o);

        UnitPo u = gson.fromJson(json, UnitPo.class);

        assertEquals(UnitActionType.plowField, u.getAction().getType());
        assertEquals(3, ((UnitActionPlowFieldPo) u.getAction()).getRemainingTurns());
    }

    private UnitPo makeUnit() {
        UnitPo u = new UnitPo();
        u.setId(12);
        u.setAvailableMoves(2);
        u.setCargo(new CargoPo());
        u.setOwnerId("Dutch");
        u.setType(UnitType.COLONIST);
        u.setPlaceMap(new PlaceMapPo());
        u.getPlaceMap().setLocation(Location.of(2, 92));
        u.setAction(new UnitActionNoActionPo());

        return u;
    }

}
