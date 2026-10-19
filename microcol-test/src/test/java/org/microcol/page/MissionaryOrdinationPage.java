package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.microcol.model.Colony;
import org.microcol.model.ConstructionType;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Player gestures and model observations for missionary ordination on the
 * European pier and outside an owned colony. The fixture is the F19 scenario
 * patched through the existing JSON replacement path.
 */
public final class MissionaryOrdinationPage extends AbstractScreen {

    private static final String SOURCE = "src/test/scenarios/F19-europe-equipment.microcol";

    private static final Location COLONY = Location.of(24, 16);

    /** Colonists standing outside the colony, ordinary with 20 tools. */
    public static final int ORDINARY = 200;

    /** A Jesuit standing outside the colony. */
    public static final int JESUIT = 201;

    /** An ordinary colonist that already is a missionary. */
    public static final int MISSIONARY = 202;

    /** An ordinary colonist without equipment or role. */
    public static final int PLAIN = 203;

    public MissionaryOrdinationPage(final TestContext context) {
        super(context);
    }

    /**
     * Replace the loaded game with F19 plus four colonists outside the colony.
     *
     * @param religion new religious building replacing the chapel, or
     *                 <code>null</code> to keep the chapel
     * @param brebeuf  whether the Dutch Congress already has Jean de Brebeuf
     */
    public void loadFixture(final ConstructionType religion,
            final boolean brebeuf) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(Path.of(SOURCE)),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        if (religion != null) {
            for (final JsonElement colony : model.getAsJsonArray("colonies")) {
                for (final JsonElement building : colony.getAsJsonObject()
                        .getAsJsonArray("constructions")) {
                    final JsonObject entry = building.getAsJsonObject();
                    if (entry.get("type").getAsString().equals("CHAPEL")) {
                        entry.addProperty("type", religion.name());
                    }
                }
            }
        }
        if (brebeuf) {
            for (final JsonElement entry : model.getAsJsonObject("gameManager")
                    .getAsJsonArray("players")) {
                final JsonObject player = entry.getAsJsonObject();
                if (player.get("name").getAsString().equals("Dutch")) {
                    final JsonArray members = new JsonArray();
                    members.add(Founder.JEAN_DE_BREBEUF.name());
                    player.getAsJsonObject("congress").add("members", members);
                }
            }
        }
        final JsonArray units = model.getAsJsonArray("units");
        units.add(outsideColonist(ORDINARY, "COLONIST", false, 20));
        units.add(outsideColonist(JESUIT, "JESUIT_MISSIONARY", false, 0));
        units.add(outsideColonist(MISSIONARY, "COLONIST", true, 0));
        units.add(outsideColonist(PLAIN, "COLONIST", false, 0));
        final Path file = Files.createTempFile("missionary-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private JsonObject outsideColonist(final int id, final String type,
            final boolean missionary, final int tools) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", COLONY.getX());
        location.addProperty("y", COLONY.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "east");
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", missionary);
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", tools);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    /** Open the ordination dialog from the unit's menu on the European pier. */
    public void openFromPier(final int unitId, final String menuText) {
        getRobot().clickOn("#europe-pier-unit-" + unitId).clickOn(menuText);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Open the ordination dialog from the outside-colony unit's menu. */
    public void openFromColony(final int unitId, final String menuText) {
        getRobot().clickOn("#colonyUnit-" + unitId, MouseButton.SECONDARY)
                .clickOn(menuText);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("missionaryConfirm");
    }

    public void confirmWithEnter() {
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("missionaryCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * A competing action: the colonist takes a town hall job while the dialog
     * built from the earlier preview is still open.
     */
    public void assignToTownHall(final int unitId) {
        getRobot().interact(() -> {
            final Colony colony = getModel().getColonyAt(COLONY).orElseThrow();
            getModel().getUnitById(unitId)
                    .placeToColonyStructureSlot(colony
                            .getConstructionByType(ConstructionType.TOWN_HALL)
                            .getFirstEmptySlot().orElseThrow());
        });
    }

    public void verifyChoice(final String caption, final boolean canConfirm,
            final String message) {
        assertTrue(isCssIdVisible("missionaryDialog"));
        assertEquals(caption, getLabeledById("missionaryCaption").getText());
        assertEquals(!canConfirm,
                getButtonById("missionaryConfirm").isDisabled());
        assertEquals(message, getLabeledById("missionaryMessage").getText());
    }

    /**
     * Compare the registered model colonist: same identity and owner, the
     * missionary role and effective expertise, equipment, exact movement and
     * place.
     */
    public void verifyMissionary(final int unitId, final boolean missionary,
            final boolean effectiveExpert, final UnitType type, final int tools,
            final int movementThirds, final Class<?> placeType) {
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        assertEquals(unitId, unit.getId());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(type, unit.getType());
        assertEquals(missionary, unit.isMissionary());
        assertEquals(effectiveExpert, unit.hasEffectiveMissionaryExpertise());
        assertEquals(tools, unit.getTools());
        assertEquals(movementThirds, unit.getMovementThirds());
        assertTrue(placeType.isInstance(unit.getPlace()),
                "Expected " + placeType.getSimpleName());
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(candidate -> candidate.getId() == unitId).count());
    }

    public void verifyGold(final int expectedGold) {
        assertEquals(expectedGold,
                getModel().getPlayerByName("Dutch").getGold());
    }

    public void verifyBrebeufMember(final boolean expectedMember) {
        assertEquals(expectedMember, getModel().getPlayerByName("Dutch")
                .getCongressMembers().contains(Founder.JEAN_DE_BREBEUF));
    }
}
