package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;

/** Saved promotion fixtures and explicit rank/equipment observations. */
public final class MilitaryPromotionPage extends AbstractScreen {
    public MilitaryPromotionPage(TestContext context) {
        super(context);
    }

    public void loadWashingtonFixture() throws Exception {
        Gson gson = new Gson();
        JsonObject game = gson.fromJson(
                Files.readString(Path.of(
                        "src/test/scenarios/F57-military-promotion.microcol")),
                JsonObject.class);
        for (var entry : game.getAsJsonObject("model")
                .getAsJsonObject("gameManager").getAsJsonArray("players")) {
            JsonObject player = entry.getAsJsonObject();
            if (player.get("name").getAsString().equals("Dutch")) {
                JsonObject congress = player.getAsJsonObject("congress");
                congress.addProperty("activated", true);
                congress.addProperty("generation", 2);
                congress.addProperty("bells", 0);
                JsonArray members = new JsonArray();
                members.add("GEORGE_WASHINGTON");
                congress.add("members", members);
                JsonArray candidates = new JsonArray();
                for (String name : new String[] { "JOHN_PAUL_JONES",
                        "THOMAS_JEFFERSON", "JAKOB_FUGGER", "WILLIAM_PENN",
                        "FERDINAND_MAGELLAN" })
                    candidates.add(name);
                congress.add("candidates", candidates);
            }
        }
        Path file = Files.createTempFile("military-washington-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public void prepareNextReview(int winner) {
        getRobot().interact(() -> {
            ((UnitFreeColonist) getModel().getUnitById(winner))
                    .setActionPoints(1);
            ((UnitFreeColonist) getModel().getUnitById(6)).setHoldingGuns(true);
        });
    }

    public void inspect(Location at) {
        new FogPage(getContext()).inspect(at);
    }

    public void verifyRank(int id, UnitType specialty, boolean veteran,
            boolean mounted) {
        UnitFreeColonist unit = (UnitFreeColonist) getModel().getUnitById(id);
        assertEquals(specialty, unit.getType());
        assertEquals(veteran, unit.isVeteran());
        assertEquals(mounted, unit.isMounted());
        assertTrue(unit.isHoldingGuns());
        assertEquals("Dutch", unit.getOwner().getName());
        assertEquals(0, unit.getActionPoints());
    }

    public void verifyVisibleRank(int id, String text) {
        assertEquals(text, getLabeledById("unitVeteran-" + id).getText());
        javafx.scene.control.Labeled label = getLabeledById(
                "unitVeteran-" + id);
        assertTrue(label.isVisible());
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()),
                "The full veteran status must fit in its label");
    }

    public void verifyPromotionFeedbackVisible() {
        javafx.scene.control.Labeled label = getLabeledById("statusBarLabel");
        assertTrue(label.getText().contains("\n"));
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()),
                "Both combat outcome lines must fit in the status bar");
        assertTrue(
                label.localToScene(label.getBoundsInLocal()).getMaxY() <= label
                        .getScene().getHeight(),
                "Promotion must remain inside the scene");
    }
}
