package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.microcol.gui.FileSelectingService;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the lost-rebellion scenario (R-0192). It derives a
 * prepared copy of the shared loosing-battles scenario, so that file keeps its
 * content: some colonies are already held by the Crown, with every resident, as
 * if the Crown had taken them in an earlier battle. The saved rebellion members
 * are untouched, so a colony the Crown took stays a member.
 */
final class RebellionLossFixture {

    private static final String CROWN = "Dutch's King";

    private RebellionLossFixture() {
    }

    /**
     * Write the prepared copy and make the next Load game use it.
     *
     * @param context    running application context
     * @param source     shared scenario
     * @param name       file name of the prepared copy
     * @param crownHolds identities of the colonies that the Crown already holds
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int... crownHolds) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final Set<Integer> residents = new HashSet<>();
        for (final JsonElement element : model.getAsJsonArray("colonies")) {
            final JsonObject colony = element.getAsJsonObject();
            if (!holds(crownHolds, colony.get("id").getAsInt())) {
                continue;
            }
            colony.addProperty("ownerName", CROWN);
            collectResidents(colony, residents);
        }
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (residents.contains(unit.get("id").getAsInt())) {
                unit.addProperty("ownerId", CROWN);
            }
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static boolean holds(final int[] crownHolds, final int colonyId) {
        for (final int id : crownHolds) {
            if (id == colonyId) {
                return true;
            }
        }
        return false;
    }

    /** Workers of the fields and of every building slot of one colony. */
    private static void collectResidents(final JsonObject colony,
            final Set<Integer> residents) {
        for (final JsonElement field : colony.getAsJsonArray("colonyFields")) {
            addWorker(field.getAsJsonObject(), residents);
        }
        for (final JsonElement construction : colony
                .getAsJsonArray("constructions")) {
            for (final JsonElement slot : construction.getAsJsonObject()
                    .getAsJsonArray("slots")) {
                addWorker(slot.getAsJsonObject(), residents);
            }
        }
    }

    private static void addWorker(final JsonObject place,
            final Set<Integer> residents) {
        if (place.has("workerId")) {
            residents.add(place.get("workerId").getAsInt());
        }
    }
}
