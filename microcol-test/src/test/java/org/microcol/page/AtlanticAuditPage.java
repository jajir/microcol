package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.util.PaintService;
import org.microcol.model.*;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;

/** Read-only voyage observations and the ordinary Europe return gesture. */
public final class AtlanticAuditPage extends AbstractScreen {
    public AtlanticAuditPage(final TestContext context) {
        super(context);
    }

    public void verifyVoyage(final int turns, final boolean toEurope) {
        final Unit ship = getModel().getUnitById(3);
        assertTrue(ship.getPlace() instanceof PlaceHighSea);
        final PlaceHighSea place = (PlaceHighSea) ship.getPlace();
        assertEquals(turns, place.getRemainigTurns());
        assertEquals(toEurope, place.isTravelToEurope());
    }

    public int getVoyageWeather() {
        return ((PlaceHighSea) getModel().getUnitById(3).getPlace())
                .getWeather();
    }

    public int verifyVoyageSample(final boolean toEurope,
            final boolean westernEdge) {
        final Unit ship = getModel().getUnitById(3);
        final PlaceHighSea voyage = (PlaceHighSea) ship.getPlace();
        final int weather = voyage.getWeather();
        assertTrue(weather >= -1 && weather <= 1);
        assertEquals(toEurope, voyage.isTravelToEurope());
        assertEquals(westernEdge ? Direction.west : Direction.east,
                ((org.microcol.model.unit.Ship) ship).getDepartureDirection()
                        .orElseThrow());
        final int ordinary = Math.max(1,
                Math.min(4,
                        2 + weather + (toEurope ? 0 : 1)
                                + (toEurope && westernEdge ? 1 : 0)
                                - (ship.getType().getSpeed() >= 6 ? 1 : 0)));
        final int expected = toEurope && westernEdge
                && ship.getOwner().getCongressMembers()
                        .contains(Founder.FERDINAND_MAGELLAN)
                                ? (ordinary + 1) / 2
                                : ordinary;
        assertEquals(expected, voyage.getOriginalTurns());
        assertEquals(expected, voyage.getRemainigTurns());
        return expected;
    }

    public void verifyMagellanArrival(final int existingRemaining) {
        final Player owner = getModel().getPlayerByName("Dutch");
        assertTrue(owner.getCongressMembers()
                .contains(Founder.FERDINAND_MAGELLAN));
        assertEquals(UnitType.GALLEON.getSpeed() + 1,
                getModel().getUnitById(3).getSpeed());
        final PlaceHighSea existing = (PlaceHighSea) getModel()
                .getUnitById(1001).getPlace();
        assertEquals(4, existing.getOriginalTurns());
        assertEquals(existingRemaining, existing.getRemainigTurns());
        assertEquals(1, existing.getWeather());
    }

    public void verifyVoyageMetadata(final int weather, final int originalTurns,
            final int remainingTurns, final boolean toEurope) {
        final PlaceHighSea voyage = (PlaceHighSea) getModel().getUnitById(3)
                .getPlace();
        assertEquals(weather, voyage.getWeather());
        assertEquals(originalTurns, voyage.getOriginalTurns());
        assertEquals(remainingTurns, voyage.getRemainigTurns());
        assertEquals(toEurope, voyage.isTravelToEurope());
    }

    public void verifyShipMapPainting(final boolean expectedVisible) {
        getRobot().interact(() -> {
            final Canvas canvas = new Canvas(Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX);
            canvas.getGraphicsContext2D().setFill(Color.MAGENTA);
            canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(),
                    canvas.getHeight());
            // A frame may still hold this unit after the model moves it to sea.
            getContext().getClassFromGuice(PaintService.class).paintUnit(
                    canvas.getGraphicsContext2D(), Point.ZERO,
                    getModel().getUnitById(3));
            final WritableImage frame = canvas.snapshot(null, null);
            boolean painted = false;
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    painted |= frame.getPixelReader().getArgb(x,
                            y) != 0xffff00ff;
                }
            }
            assertEquals(expectedVisible, painted,
                    "Only an on-map ship should paint a sprite or flag");
        });
    }

    public void verifyCargo() {
        final UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(3);
        new ShipWrapper(ship).verifyCargoGoods(0, Goods.of(GoodsType.ORE, 7));
        final UnitFreeColonist passenger = (UnitFreeColonist) getModel()
                .getUnitById(4);
        assertTrue(passenger.getPlace() instanceof PlaceCargoSlot);
        assertSame(ship.getCargo().getSlotByIndex(1),
                passenger.getPlaceCargoSlot().getCargoSlot());
        assertEquals(3, passenger.getPlaceCargoSlot().getOwnerUnit().getId());
        assertEquals(80, passenger.getTools());
        assertFalse(passenger.isHoldingGuns());
    }

    public void verifyInEurope() {
        assertTrue(getModel().getUnitById(3).isAtEuropePort());
    }

    public void verifyMovement(final int expected) {
        assertEquals(expected, getModel().getUnitById(3).getActionPoints());
    }

    /** Current movement of the voyage ship, read before a no-spend check. */
    public int getMovement() {
        return getModel().getUnitById(3).getActionPoints();
    }

    public void verifyVoyageText(final String expected) {
        assertTrue(isCssIdVisible("voyage-remaining-3"));
        assertEquals(expected, getLabeledById("voyage-remaining-3").getText());
        final javafx.scene.text.Text rendered = (javafx.scene.text.Text) getLabeledById(
                "voyage-remaining-3").lookup(".text");
        assertEquals(expected, rendered.getText(),
                "The visible voyage reason must not be truncated");
    }

    public void verifyVoyagePanelsSeparated() {
        final Parent inbound = getNodeFinder().lookup(".to-europe")
                .queryAs(Parent.class);
        final Parent outbound = getNodeFinder().lookup(".to-new-world")
                .queryAs(Parent.class);
        final var incoming = inbound.localToScene(inbound.getBoundsInLocal());
        final var outgoing = outbound.localToScene(outbound.getBoundsInLocal());
        assertTrue(incoming.getMaxX() < outgoing.getMinX(),
                "Incoming voyage controls must not cover the departure area: incoming="
                        + incoming + "; outgoing=" + outgoing);
    }

    public void sendShipBack() throws Exception {
        attemptSendShipBack();
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(3).isAtHighSea());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void attemptSendShipBack() {
        final javafx.scene.Node target = getNodeFinder()
                .lookup("#europe-outgoing-scroll .viewport").query();
        getRobot().drag(
                PageComponentPanelDock.of(getContext()).getUnitsInPort().get(0),
                MouseButton.PRIMARY).dropTo(target);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDepartureRejection(final String expected) {
        assertEquals(expected, getNodeFinder().lookup(expected)
                .queryAs(javafx.scene.control.Label.class).getText());
        assertTrue(isCssIdVisible("buttonOk"));
    }

    public void closeRejection() {
        clickOnButtonWithId("buttonOk");
    }

    public void verifyReturnedTo(final Location departure) {
        final Unit ship = getModel().getUnitById(3);
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(departure, ship.getLocation());
    }
}
