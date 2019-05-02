package org.microcol.model.campaign.po;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

/**
 * Test basic loading of game model.
 * <p>
 * DAO can't test logic error in data like non existing colony name.
 * </p>
 */
public class GameModelPoDaoTest {

    private GameModelPoDao dao = new GameModelPoDao();

    @Test
    public void test_loadFromClassPath_invalid_file_format() throws Exception {
        assertThrows(JsonSyntaxException.class, () -> {
            dao.loadFromClassPath("/maps/test-map-invalid-0x0.json");
        });
    }

    @Test
    public void test_loadFromClassPath_non_existing_file() {
        assertThrows(IllegalArgumentException.class, () -> {
            dao.loadFromClassPath("/maps/test-map-missing-0x0.json");
        });
    }

    @Test
    public void test_loadFromClassPath_curruped_file() {
        final JsonSyntaxException exception = assertThrows(JsonSyntaxException.class, () -> {
            dao.loadFromClassPath("/maps/test-currupted-file.microcol");
        });

        assertEquals(
                "com.google.gson.stream.MalformedJsonException: Expected name at line 189 column 9 path $.model.units[9].action",
                exception.getMessage());
    }

}
