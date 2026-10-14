package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class EuropeanRelationSaveTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();

    static Stream<String> malformedRelations() {
        return Stream.of("null", "[]", "[null]",
                "[{\"secondPlayer\":\"France\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\"}]",
                "[{\"firstPlayer\":null,\"secondPlayer\":\"France\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":null,\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\",\"state\":null}]",
                "[{\"firstPlayer\":\"Unknown\",\"secondPlayer\":\"France\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"Unknown\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"Dutch\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"Dutch's King\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\",\"state\":\"UNKNOWN\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\",\"state\":\"PEACE\"},{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\",\"state\":\"PEACE\"}]",
                "[{\"firstPlayer\":\"Dutch\",\"secondPlayer\":\"France\",\"state\":\"PEACE\"},{\"firstPlayer\":\"France\",\"secondPlayer\":\"Dutch\",\"state\":\"WAR\"}]");
    }

    private Path saveWithRelations(final String relations) throws Exception {
        final Path file = directory.resolve("relations.microcol");
        dao.saveToFile(file.toString(),
                dao.loadFromClassPath("/maps/free-play.microcol"));
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final JsonObject json = gson.fromJson(Files.readString(file),
                JsonObject.class);
        final JsonObject model = json.getAsJsonObject("model");
        if (relations == null) {
            model.remove("europeanRelations");
        } else {
            model.add("europeanRelations",
                    gson.fromJson(relations, JsonElement.class));
        }
        Files.writeString(file, gson.toJson(json));
        return file;
    }

    private void assertRejectedUnchanged(final String relations)
            throws Exception {
        final Path file = saveWithRelations(relations);
        final byte[] before = Files.readAllBytes(file);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(dao.loadFromFile(file.toFile()).getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void missingRelationListIsRejectedWithoutRewritingTheFile()
            throws Exception {
        assertRejectedUnchanged(null);
    }

    @ParameterizedTest
    @MethodSource("malformedRelations")
    void malformedRelationDataIsRejectedWithoutRewritingTheFile(
            final String relations) throws Exception {
        assertRejectedUnchanged(relations);
    }
}
