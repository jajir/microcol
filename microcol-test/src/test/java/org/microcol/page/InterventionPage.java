package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.MilitaryRole;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Foreign-intervention target as the first rebel update announces it and as a
 * later update reports its earning (R-0189), and the delivery of the earned
 * forces to the port (R-0190). Reading the bells that an update will produce
 * changes nothing.
 */
public final class InterventionPage extends AbstractScreen {

    /** Tab presses enough to cycle through every unit with movement left. */
    private static final int MAX_TAB_PRESSES = 16;

    private List<Integer> remembered = List.of();

    private int usedUnitId = -1;

    public InterventionPage(final TestContext context) {
        super(context);
    }

    /**
     * Bells the colonies of the current nation produce in its next update, read
     * before the turn ends.
     */
    public int plannedBells() {
        return getModel().getColonies(getModel().getCurrentPlayer()).stream()
                .mapToInt(colony -> Math.max(0, colony.getGoodsStats()
                        .getStatsByType(GoodsType.BELL).getRowProduction()))
                .sum();
    }

    /** The first update announces the fixed target in the turn report. */
    public void verifyAnnouncement(final ScreenTurnReport report,
            final int target) {
        report.verifyThatAtLeastOneEventMessageContains(
                "Foreign intervention target: " + target + " bells.");
    }

    /** The update that reaches the target reports the earning. */
    public void verifyEarnedNotice(final Optional<ScreenTurnReport> report,
            final int target) {
        assertTrue(report.isPresent(),
                "The earning update must show a turn report");
        report.get().verifyThatAtLeastOneEventMessageContains(
                "Foreign intervention is earned: the target of " + target
                        + " bells is reached.");
    }

    /** A later update has no second earning notice. */
    public void verifyNoEarnedNotice(final Optional<ScreenTurnReport> report) {
        report.ifPresent(shown -> shown
                .verifyEventMessageCount("Foreign intervention is earned", 0));
    }

    /** The update that delivers the earned forces reports their arrival. */
    public void verifyArrivalNotice(final Optional<ScreenTurnReport> report,
            final String colonyName) {
        assertTrue(report.isPresent(),
                "The arrival update must show a turn report");
        report.get().verifyThatAtLeastOneEventMessageContains(
                "Foreign intervention arrived at " + colonyName
                        + ": two men-of-war, four Continental infantry and two Continental cavalry.");
    }

    /** A later update has no second arrival notice. */
    public void verifyNoArrivalNotice(final Optional<ScreenTurnReport> report) {
        report.ifPresent(shown -> shown
                .verifyEventMessageCount("Foreign intervention arrived", 0));
    }

    /**
     * The delivered forces stand on the port as rebel-controlled units: the
     * given number of men-of-war, infantry and cavalry. Every Continental
     * soldier is a rebel Continental, armed, and marked as delivered by
     * intervention.
     */
    public void verifyDeliveredForces(final Location port, final int ships,
            final int infantry, final int cavalry) {
        final Player owner = getModel().getCurrentPlayer();
        final List<Unit> atPort = getModel().getUnitsAt(port).stream()
                .filter(unit -> unit.getOwner() == owner)
                .collect(Collectors.toList());
        assertEquals(ships, atPort.stream()
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR).count());
        final List<UnitFreeColonist> soldiers = atPort.stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .map(unit -> (UnitFreeColonist) unit)
                .filter(UnitFreeColonist::isInterventionDelivered)
                .collect(Collectors.toList());
        assertEquals(infantry, soldiers.stream()
                .filter(soldier -> !soldier.isMounted()).count());
        assertEquals(cavalry,
                soldiers.stream().filter(UnitFreeColonist::isMounted).count());
        assertTrue(soldiers.stream().allMatch(
                soldier -> soldier.getMilitaryRole() == MilitaryRole.CONTINENTAL
                        && soldier.isHoldingGuns()));
    }

    /** The delivered ships and soldiers of the current nation, by identity. */
    private List<Unit> deliveredForces() {
        final Player owner = getModel().getCurrentPlayer();
        return owner.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                        || unit instanceof UnitFreeColonist
                                && ((UnitFreeColonist) unit)
                                        .isInterventionDelivered())
                .sorted(java.util.Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    /** Remember the identity of the delivered forces for a later comparison. */
    public void rememberDeliveredForces() {
        remembered = deliveredForces().stream().map(Unit::getId)
                .collect(Collectors.toList());
    }

    /**
     * The same eight units, with the same identities, types and roles, are
     * still the delivered forces, for example after Save and Continue.
     */
    public void verifyDeliveredIdentityKept(final int expectedCount) {
        assertEquals(expectedCount, remembered.size());
        assertEquals(remembered, deliveredForces().stream().map(Unit::getId)
                .collect(Collectors.toList()));
        for (final Unit unit : deliveredForces()) {
            assertEquals(getModel().getCurrentPlayer(), unit.getOwner());
            if (unit instanceof UnitFreeColonist) {
                assertEquals(MilitaryRole.CONTINENTAL,
                        ((UnitFreeColonist) unit).getMilitaryRole());
            }
        }
    }

    /**
     * Nothing was delivered: the nation owns no man-of-war and no soldier from
     * the intervention, and the arrival was not recorded.
     */
    public void verifyNothingDelivered() {
        assertTrue(deliveredForces().isEmpty(),
                "No intervention unit may exist while delivery is pending");
        assertTrue(!getModel().getCurrentPlayer().getIntervention()
                .orElseThrow().isDelivered());
    }

    /** A saved Crown ship still stands on the given sea square. */
    public void verifyShipAt(final int shipId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(shipId).getLocation());
    }

    /** The ship no longer exists, for example after the player sank it. */
    public void verifyShipGone(final int shipId) {
        assertTrue(getModel().tryGetUnitById(shipId).isEmpty(),
                "Ship " + shipId + " must be gone");
    }

    private int selectedUnitIdOrMinusOne() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit().map(Unit::getId).orElse(-1);
    }

    /**
     * Select the delivered cavalry with the lowest identity using the Tab key a
     * player cycles through units with.
     */
    public void selectDeliveredCavalry(final GamePage game) {
        usedUnitId = deliveredForces().stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && ((UnitFreeColonist) unit).isMounted())
                .mapToInt(Unit::getId).min().orElseThrow();
        for (int press = 0; press < MAX_TAB_PRESSES
                && selectedUnitIdOrMinusOne() != usedUnitId; press++) {
            game.pressTab();
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    /** The chosen delivered cavalry is the unit selected on the map. */
    public void verifyDeliveredCavalrySelected() {
        assertEquals(usedUnitId, selectedUnitIdOrMinusOne());
    }

    /**
     * Drag the selected unit from the port square onto a neighbouring square.
     */
    public void moveSelectedUnit(final GamePage game, final Location from,
            final Location to) throws Exception {
        game.panToTile(from);
        game.panToTile(to);
        game.moveMouseAtLocation(from);
        game.dragMouseAtLocation(to);
    }

    /**
     * The delivered cavalry made a legal move: it stands on the new square, is
     * still the same rebel Continental from the intervention and has spent
     * movement.
     */
    public void verifyDeliveredCavalryMovedTo(final Location expected) {
        final UnitFreeColonist cavalry = (UnitFreeColonist) getModel()
                .getUnitById(usedUnitId);
        assertEquals(expected, cavalry.getLocation());
        assertTrue(cavalry.isInterventionDelivered());
        assertEquals(MilitaryRole.CONTINENTAL, cavalry.getMilitaryRole());
        assertTrue(cavalry.getActionPoints() < cavalry.getSpeed(),
                "The move must have used movement");
    }

    public void closeReport(final ScreenTurnReport report) {
        report.close();
    }

    public void closeReport(final Optional<ScreenTurnReport> report) {
        report.ifPresent(ScreenTurnReport::close);
    }
}
