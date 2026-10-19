package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.MovementPoints;
import org.microcol.model.UnitType;
import org.microcol.model.unit.Ship;

/** Observe diplomatic state after the actual privateer controls. */
public final class PrivateerPage extends AbstractScreen {
    public PrivateerPage(TestContext context) {
        super(context);
    }

    public void loadCounterattackFixture() throws Exception {
        Gson gson = new Gson();
        JsonObject game = gson.fromJson(
                Files.readString(
                        Path.of("src/test/scenarios/F56-privateer.microcol")),
                JsonObject.class);
        for (var element : game.getAsJsonObject("model")
                .getAsJsonArray("units")) {
            JsonObject unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == 0) {
                unit.addProperty("type", "FRIGATE");
                unit.addProperty("movementThirds", MovementPoints.ofPoints(6));
                unit.getAsJsonObject("cargo").getAsJsonArray("slots")
                        .add(new JsonObject());
                unit.getAsJsonObject("cargo").getAsJsonArray("slots")
                        .add(new JsonObject());
            } else if (unit.get("id").getAsInt() == 6) {
                unit.addProperty("type", "PRIVATEER");
                unit.addProperty("movementThirds", MovementPoints.ofPoints(8));
                JsonArray slots = new JsonArray();
                JsonObject goods = new JsonObject();
                goods.addProperty("goodsType", "ORE");
                goods.addProperty("amount", 7);
                slots.add(goods);
                JsonObject passenger = new JsonObject();
                passenger.addProperty("unitId", 8);
                slots.add(passenger);
                unit.getAsJsonObject("cargo").add("slots", slots);
            }
        }
        Path file = Files.createTempFile("privateer-counterattack-",
                ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public void verifyPeacefulVictory(UnitType attackerType, int ore) {
        assertFalse(getModel().areHostile(getModel().getPlayerByName("Dutch"),
                getModel().getPlayerByName("France")));
        Ship ship = (Ship) getModel().getUnitById(0);
        assertEquals(attackerType, ship.getType());
        assertEquals(0, ship.getActionPoints());
        assertFalse(ship.isDamaged());
        assertEquals(Goods.of(GoodsType.ORE, ore),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(9, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertTrue(getModel().tryGetUnitById(6).isEmpty());
        assertTrue(getModel().tryGetUnitById(8).isEmpty());
    }
}
