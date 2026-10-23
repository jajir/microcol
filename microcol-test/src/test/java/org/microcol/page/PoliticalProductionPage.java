package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Colony;
import org.microcol.model.Difficulty;
import org.microcol.model.GoodsType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Rebel support bonus and Tory penalty in real production. Fixture F69 is
 * derived from F64: New Amsterdam has two field workers, a blacksmith worker in
 * the blacksmith's house and a second colonist waiting on the colony square.
 */
public final class PoliticalProductionPage extends AbstractScreen {
    private static final File SOURCE = new File(
            "src/test/scenarios/F64-la-salle-stockade.microcol");
    private static final int BLACKSMITH = 4;
    private static final int WAITING_COLONIST = 5;

    public PoliticalProductionPage(final TestContext context) {
        super(context);
    }

    /**
     * F69: three residents, so 300 liberty is exactly 50 percent. The
     * blacksmith's house makes three tools from three ore at the base rate and
     * four with the support bonus, but the warehouse holds only the given ore.
     */
    public void prepareFixture(final long liberty, final int ore)
            throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject colony = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        colony.addProperty("liberty", liberty);
        colony.getAsJsonObject("colonyWarehouse").addProperty("ORE", ore);
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            if ("BLACKSMITHS_HOUSE"
                    .equals(construction.get("type").getAsString())) {
                construction.getAsJsonArray("slots").get(0).getAsJsonObject()
                        .addProperty("workerId", BLACKSMITH);
            }
        }
        JsonObject waiting = null;
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == BLACKSMITH) {
                waiting = unit.deepCopy();
                unit.remove("placeMap");
                unit.add("placeConstructionSlot", new JsonObject());
            }
        }
        waiting.addProperty("id", WAITING_COLONIST);
        model.getAsJsonArray("units").add(waiting);
        final Path directory = Path.of("target", "f69");
        Files.createDirectories(directory);
        final File file = directory.resolve("political-production.microcol")
                .toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    /**
     * F69 Tory variant: the Viceroy profile draws its Tory line at 6. New
     * Amsterdam has six residents and no liberty: two field workers, the
     * blacksmith and three carpenters (units 6 to 8), so the penalty applies.
     */
    public void prepareToryFixture() throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.addProperty("difficulty", "VICEROY");
        authorCrownForce(model, Difficulty.VICEROY);
        final JsonObject colony = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        colony.addProperty("liberty", 0);
        JsonObject template = null;
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == BLACKSMITH) {
                template = unit.deepCopy();
                unit.remove("placeMap");
                unit.add("placeConstructionSlot", new JsonObject());
            }
        }
        template.remove("placeMap");
        template.add("placeConstructionSlot", new JsonObject());
        int carpenter = 0;
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            final String type = construction.get("type").getAsString();
            if ("BLACKSMITHS_HOUSE".equals(type)) {
                construction.getAsJsonArray("slots").get(0).getAsJsonObject()
                        .addProperty("workerId", BLACKSMITH);
            } else if ("CARPENTERS_STAND".equals(type)) {
                for (final JsonElement slot : construction
                        .getAsJsonArray("slots")) {
                    final JsonObject unit = template.deepCopy();
                    unit.addProperty("id", 6 + carpenter);
                    model.getAsJsonArray("units").add(unit);
                    slot.getAsJsonObject().addProperty("workerId",
                            6 + carpenter++);
                }
            }
        }
        final Path directory = Path.of("target", "f69");
        Files.createDirectories(directory);
        final File file = directory.resolve("political-tory.microcol").toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    /**
     * Author the Crown's starting force for the difficulty, all in reserve: 4
     * plus ordinal infantry, 2 plus ordinal cavalry, 2 artillery and 2 ships. A
     * saved roster must match its difficulty, so a changed difficulty needs a
     * matching roster.
     */
    private static void authorCrownForce(final JsonObject model,
            final Difficulty difficulty) {
        final int index = difficulty.ordinal();
        final String[] families = { "INFANTRY", "CAVALRY", "ARTILLERY",
                "MAN_OF_WAR" };
        final int[] counts = { 4 + index, 2 + index, 2, 2 };
        final JsonArray members = new JsonArray();
        int id = 1;
        for (int family = 0; family < families.length; family++) {
            for (int i = 0; i < counts[family]; i++) {
                final JsonObject member = new JsonObject();
                member.addProperty("id", id++);
                member.addProperty("family", families[family]);
                member.addProperty("state", "RESERVE");
                members.add(member);
            }
        }
        final JsonObject roster = new JsonObject();
        roster.addProperty("growthCount", 0);
        roster.addProperty("remainder", 0);
        roster.add("members", members);
        for (final JsonElement element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            if (player.has("royalRoster")) {
                player.add("royalRoster", roster.deepCopy());
            }
        }
    }

    /** The colonist waiting on the colony square is the one to be employed. */
    public int getWaitingColonistId() {
        return WAITING_COLONIST;
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    /** What the next update would make, consume and leave unproduced. */
    public void verifyPlannedTools(final int produced, final int consumed,
            final int blocked) {
        assertEquals(produced, colony().getGoodsStats()
                .getStatsByType(GoodsType.TOOLS).getRowProduction());
        assertEquals(consumed, colony().getGoodsStats()
                .getStatsByType(GoodsType.ORE).getConsumed());
        assertEquals(blocked, colony().getGoodsStats()
                .getStatsByType(GoodsType.TOOLS).getBlockedProduction());
    }

    public void verifyWarehouse(final GoodsType type, final int amount) {
        assertEquals(amount,
                colony().getWarehouse().getGoods(type).getAmount());
    }

    /** The exact fraction, never the rounded display, decides the bonus. */
    public void verifyPoliticalProduction(final int residents, final int bonus,
            final int tories, final int penalty) {
        assertEquals(residents, colony().getPopulation());
        assertEquals(bonus, colony().getPoliticalProduction().getBonus());
        assertEquals(tories, colony().getPoliticalProduction().getToryCount());
        assertEquals(penalty, colony().getPoliticalProduction().getPenalty());
    }
}
