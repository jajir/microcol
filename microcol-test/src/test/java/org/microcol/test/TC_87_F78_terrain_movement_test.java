package org.microcol.test;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitPo;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RightPanelUnit;
import org.microcol.page.TerrainMovementPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Verify that the terrain entry cost decides a colonist's move: a full
 * allowance may take one expensive step, a partly spent allowance may not, and
 * the remaining movement survives Save and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_87_F78_terrain_movement_test extends AbstractMicroColTest {

    private static final Location COLONY = Location.of(22, 12);
    private static final Location MOUNTAIN = Location.of(23, 12);
    private static final Location BEYOND = Location.of(24, 12);
    private static final File FIXTURE = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_full_allowance_enters_mountain_and_keeps_zero_movement()
            throws Exception {
        // The colonist next to the colony has one point, a full allowance; the
        // mountain costs three points, so it may enter once and finish at zero.
        useVariant(3, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final TerrainMovementPage terrain = new TerrainMovementPage(
                getContext());
        final FogPage map = new FogPage(getContext());
        final RightPanelUnit rpu = game.getRightPanelUnit(2);
        rpu.selectUnit();
        rpu.verifyFreeActionPoints(1);
        final int colonistId = getModel().getUnitsAt(COLONY).get(2).getId();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 3);

        // The mountain is reachable from the full allowance, a square two steps
        // away is not, and the preview shows the same split.
        terrain.verifyReach(colonistById(colonistId), MOUNTAIN, BEYOND);
        map.preview(COLONY, MOUNTAIN);
        map.verifyPreviewIncludes(MOUNTAIN);
        map.cancelPreview();
        map.preview(COLONY, BEYOND);
        map.verifyPreviewEmpty();
        map.cancelPreview();

        // Moving onto the mountain spends the whole allowance.
        map.move(COLONY, MOUNTAIN, colonistId);
        terrain.verifyUnitAt(colonistById(colonistId), MOUNTAIN, 0);
        // Movement rebuilds the panel; inspect the unit at its new square.
        map.inspect(MOUNTAIN);
        game.verifyThatSelectedTileIs(MOUNTAIN);
        game.getRightPanelUnit(0).verifyFreeActionPoints(0);

        // Zero movement cannot pay even the plains step back to the colony.
        game.moveMouseAtLocation(MOUNTAIN);
        // With zero points the drag never enters movement mode.
        game.dragMouseAtLocationWithFeedback(COLONY, "[23,12]");
        terrain.verifyMovementUnavailable();
        terrain.verifyUnitAt(colonistById(colonistId), MOUNTAIN, 0);

        // Save and Continue keeps the zero allowance on the mountain.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        terrain.verifyUnitAt(colonistById(colonistId), MOUNTAIN, 0);
        terrain.verifyNoReach(colonistById(colonistId));
    }

    @Test
    @Tag("local")
    void TC_02_partly_spent_allowance_cannot_pay_the_mountain_and_keeps_position()
            throws Exception {
        // Two thirds of a point is positive movement, yet below the cost of any
        // neighbouring square, so the colonist may not move and keeps its
        // movement.
        useVariant(2, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final TerrainMovementPage terrain = new TerrainMovementPage(
                getContext());
        final RightPanelUnit rpu = game.getRightPanelUnit(2);
        rpu.selectUnit();
        final int colonistId = getModel().getUnitsAt(COLONY).get(2).getId();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 2);
        terrain.verifyNoReach(colonistById(colonistId));

        // The rejected move changes neither location nor movement.
        game.moveMouseAtLocation(COLONY);
        game.dragMouseAtLocation(MOUNTAIN);
        terrain.verifyCannotMoveHere();
        terrain.closeRejection();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 2);

        // Save and Continue keeps the partly spent allowance.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 2);
    }

    @Test
    @Tag("local")
    void TC_03_fractional_movement_survives_save_and_continue()
            throws Exception {
        // A mounted colonist keeps four thirds of a point, one and a third
        // points, through the real save file and Continue.
        useVariant(4, true);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final TerrainMovementPage terrain = new TerrainMovementPage(
                getContext());
        final int colonistId = getModel().getUnitsAt(COLONY).get(2).getId();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 4);

        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        terrain.verifyUnitAt(colonistById(colonistId), COLONY, 4);
    }

    private Unit colonistById(final int unitId) {
        return getModel().getUnitById(unitId);
    }

    /**
     * Writes a scoped copy of the shared scenario where the square beyond the
     * colony's neighbour is a mountain, and sets every colonist that stands on
     * the colony square to the given allowance. The shared scenario file is not
     * modified.
     */
    private void useVariant(final int movementThirds, final boolean mounted)
            throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(FIXTURE);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(MOUNTAIN, TerrainType.MOUNTAIN);
        data.getModel().getMap().setTerrainType(terrain);
        // The colony's eastern field is the mountain square; a mountain cannot
        // grow tobacco, so that field produces ore, which mountains do have.
        data.getModel().getColonies().stream()
                .filter(colony -> COLONY.equals(colony.getLocation()))
                .flatMap(colony -> colony.getColonyFields().stream())
                .filter(field -> MOUNTAIN
                        .equals(COLONY.add(field.getDirection())))
                .forEach(field -> field.setProducedGoodsType(GoodsType.ORE));
        for (final UnitPo unit : data.getModel().getUnits()) {
            if (unit.getType() == UnitType.COLONIST
                    && unit.getPlaceMap() != null
                    && COLONY.equals(unit.getPlaceMap().getLocation())) {
                unit.setMovementThirds(movementThirds);
                unit.setMounted(mounted);
            }
        }
        final File file = new File(System.getProperty("user.home"),
                "terrain-movement.microcol");
        dao.saveToFile(file.toString(), data);
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }
}
