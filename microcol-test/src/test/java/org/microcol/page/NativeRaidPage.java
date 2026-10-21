package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.microcol.model.ConstructionType;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitNativeBrave;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Native raids on the saved native position (R-0156). The initial position is a
 * current-schema fixture derived from the saved scenario: the Aztecs' tribal
 * anger toward the Dutch is set as given, and the saved combat random state
 * decides the quoted battle. Nothing in the running model is changed by this
 * page; it reads the model only to verify.
 * <p>
 * Quotes (R-0173): a brave attacks with its role base 1 and +50%; the raid
 * ambush comes from the <em>defender's</em> tile, which is plain grass here, so
 * it is 0 and the raid total is 1.5. An armed soldier defends with base 2. A
 * full gun on the colony tile defends with base 5, +100% for the stockade and
 * +75% against a native raid, which is 13.75.
 * </p>
 */
public final class NativeRaidPage extends AbstractScreen {

    /** Tribal anger that makes the Aztec brave hostile to the Dutch. */
    private static final double HOSTILE_ANGER = 80;

    /** Homeless Aztec survivor, removed so that only one brave can raid. */
    private static final int SURVIVOR_ID = 201;

    /** Brave on grass against grass: base 1 with +50% attack, no ambush. */
    private static final double RAID_ATTACK = 1.5;

    /** Unfortified armed soldier on grass. */
    private static final double SOLDIER_DEFENSE = 2;

    /** Full gun in a stockaded colony against a raid: 5 * (1 + 1 + 0.75). */
    private static final double GUN_DEFENSE = 13.75;

    /**
     * Width of the saved first draw above the raid odds that keeps the gun's
     * victory sensitive to every bonus: a draw this close is lost for the brave
     * only when 75% and 100% are both applied and no hill ambush is.
     */
    private static final double DRAW_NEAR = 0.003;
    private static final double DRAW_FAR = 0.03;

    public NativeRaidPage(final TestContext context) {
        super(context);
    }

    /**
     * Load the scenario with the given Aztec anger toward the Dutch, one more
     * armed Dutch soldier, and a saved draw that lets the brave win the raid.
     *
     * @param source     scenario file with the native position
     * @param aztecAnger tribal anger of the Aztecs toward the Dutch, 0..100
     * @param soldierId  unused unit ID of the new soldier
     * @param soldierAt  land square beside the Aztec brave
     */
    public void loadFixture(final File source, final double aztecAnger,
            final int soldierId, final Location soldierAt) throws Exception {
        final JsonObject game = read(source);
        final JsonObject model = game.getAsJsonObject("model");
        setAztecAnger(model, aztecAnger);
        model.getAsJsonArray("units").add(soldier(soldierId, soldierAt));
        model.addProperty("combatRandomState", NativeCombatPage
                .stateFor(RAID_ATTACK / (RAID_ATTACK + SOLDIER_DEFENSE), true));
        replace(game);
    }

    /**
     * Load the scenario with a full gun inside a stockade on the colony square,
     * hostile Aztecs, and a saved draw that lets the gun hold. The draw lies
     * just above the correct raid odds, so a missing 75%, 100% or a wrongly
     * applied hill ambush would let the brave win instead. The homeless
     * survivor is removed, so that only the hill brave can raid the colony.
     *
     * @param source   scenario file with the native position
     * @param gunId    unused unit ID of the new gun
     * @param colonyAt square of the colony beside the hill brave
     */
    public void loadFortifiedGunFixture(final File source, final int gunId,
            final Location colonyAt) throws Exception {
        final JsonObject game = read(source);
        final JsonObject model = game.getAsJsonObject("model");
        setAztecAnger(model, HOSTILE_ANGER);
        final Iterator<JsonElement> units = model.getAsJsonArray("units")
                .iterator();
        while (units.hasNext()) {
            if (units.next().getAsJsonObject().get("id")
                    .getAsInt() == SURVIVOR_ID) {
                units.remove();
            }
        }
        final JsonObject stockade = new JsonObject();
        stockade.addProperty("type", "STOCKADE");
        // The same three empty slots a saved construction gets by default, as
        // in
        // the model's own fortified-gun fixture.
        final JsonArray slots = new JsonArray();
        for (int i = 0; i < 3; i++) {
            slots.add(new JsonObject());
        }
        stockade.add("slots", slots);
        model.getAsJsonArray("colonies").get(0).getAsJsonObject()
                .getAsJsonArray("constructions").add(stockade);
        model.getAsJsonArray("units").add(gun(gunId, colonyAt));
        model.addProperty("combatRandomState", stateAboveGunOdds());
        replace(game);
    }

    private static long stateAboveGunOdds() {
        final double odds = RAID_ATTACK / (RAID_ATTACK + GUN_DEFENSE);
        for (long state = 0;; state++) {
            final double first = NativeCombatPage.draw(state, 1);
            if (first >= odds + DRAW_NEAR && first <= odds + DRAW_FAR) {
                return state;
            }
        }
    }

    private static JsonObject read(final File source) throws Exception {
        return new Gson().fromJson(Files.readString(source.toPath()),
                JsonObject.class);
    }

    private void replace(final JsonObject game) throws Exception {
        final Path fixture = Files.createTempFile("native-raid-", ".microcol");
        try {
            Files.writeString(fixture, new Gson().toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static void setAztecAnger(final JsonObject model,
            final double value) {
        for (final JsonElement entry : model
                .getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString())
                    && "DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", value);
            }
        }
    }

    private static JsonObject unit(final int id, final String type,
            final Location at) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("mounted", false);
        return unit;
    }

    private static JsonObject soldier(final int id, final Location at) {
        final JsonObject unit = unit(id, "COLONIST", at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("holdingGuns", true);
        return unit;
    }

    private static JsonObject gun(final int id, final Location at) {
        final JsonObject unit = unit(id, "ARTILLERY", at);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("artilleryDamaged", false);
        return unit;
    }

    /**
     * Before the native turn: the inputs from which the shared model derives
     * the raid quote on the colony square are exactly the documented ones. The
     * brave is a plain brave of base 1 beside the colony; the colony tile is
     * grass without trees, so the raid ambush is 0; the colony holds a stockade
     * and no stronger fortification, so its bonus is 100%; and the gun is a
     * full gun of the colony's owner on the colony tile without a personal
     * fortify order, which leaves the native-raid 75% as its only modifier.
     */
    public void verifyRaidInputs(final int braveId, final int gunId,
            final Location colonyAt) {
        final UnitNativeBrave brave = (UnitNativeBrave) getModel()
                .getUnitById(braveId);
        assertEquals(UnitNativeBrave.Role.BRAVE, brave.getRole());
        assertEquals(1.0, brave.getMilitaryStrenght());
        assertTrue(brave.getLocation().isNeighbor(colonyAt));
        final Terrain tile = getModel().getMap().getTerrainAt(colonyAt);
        assertEquals(TerrainType.GRASSLAND, tile.getTerrainType());
        assertFalse(tile.isHasTrees());
        final org.microcol.model.Colony colony = getModel()
                .getColonyAt(colonyAt).orElseThrow();
        assertTrue(colony.wasConstructionBuilded(ConstructionType.STOCKADE));
        assertFalse(colony.wasConstructionBuilded(ConstructionType.FORT));
        assertFalse(colony.wasConstructionBuilded(ConstructionType.FORTRESS));
        final UnitArtillery gun = (UnitArtillery) getModel().getUnitById(gunId);
        assertFalse(gun.isDamaged());
        assertEquals(colonyAt, gun.getLocation());
        assertSame(colony.getOwner(), gun.getOwner());
        assertFalse(getModel().isFortified(gun));
    }

    /**
     * The hill brave raided the colony square and was destroyed with its
     * equipment; the stockaded gun held at full strength on its square.
     */
    public void verifyGunDefendedAgainstRaid(final int braveId, final int gunId,
            final Location colonyAt) throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(
                getModel().getAllUnits().stream()
                        .noneMatch(unit -> unit.getId() == braveId),
                "The raiding brave survived the fortified gun");
        final UnitArtillery gun = (UnitArtillery) getModel().getUnitById(gunId);
        assertEquals(colonyAt, gun.getLocation());
        assertFalse(gun.isDamaged(), "The fortified gun was damaged");
    }

    /**
     * The Dutch soldier was raided by the brave and lost its muskets; it was
     * not destroyed, and it stands where it stood. The Dutch turn prepared the
     * soldier again after the natives acted, so its movement proves nothing;
     * the brave has spent its own movement and stays on its square.
     */
    public void verifySoldierLostMusketsToRaid(final int soldierId,
            final Location soldierAt, final int braveId, final Location braveAt)
            throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        final UnitFreeColonist soldier = (UnitFreeColonist) getModel()
                .getUnitById(soldierId);
        assertEquals("Dutch", soldier.getOwner().getName());
        assertEquals(soldierAt, soldier.getLocation());
        assertFalse(soldier.isHoldingGuns(),
                "The raided soldier kept its muskets");
        final UnitNativeBrave brave = (UnitNativeBrave) getModel()
                .getUnitById(braveId);
        assertEquals(braveAt, brave.getLocation());
        assertEquals(0, brave.getActionPoints(),
                "The raiding brave did not spend its movement");
    }

    /** The calm native turn left the soldier armed on its square. */
    public void verifySoldierUnharmed(final int soldierId,
            final Location soldierAt) throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        final UnitFreeColonist soldier = (UnitFreeColonist) getModel()
                .getUnitById(soldierId);
        assertEquals("Dutch", soldier.getOwner().getName());
        assertEquals(soldierAt, soldier.getLocation());
        assertTrue(soldier.isHoldingGuns());
    }

    /**
     * The calm brave is alive and took the home patrol: it is within two
     * squares of the home the model knows for it.
     */
    public void verifyBraveOnHomePatrol(final int braveId) throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        final UnitNativeBrave brave = (UnitNativeBrave) getModel()
                .getUnitById(braveId);
        assertNotNull(brave, "The calm brave was removed");
        final Location home = getModel().getNativeBraveHome(brave).orElseThrow()
                .getLocation();
        assertTrue(brave.getLocation().getDistanceChebyshev(home) <= 2,
                "The patrol left the radius of home");
    }
}
