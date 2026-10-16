package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.microcol.model.*;
import com.google.gson.*;

/**
 * Religious output and real immigration credit share the colony calculation.
 */
public final class ReligiousProductionPage extends AbstractScreen {
    public ReligiousProductionPage(TestContext context) {
        super(context);
    }

    public void loadPennFixture() throws Exception {
        Gson gson = new Gson();
        JsonObject game = gson.fromJson(Files.readString(Path
                .of("src/test/scenarios/F58-religious-production.microcol")),
                JsonObject.class);
        for (JsonElement entry : game.getAsJsonObject("model")
                .getAsJsonObject("gameManager").getAsJsonArray("players")) {
            JsonObject player = entry.getAsJsonObject();
            if (player.get("name").getAsString().equals("Dutch")) {
                JsonObject congress = player.getAsJsonObject("congress");
                congress.addProperty("generation", 2);
                JsonArray members = new JsonArray();
                members.add("WILLIAM_PENN");
                congress.add("members", members);
                JsonArray candidates = congress.getAsJsonArray("candidates");
                candidates.set(3, new JsonPrimitive("JUAN_DE_SEPULVEDA"));
            }
        }
        Path file = Files.createTempFile("religious-penn-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public void prepareCathedralMaterials() {
        getRobot().interact(() -> {
            var warehouse = getModel().getColonyById(0).orElseThrow()
                    .getWarehouse();
            warehouse.removeGoods(warehouse.getGoods(GoodsType.HAMMERS));
            warehouse.addGoods(Goods.of(GoodsType.HAMMERS, 173));
            warehouse.removeGoods(warehouse.getGoods(GoodsType.TOOLS));
            warehouse.addGoods(Goods.of(GoodsType.TOOLS, 100));
        });
    }

    public void verifyCrosses(ColonyScreen screen, int production,
            int credited) {
        javafx.scene.Node building = getNodeFinder().lookup("#id_church")
                .query();
        for (javafx.scene.Node parent = building; parent != null; parent = parent
                .getParent()) {
            assertTrue(parent.isVisible(),
                    "Religious building must be visible through every ancestor");
        }
        screen.verifyCrossProduction(production);
        screen.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, credited));
        assertEquals(credited,
                getModel().getPlayerByName("Dutch").getImmigrationCrosses());
    }
}
