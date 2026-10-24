package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.GoodsType;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for warehouse overflow scenarios. It derives a
 * prepared copy of a shared scenario, so the shared file keeps its content.
 */
final class OverflowFixture {

    private OverflowFixture() {
    }

    /**
     * Fill one stock of one colony to a given amount and make the next Load
     * game use the prepared copy. A full stock throws away everything that the
     * colony then produces of that good.
     *
     * @param context    running application context
     * @param source     shared scenario
     * @param name       file name of the prepared copy
     * @param colonyName colony whose stock is set
     * @param goodsType  good whose stock is set
     * @param amount     stock of that good
     */
    static void prepare(final TestContext context, final File source,
            final String name, final String colonyName,
            final GoodsType goodsType, final int amount) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        for (final JsonElement colony : saved.getAsJsonObject("model")
                .getAsJsonArray("colonies")) {
            final JsonObject colonyObject = colony.getAsJsonObject();
            if (colonyName.equals(colonyObject.get("name").getAsString())) {
                colonyObject.getAsJsonObject("colonyWarehouse")
                        .addProperty(goodsType.name(), amount);
            }
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
