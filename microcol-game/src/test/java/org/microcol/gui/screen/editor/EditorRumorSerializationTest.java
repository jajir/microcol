package org.microcol.gui.screen.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

/**
 * The map editor edits a loaded save and writes it back through the shared file
 * format. Unresolved lost-city rumors are kept by that path even when another
 * square is painted, so the editor never silently removes them.
 */
class EditorRumorSerializationTest {

    private static final Location FIRST = Location.of(34, 8);

    private static final Location SECOND = Location.of(33, 6);

    /** A square away from both markers that the terrain brush repaints. */
    private static final Location PAINTED = Location.of(36, 10);

    @Test
    void editorLoadPaintAndSaveKeepTheRumorMarkers(@TempDir final Path dir)
            throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(FIRST, TerrainType.GRASSLAND);
        terrain.put(SECOND, TerrainType.GRASSLAND);
        terrain.put(PAINTED, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setRumors(Set.of(FIRST, SECOND));
        final File file = dir.resolve("edited.microcol").toFile();
        dao.saveToFile(file.getAbsolutePath(), data);

        // The editor loads the file, paints one other square and saves it.
        final ModelService editor = new ModelService(dao,
                new VisibleAreaService());
        editor.load(file);
        new MouseOperationTerrain(TerrainType.PLAINS)
                .execute(new MouseOperationContext(editor), PAINTED);
        editor.save();

        // The saved file holds the painted square and both markers.
        final GameModelPo saved = dao.loadFromFile(file);
        assertEquals(TerrainType.PLAINS,
                saved.getModel().getMap().getTerrainMap().get(PAINTED));
        assertEquals(Set.of(FIRST, SECOND),
                saved.getModel().getMap().getRumorSet());

        // The game accepts the edited world and sees both markers.
        final Model model = Model.make(saved.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            assertTrue(model.getMap().hasLostCityRumorAt(FIRST));
            assertTrue(model.getMap().hasLostCityRumorAt(SECOND));
            assertFalse(model.getMap().hasLostCityRumorAt(PAINTED));
        } finally {
            model.stop();
        }
    }

    @Test
    void aWorldWithoutMarkersStaysWithoutMarkersAfterEditing(
            @TempDir final Path dir) throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        final File file = dir.resolve("plain.microcol").toFile();
        dao.saveToFile(file.getAbsolutePath(), data);

        final ModelService editor = new ModelService(dao,
                new VisibleAreaService());
        editor.load(file);
        editor.save();

        assertTrue(dao.loadFromFile(file).getModel().getMap().getRumorSet()
                .isEmpty());
    }

}
