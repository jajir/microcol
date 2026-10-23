package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Direction;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Founder;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Trade with the adjacent French colony Quebec under Jan de Witt (R-0167). The
 * derived scenario keeps the saved F25 world and its identities, sets Quebec's
 * stock, makes one free square beside Quebec open sea for a Dutch frigate and
 * places a loaded Dutch wagon on another free square beside it. Squares are
 * chosen from the real terrain: land that carries no road, river, resource,
 * rumor or mineral and that no colonist works. The dialog is driven only by
 * real mouse and keyboard input; expected numbers come from the Europe market
 * of the host nation.
 */
public final class ForeignColonyTradePage extends AbstractScreen {

    public static final File SOURCE = new File(
            "src/test/scenarios/F25-european-relations.microcol");
    public static final int WAGON_ID = 900;
    public static final int SHIP_ID = 901;
    public static final int START_GOLD = 1000;
    private static final Path DIRECTORY = Path.of("target", "f148");
    private static final String QUEBEC = "Quebec";
    private static final String DUTCH = "Dutch";
    private static final String FRANCE = "France";
    /** Most spinner clicks one amount may take before the page gives up. */
    private static final int MOST_CLICKS = 120;
    private static final int QUEBEC_CORN = 20;
    private static final int QUEBEC_TOBACCO = 30;
    private static final String GIVE_GOODS = "#foreignTradeGiveGoods";
    private static final String RECEIVE_GOODS = "#foreignTradeReceiveGoods";
    private static final String GIVE_AMOUNT = "#foreignTradeGiveAmount";
    private static final String RECEIVE_AMOUNT = "#foreignTradeReceiveAmount";

    private final FogPage map;
    private Location quebecAt;
    private Location wagonAt;
    private Location seaAt;
    private final Map<Integer, Location> rememberedAt = new HashMap<>();
    private final Map<Integer, Integer> rememberedMoves = new HashMap<>();

    public ForeignColonyTradePage(final TestContext context) {
        super(context);
        map = new FogPage(context);
    }

    /**
     * Write the derived scenario and make it the next game to load. The wagon
     * carries four corn and the frigate two corn; Quebec holds twenty corn,
     * thirty tobacco and no sugar cane; both nations have 1000 gold.
     *
     * @param name      file name of the derived scenario
     * @param deWitt    whether the Dutch Congress holds Jan de Witt
     * @param rebellion whether the Dutch declared independence (Europe closed)
     */
    public void prepare(final String name, final boolean deWitt,
            final boolean rebellion) throws Exception {
        build(name, deWitt, rebellion, List.of(Goods.of(GoodsType.CORN, 4)),
                START_GOLD);
    }

    /**
     * As {@link #prepare(String, boolean, boolean)} with Jan de Witt, but the
     * wagon is full: two sugar cane in its first hold and a full hold of
     * tobacco in its second, so only giving the sugar cane frees room.
     */
    public void prepareFullWagon(final String name) throws Exception {
        build(name, true, false, List.of(Goods.of(GoodsType.SUGAR, 2),
                Goods.of(GoodsType.TOBACCO, 100)), START_GOLD);
    }

    /**
     * As {@link #prepare(String, boolean, boolean)} with Jan de Witt, but the
     * French treasury is empty.
     */
    public void prepareEmptyFrenchTreasury(final String name) throws Exception {
        build(name, true, false, List.of(Goods.of(GoodsType.CORN, 4)), 0);
    }

    private void build(final String name, final boolean deWitt,
            final boolean rebellion, final List<Goods> wagonHolds,
            final int frenchGold) throws Exception {
        final GameModelPo game = new GameModelPoDao().loadFromFile(SOURCE);
        final ModelPo model = game.getModel();
        final ColonyPo quebec = model.getColonies().stream()
                .filter(colony -> QUEBEC.equals(colony.getName())).findFirst()
                .orElseThrow();
        quebecAt = quebec.getLocation();
        // Keep every saved goods entry and change only the three that matter.
        final Map<String, Integer> stock = new HashMap<>(
                quebec.getColonyWarehouse());
        stock.put(GoodsType.CORN.name(), QUEBEC_CORN);
        stock.put(GoodsType.TOBACCO.name(), QUEBEC_TOBACCO);
        stock.put(GoodsType.SUGAR.name(), 0);
        quebec.setColonyWarehouse(stock);
        final List<Location> sites = freeSites(model, quebec);
        if (sites.size() < 2) {
            throw new IllegalStateException(
                    "Quebec needs two free squares beside it, found " + sites);
        }
        wagonAt = sites.get(0);
        seaAt = sites.get(1);
        makeSea(model.getMap(), seaAt);
        model.getUnits()
                .add(carrier(WAGON_ID, UnitType.WAGON, wagonAt, wagonHolds));
        model.getUnits().add(carrier(SHIP_ID, UnitType.FRIGATE, seaAt,
                List.of(Goods.of(GoodsType.CORN, 2))));
        for (final PlayerPo player : model.getGameManager().getPlayers()) {
            if (player.getName().equals(DUTCH)) {
                player.setCongress(congress(deWitt));
                player.setDeclaredIndependence(rebellion);
                // A declaration fixes the colonies the nation owns now
                // (R-0192).
                player.setRebellionColonyIds(
                        rebellion ? ownedColonyIds(model, DUTCH) : null);
                player.setGold(START_GOLD);
            } else if (player.getName().equals(FRANCE)) {
                player.setGold(frenchGold);
            }
        }
        Files.createDirectories(DIRECTORY);
        final File file = DIRECTORY.resolve(name + ".microcol").toFile();
        new GameModelPoDao().saveToFile(file.getPath(), game);
        Mockito.when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(ArgumentMatchers.any(File.class))).thenReturn(file);
    }

    /**
     * Land squares beside the colony that a wagon may stand on and that may
     * become sea: no unit, colony, worked field or land-only overlay on them.
     */
    private static List<Integer> ownedColonyIds(final ModelPo model,
            final String owner) {
        return model.getColonies().stream()
                .filter(colony -> owner.equals(colony.getOwnerName()))
                .map(colony -> colony.getId()).collect(Collectors.toList());
    }

    private static List<Location> freeSites(final ModelPo model,
            final ColonyPo colony) {
        final WorldMapPo world = model.getMap();
        final Map<Location, TerrainType> terrain = world.getTerrainMap();
        final Set<Location> unusable = new HashSet<>();
        unusable.addAll(world.getClearedSet());
        unusable.addAll(world.getRoadSet());
        unusable.addAll(world.getRumorSet());
        unusable.addAll(world.getRiverMap().keySet());
        unusable.addAll(world.getResourceMap().keySet());
        unusable.addAll(world.getMineralReserveMap().keySet());
        world.getRiverSegments().forEach(segment -> {
            unusable.add(segment.getFrom());
            unusable.add(segment.getTo());
        });
        model.getUnits().stream().filter(unit -> unit.getPlaceMap() != null)
                .forEach(
                        unit -> unusable.add(unit.getPlaceMap().getLocation()));
        model.getColonies().forEach(other -> unusable.add(other.getLocation()));
        for (final ColonyPo other : model.getColonies()) {
            for (final ColonyFieldPo field : other.getColonyFields()) {
                if (field.getWorkerId() != null) {
                    unusable.add(other.getLocation().add(field.getDirection()));
                }
            }
        }
        return colony.getLocation().getNeighbors().stream()
                .filter(at -> terrain.containsKey(at)
                        && UnitType.WAGON.canMoveAtTerrain(terrain.get(at)))
                .filter(at -> !unusable.contains(at))
                .collect(Collectors.toList());
    }

    /** Open sea on a free square; its fields and trees go with the land. */
    private static void makeSea(final WorldMapPo world, final Location at) {
        final Map<Location, TerrainType> terrain = world.getTerrainMap();
        terrain.put(at, TerrainType.OCEAN);
        world.setTerrainType(terrain);
        final Set<Location> fields = world.getFieldSet();
        if (fields.remove(at)) {
            world.setFields(fields);
        }
        final Set<Location> trees = world.getTreeSet();
        if (trees.remove(at)) {
            world.setTrees(trees);
        }
    }

    private static CongressPo congress(final boolean deWitt) {
        final CongressPo congress = new CongressPo();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setGeneration(1);
        if (deWitt) {
            congress.setMembers(List.of(Founder.JAN_DE_WITT));
            congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                    Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                    Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        } else {
            congress.setMembers(List.<Founder>of());
            congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                    Founder.THOMAS_JEFFERSON, Founder.JAN_DE_WITT,
                    Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        }
        congress.setJonesDeliveryPending(false);
        return congress;
    }

    private static UnitPo carrier(final int id, final UnitType type,
            final Location at, final List<Goods> holds) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(DUTCH);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        unit.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int i = 0; i < type.getCargoCapacity(); i++) {
            final CargoSlotPo slot = new CargoSlotPo();
            if (i < holds.size()) {
                slot.setGoodsType(holds.get(i).getType());
                slot.setAmount(holds.get(i).getAmount());
            }
            cargo.getSlots().add(slot);
        }
        unit.setCargo(cargo);
        return unit;
    }

    // --- player actions ------------------------------------------------------

    /** Open the trade choice by dragging the wagon onto the colony. */
    public void openWagonTrade() throws Exception {
        openTradeFrom(wagonAt);
    }

    /** Open the trade choice by dragging the frigate onto the colony. */
    public void openShipTrade() throws Exception {
        openTradeFrom(seaAt);
    }

    /**
     * The same drag gesture that would move or attack the colony square. Both
     * squares are scrolled into view with the map keys first.
     */
    private void openTradeFrom(final Location from) throws Exception {
        final GamePage game = GamePage.of(getContext());
        game.panToTile(from);
        game.panToTile(quebecAt);
        game.verifyThatTileIsVisible(from);
        game.verifyThatTileIsVisible(quebecAt);
        map.preview(from, quebecAt);
        getRobot().release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#foreignTradeDialog").tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Choose the goods given to the colony from the open list. */
    public void chooseGive(final GoodsType type) {
        choose(GIVE_GOODS, type);
    }

    /** Choose the goods received from the colony in a barter. */
    public void chooseReceive(final GoodsType type) {
        choose(RECEIVE_GOODS, type);
    }

    /** Open the list with the mouse and pick the entry with the keyboard. */
    private void choose(final String id, final GoodsType type) {
        @SuppressWarnings("unchecked")
        final ComboBox<GoodsType> combo = getNodeFinder().lookup(id)
                .queryAs(ComboBox.class);
        final int index = combo.getItems().indexOf(type);
        if (index < 0) {
            throw new IllegalStateException("Goods is not offered: " + type);
        }
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Click the barter box until it has the wanted state; unticked is a sale.
     */
    public void setBarter(final boolean barter) {
        final CheckBox box = getNodeFinder().lookup("#foreignTradeBarter")
                .queryAs(CheckBox.class);
        if (box.isSelected() != barter) {
            getRobot().clickOn(box);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    /** Click the arrows of the quantity given until it shows the amount. */
    public void setGiveAmount(final int amount) {
        setAmount(GIVE_AMOUNT, amount);
    }

    /** Click the arrows of the quantity received until it shows the amount. */
    public void setReceiveAmount(final int amount) {
        setAmount(RECEIVE_AMOUNT, amount);
    }

    private void setAmount(final String id, final int wanted) {
        @SuppressWarnings("unchecked")
        final Spinner<Integer> spinner = getNodeFinder().lookup(id)
                .queryAs(Spinner.class);
        for (int clicks = 0; clicks < MOST_CLICKS
                && spinner.getValue() != wanted; clicks++) {
            final String arrow = spinner.getValue() < wanted
                    ? ".increment-arrow-button"
                    : ".decrement-arrow-button";
            final Node button = getRobot().from(spinner).lookup(arrow).query();
            getRobot().clickOn(button);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (spinner.getValue() != wanted) {
            throw new IllegalStateException(
                    "The quantity cannot reach " + wanted + " in " + id);
        }
    }

    public void confirm() {
        clickOnButtonWithId("foreignTradeConfirm");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the dialog with its cancel button; nothing is traded. */
    public void cancel() {
        clickOnButtonWithId("foreignTradeCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Remember where both carriers stand and how much they can still move. */
    public void rememberVisitors() {
        for (final int id : new int[] { WAGON_ID, SHIP_ID }) {
            rememberedAt.put(id, getModel().getUnitById(id).getLocation());
            rememberedMoves.put(id,
                    getModel().getUnitById(id).getMovementThirds());
        }
    }

    /** Leave the game through the menu and Continue the saved game. */
    public void exitAndContinue() {
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
    }

    // --- expected values -----------------------------------------------------

    /** English net line: positive means the colony pays the carrier's owner. */
    public static String netEnglish(final int net) {
        if (net == 0) {
            return "No gold changes hands.";
        }
        return net > 0 ? "The colony pays you " + net + " gold."
                : "You pay the colony " + -net + " gold.";
    }

    /** Czech net line, the same rule as {@link #netEnglish(int)}. */
    public static String netCzech(final int net) {
        if (net == 0) {
            return "Žádné zlato nepřechází.";
        }
        return net > 0 ? "Kolonie vám zaplatí " + net + " zlata."
                : "Zaplatíte kolonii " + -net + " zlata.";
    }

    /** Exact expected gold of selling this many pieces at the colony's bid. */
    public int expectedSale(final GoodsType given, final int amount) {
        return amount * bid(given);
    }

    /** Exact net gold the colony pays for the barter; negative when paid. */
    public int expectedBarterNet(final GoodsType given, final int amount,
            final GoodsType received, final int receivedAmount) {
        return amount * bid(given) - receivedAmount * ask(received);
    }

    private int bid(final GoodsType type) {
        return getModel().getEurope()
                .getGoodsTradeForType(type, EuropeanNation.FRENCH)
                .getSellPrice();
    }

    private int ask(final GoodsType type) {
        return getModel().getEurope()
                .getGoodsTradeForType(type, EuropeanNation.FRENCH)
                .getBuyPrice();
    }

    // --- explicit observations -----------------------------------------------

    public void verifyDialogOpen(final boolean open) {
        assertEquals(open, isCssIdVisible("foreignTradeDialog"));
    }

    /** The exact give, receive and net lines shown in the dialog. */
    public void verifyLines(final String give, final String receive,
            final String net) {
        assertEquals(give, label("foreignTradeGive"));
        assertEquals(receive, label("foreignTradeReceive"));
        assertEquals(net, label("foreignTradeNet"));
    }

    public void verifyFeedback(final String expected) {
        assertEquals(expected, label("foreignTradeFeedback"));
    }

    public void verifyConfirmEnabled(final boolean enabled) {
        assertEquals(enabled,
                !getButtonById("foreignTradeConfirm").isDisabled());
    }

    /** Whether the controls of the goods received are usable (barter). */
    public void verifyReceiveControlsEnabled(final boolean enabled) {
        assertEquals(enabled,
                !getNodeFinder().lookup(RECEIVE_GOODS).query().isDisabled());
        assertEquals(enabled,
                !getNodeFinder().lookup(RECEIVE_AMOUNT).query().isDisabled());
    }

    /** The most the model lets the player receive of the chosen goods. */
    public void verifyReceiveMaximum(final int expected) {
        @SuppressWarnings("unchecked")
        final Spinner<Integer> spinner = getNodeFinder().lookup(RECEIVE_AMOUNT)
                .queryAs(Spinner.class);
        final SpinnerValueFactory.IntegerSpinnerValueFactory range = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner
                .getValueFactory();
        assertEquals(expected, range.getMax());
    }

    public void verifyGold(final String player, final int gold) {
        assertEquals(gold, getModel().getPlayerByName(player).getGold());
    }

    public void verifyCarried(final int unitId, final GoodsType type,
            final int amount) {
        final UnitWithCargo unit = (UnitWithCargo) getModel()
                .getUnitById(unitId);
        assertEquals(amount,
                unit.getCargo().getSlots().stream()
                        .flatMap(slot -> slot.getGoods().stream())
                        .filter(goods -> goods.getType().equals(type))
                        .mapToInt(goods -> goods.getAmount()).sum());
    }

    public void verifyColonyStock(final GoodsType type, final int amount) {
        assertEquals(amount, getModel().getColonyAt(quebecAt).orElseThrow()
                .getWarehouse().getGoods(type).getAmount());
    }

    /** The visit never captures or changes the owner of Quebec. */
    public void verifyColonyOwner(final String expectedOwner) {
        assertEquals(expectedOwner, getModel().getColonyAt(quebecAt)
                .orElseThrow().getOwner().getName());
    }

    /** Both carriers stand where they did and kept all their movement. */
    public void verifyVisitorsUnmoved() {
        for (final int id : new int[] { WAGON_ID, SHIP_ID }) {
            assertEquals(rememberedAt.get(id),
                    getModel().getUnitById(id).getLocation());
            assertEquals(rememberedMoves.get(id), Integer
                    .valueOf(getModel().getUnitById(id).getMovementThirds()));
        }
    }

    private String label(final String id) {
        return getLabeledById(id).getText();
    }
}
