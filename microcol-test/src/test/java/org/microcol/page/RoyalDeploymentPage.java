package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.microcol.model.CargoSlot;
import org.microcol.model.MilitaryRole;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.RoyalRoster;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

/**
 * The Crown's real deployment after independence (R-0186): roster families, the
 * voyage of the carrying men-of-war, the landing and the attack on a rebel
 * colony. The page only reads the live model and counts its events; the
 * player's actions go through the ordinary controls of the other pages, and
 * only the verify methods assert.
 */
public final class RoyalDeploymentPage extends AbstractScreen {

    private static final String CROWN = "Dutch's King";

    private final AtomicInteger royalAttacks = new AtomicInteger();

    /** Royal turn of the first landing, or zero while nobody has landed. */
    private int landedTurn;

    /** Royal turn of the first attack, or zero while there was none. */
    private int attackTurn;

    public RoyalDeploymentPage(final TestContext context) {
        super(context);
    }

    /**
     * Count every attack by a Crown unit from now on. A Continue replaces the
     * model, so the page watches again after loading; the count carries over.
     */
    public void watchRoyalAttacks() {
        getRobot().interact(
                () -> getModel().addListener(new ModelListenerAdapter() {
                    @Override
                    public void onUnitAttacked(final UnitAttackedEvent event) {
                        if (event.getAttacker().getOwner().getName()
                                .equals(CROWN)) {
                            royalAttacks.incrementAndGet();
                        }
                    }
                }));
    }

    private RoyalRoster crown() {
        final Player king = getModel().getPlayerByName(CROWN);
        return getModel().getRoyalRosterOf(king);
    }

    private List<Unit> crownUnits() {
        return getModel().getAllUnits().stream()
                .filter(unit -> unit.getOwner().getName().equals(CROWN))
                .collect(Collectors.toList());
    }

    private List<Unit> crownShips() {
        return crownUnits().stream().filter(Unit::isShip)
                .collect(Collectors.toList());
    }

    private List<Unit> aboard() {
        return crownShips().stream()
                .flatMap(ship -> ((UnitWithCargo) ship).getCargo().getSlots()
                        .stream())
                .filter(CargoSlot::isLoadedUnit)
                .map(slot -> slot.getUnit().orElseThrow())
                .collect(Collectors.toList());
    }

    /**
     * Crown units bound to roster members. Colonists of a captured colony also
     * belong to the Crown, but they are not part of its royal force.
     */
    private List<Unit> boundUnits() {
        final RoyalRoster crown = crown();
        return crownUnits().stream()
                .filter(unit -> crown.findByUnitId(unit.getId()).isPresent())
                .collect(Collectors.toList());
    }

    /** Royal units of the roster that stand on land. */
    private List<Unit> ashore() {
        return boundUnits().stream()
                .filter(unit -> !unit.isShip() && unit.isAtPlaceLocation())
                .collect(Collectors.toList());
    }

    private static boolean isFamily(final Family family, final Unit unit) {
        switch (family) {
        case MAN_OF_WAR:
            return unit.getType() == UnitType.MAN_OF_WAR;
        case ARTILLERY:
            return unit instanceof UnitArtillery;
        case INFANTRY:
            return isInfantry(unit);
        case CAVALRY:
            return isCavalry(unit);
        default:
            return false;
        }
    }

    private static boolean isInfantry(final Unit unit) {
        return unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit)
                        .getMilitaryRole() == MilitaryRole.ROYAL
                && !((UnitFreeColonist) unit).isMounted();
    }

    private static boolean isCavalry(final Unit unit) {
        return unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit)
                        .getMilitaryRole() == MilitaryRole.ROYAL
                && ((UnitFreeColonist) unit).isMounted();
    }

    /**
     * @return the roster's four families and their counts in every state as one
     *         text, to compare across a Continue
     */
    public String crownForce() {
        final RoyalRoster crown = crown();
        return Arrays.stream(Family.values())
                .map(family -> family + " " + Arrays.stream(State.values()).map(
                        state -> String.valueOf(crown.getCount(family, state)))
                        .collect(Collectors.joining("/")))
                .collect(Collectors.joining("; "));
    }

    /**
     * Play ordinary turns with the player's own control until the Crown has
     * attacked or the limit is reached, and remember on which royal turn the
     * soldiers first stood on land and on which the first attack happened. The
     * Crown's turn is complete when the player's next turn has started.
     *
     * @param game      required game screen
     * @param firstTurn royal turn number of the first turn played here
     * @param limit     last royal turn that is played
     */
    public void playRoyalTurnsUntilAttack(final GamePage game,
            final int firstTurn, final int limit) throws Exception {
        for (int turn = firstTurn; turn <= limit && attackTurn == 0; turn++) {
            game.nextTurnAndCloseDialogs();
            if (landedTurn == 0 && !ashore().isEmpty()) {
                landedTurn = turn;
            }
            if (royalAttacks.get() > 0) {
                attackTurn = turn;
            }
        }
    }

    /** Every member of the starting force has been put on a ship. */
    public void verifyAllReserveSent() {
        final RoyalRoster crown = crown();
        assertEquals(0, crown.getCount(Family.MAN_OF_WAR, State.RESERVE),
                "A reserve man-of-war stayed in Europe");
        assertEquals(0,
                crown.getCount(Family.INFANTRY, State.RESERVE)
                        + crown.getCount(Family.CAVALRY, State.RESERVE)
                        + crown.getCount(Family.ARTILLERY, State.RESERVE),
                "Reserve ground units stayed in Europe");
        assertEquals(2, crown.getCount(Family.MAN_OF_WAR, State.SHIPPED));
        assertEquals(6, crown.getCount(Family.INFANTRY, State.SHIPPED));
        assertEquals(4, crown.getCount(Family.CAVALRY, State.SHIPPED));
        assertEquals(2, crown.getCount(Family.ARTILLERY, State.SHIPPED));
    }

    /** Both carriers are on the Atlantic crossing with their passengers. */
    public void verifyVoyageUnderway() {
        final List<Unit> ships = crownShips();
        assertEquals(2, ships.size(), "The Crown has other than two ships");
        assertEquals(2, ships.stream().filter(Unit::isAtHighSea).count(),
                "The carriers are not on the Atlantic crossing");
        assertEquals(12, aboard().size(), "The passengers are not all aboard");
    }

    /**
     * The passengers aboard are the actual royal families: unmounted royal
     * infantry, mounted royal cavalry and artillery.
     */
    public void verifyPassengerFamiliesAboard(final long infantry,
            final long cavalry, final long artillery) {
        final List<Unit> passengers = aboard();
        assertEquals(
                infantry, passengers.stream()
                        .filter(RoyalDeploymentPage::isInfantry).count(),
                "Royal infantry aboard");
        assertEquals(
                cavalry, passengers.stream()
                        .filter(RoyalDeploymentPage::isCavalry).count(),
                "Royal cavalry aboard");
        assertEquals(artillery,
                passengers.stream()
                        .filter(unit -> unit instanceof UnitArtillery).count(),
                "Artillery aboard");
    }

    /** Continue reproduces the roster exactly as it was before the save. */
    public void verifyCrownForceUnchanged(final String expected) {
        assertEquals(expected, crownForce(),
                "The Crown's force changed by Continue");
    }

    /** Soldiers first stood on land within the limit of royal turns. */
    public void verifyLandedWithin(final int limit) {
        assertTrue(landedTurn > 0 && landedTurn <= limit,
                "No royal soldier landed within " + limit
                        + " royal turns; landed at turn " + landedTurn);
    }

    /** Every unit ashore is a real regular, armed and able to attack. */
    public void verifyRoyalFamiliesAshore() {
        assertTrue(!ashore().isEmpty(), "No royal soldier stands on land");
        for (final Unit unit : ashore()) {
            if (unit instanceof UnitArtillery) {
                continue;
            }
            assertTrue(isInfantry(unit) || isCavalry(unit),
                    "A landed unit is not a royal regular: " + unit);
            assertTrue(unit.canAttack(),
                    "A landed regular has no equipment to attack");
        }
    }

    /** The Crown first attacked a rebel target within the royal turn limit. */
    public void verifyRoyalAttackWithin(final int limit) {
        assertTrue(attackTurn > 0 && attackTurn <= limit,
                "No royal attack within " + limit
                        + " royal turns; attacked at turn " + attackTurn);
        assertTrue(royalAttacks.get() > 0,
                "The model reported no attack by a Crown unit");
    }

    /**
     * Every shipped member is exactly one live unit of the Crown with its own
     * family, and the Crown has no man-of-war or royal regular outside the
     * roster. Colonists and guns taken with a captured colony may belong to the
     * Crown; they are not royal forces and are not counted as members.
     */
    public void verifyCrownUnitsMatchRoster() {
        final RoyalRoster crown = crown();
        final Player king = getModel().getPlayerByName(CROWN);
        final Set<Integer> bound = new HashSet<>();
        for (final RoyalRoster.Member member : crown.getMembers()) {
            if (member.getState() != State.SHIPPED) {
                continue;
            }
            final int unitId = member.getUnitId().orElse(-1);
            assertTrue(bound.add(unitId), "Unit " + unitId + " is bound twice");
            final Unit unit = getModel().tryGetUnitById(unitId).orElse(null);
            assertTrue(unit != null && unit.getOwner() == king,
                    "Member " + member.getId() + " has no live royal unit");
            assertTrue(isFamily(member.getFamily(), unit), "Member "
                    + member.getId() + " is not a " + member.getFamily());
        }
        assertEquals(bound.size(), boundUnits().size(),
                "A shipped member has no live unit");
        for (final Unit unit : crownUnits()) {
            if (!bound.contains(unit.getId())) {
                assertTrue(unit.getType() != UnitType.MAN_OF_WAR, "Man-of-war "
                        + unit.getId() + " is outside the roster");
                assertTrue(!(unit instanceof UnitFreeColonist)
                        || ((UnitFreeColonist) unit)
                                .getMilitaryRole() != MilitaryRole.ROYAL,
                        "Royal regular " + unit.getId()
                                + " is outside the roster");
            }
        }
    }
}
