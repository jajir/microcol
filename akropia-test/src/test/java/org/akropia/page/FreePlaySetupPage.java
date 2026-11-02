package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.Player;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.WorldMap;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.generation.GeneratedClimate;
import org.akropia.model.generation.GeneratedLandShare;
import org.akropia.model.generation.GeneratedWorld;
import org.akropia.model.generation.NorthAmericaWorld;
import org.akropia.model.store.ModelPo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public final class FreePlaySetupPage extends AbstractScreen {
    /** Four small bundled maps, the generated world and North America. */
    private static final int MAP_CHOICES = 6;

    public FreePlaySetupPage(final TestContext context) {
        super(context);
    }

    public void selectMap(final int index) {
        select("freePlayMap", index);
    }

    public void verifyOriginalWorldLandscape() {
        final WorldMap map = getModel().getMap();
        assertTrue(map.getTerrainAt(Location.of(24, 14)).isHasTrees());
        assertEquals(ResourceOverlay.GAME,
                map.getResourceOverlayAt(Location.of(24, 14)));
        assertEquals(ResourceOverlay.PRIME_FOOD,
                map.getResourceOverlayAt(Location.of(25, 14)));
        assertEquals(ResourceOverlay.FISHERY,
                map.getResourceOverlayAt(Location.of(19, 8)));
        assertEquals(TerrainType.LAKE,
                map.getTerrainTypeAt(Location.of(19, 8)));
        assertEquals(TerrainType.LAKE,
                map.getTerrainTypeAt(Location.of(15, 12)));
        assertEquals(TerrainType.GRASSLAND,
                map.getTerrainTypeAt(Location.of(20, 8)));
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(22, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(22, 13), Location.of(23, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(23, 13), Location.of(24, 13)));
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(25, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(24, 13), Location.of(25, 13)));
        assertTrue(map.hasLostCityRumorAt(Location.of(18, 25)));
    }

    public void verifyShipOnWater(final int shipId,
            final Location expectedLocation,
            final TerrainType expectedTerrain) {
        assertEquals(expectedLocation,
                getModel().getUnitById(shipId).getLocation());
        assertEquals(expectedTerrain,
                getModel().getMap().getTerrainTypeAt(expectedLocation));
        assertTrue(getModel().getPlayerByName("Dutch")
                .isCurrentlyVisible(expectedLocation));
    }

    public void verifyLakeSeparatedFromSea(final Location lake) {
        final WorldMap map = getModel().getMap();
        assertEquals(TerrainType.LAKE, map.getTerrainTypeAt(lake));
        assertTrue(lake.getNeighbors().stream().filter(map::isValid)
                .allMatch(at -> !map.getTerrainTypeAt(at).isSea()
                        || map.getTerrainTypeAt(at) == TerrainType.LAKE));
    }

    public void selectNation(final EuropeanNation nation) {
        select("freePlayNation", nation.ordinal());
    }

    public void selectDifficulty(final Difficulty difficulty) {
        final String name = getContext()
                .getClassFromGuice(org.akropia.gui.LocalizationHelper.class)
                .getDifficultyName(difficulty);
        getRobot().clickOn("#freePlayDifficulty");
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node item = getListOfNodes(".context-menu .label")
                .stream()
                .filter(node -> name.equals(
                        ((javafx.scene.control.Labeled) node).getText()))
                .findFirst().orElseThrow();
        getRobot().clickOn(item);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDifficultySelection(final Difficulty expected,
            final String expectedLabel) {
        final ChoiceBox<?> choice = getNodeFinder()
                .lookup("#freePlayDifficulty").queryAs(ChoiceBox.class);
        assertTrue(choice.isVisible());
        assertEquals(5, choice.getItems().size());
        assertEquals(expected.ordinal(),
                choice.getSelectionModel().getSelectedIndex());
        final String selected = choice.getConverter() == null
                ? String.valueOf(choice.getSelectionModel().getSelectedItem())
                : difficultyLabel(choice);
        assertEquals(expectedLabel, selected);
    }

    private <T> String difficultyLabel(final ChoiceBox<T> choice) {
        return choice.getConverter()
                .toString(choice.getSelectionModel().getSelectedItem());
    }

    public void verifyGameDifficulty(final Difficulty expected,
            final String expectedLabel) {
        assertEquals(expected, getModel().getDifficulty());
        assertEquals(expected, getModel().save().getDifficulty());
        final javafx.scene.control.Labeled label = getLabeledById(
                "gameDifficulty");
        assertTrue(label.isVisible());
        assertTrue(label.getText().contains(expectedLabel), label.getText());
    }

    /**
     * @return the recovery save Continue opens: the initial turn snapshot, or
     *         the latest position when leaving saved the game (R-0387)
     */
    private java.io.File continueFile() {
        return getContext()
                .getClassFromGuice(
                        org.akropia.gui.dialog.PersistingService.class)
                .continueFile();
    }

    public void verifySavedDifficulty(final Difficulty expected) {
        final java.io.File file = continueFile();
        final org.akropia.model.campaign.po.GameModelPo data = new org.akropia.model.campaign.po.GameModelPoDao()
                .loadPlayerGame(file);
        assertEquals(expected, data.getModel().getDifficulty());
    }

    public void selectClimate(final GeneratedClimate climate) {
        select("freePlayClimate", climate.ordinal());
    }

    public void selectLandShare(final GeneratedLandShare landShare) {
        select("freePlayLandShare", landShare.ordinal());
    }

    public void enterSeed(final String value) {
        final TextField input = getNodeFinder().lookup("#freePlaySeed")
                .queryAs(TextField.class);
        getRobot().clickOn(input).push(KeyCode.END)
                .eraseText(input.getText().length()).write(value);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyGeneratedSetup(final String expectedPreview) {
        assertTrue(isCssIdVisible("freePlayGenerated"));
        verifySelectedMap(4, "Generated new world");
        verifyNationSelection(EuropeanNation.DUTCH, true);
        assertEquals("Choose the nation you will play.",
                getLabeledById("freePlayNationNote").getText());
        verifyPreview(expectedPreview);
    }

    public void verifyPreview(final String expectedPreview) {
        assertEquals(expectedPreview,
                getLabeledById("freePlayGeneratedPreview").getText());
    }

    public void verifyGeneratedControlsHidden() {
        assertFalse(getNodeFinder().lookup("#freePlayGenerated")
                .queryAs(javafx.scene.Node.class).isVisible());
    }

    public void verifySeedRejected(final String expectedError) {
        assertEquals(expectedError,
                getLabeledById("freePlayNameError").getText());
        assertTrue(getButtonById("freePlayStart").isDisabled());
    }

    public void verifyGeneratedWorld(final int expectedSeed,
            final GeneratedClimate expectedClimate,
            final GeneratedLandShare expectedLandShare,
            final String expectedPlayerName) throws Exception {
        WaitForAsyncUtils.waitFor(15, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().isGameRunning());
        final ModelPo saved = getModel().save();
        assertEquals(expectedSeed,
                saved.getGeneratedWorld().getSeed().intValue());
        assertEquals(expectedClimate, saved.getGeneratedWorld().getClimate());
        assertEquals(expectedLandShare,
                saved.getGeneratedWorld().getLandShare());
        assertEquals(GeneratedWorld.WIDTH,
                getModel().getMap().getMaxLocationX());
        assertEquals(GeneratedWorld.HEIGHT,
                getModel().getMap().getMaxLocationY());
        assertEquals(expectedPlayerName,
                getModel().getPlayerByName("Dutch").getDisplayName());
        assertTrue(saved.getNativeSettlements().size() >= 3);
    }

    public void verifyGeneratedRiversAvoidCoastAndUplands() {
        final WorldMap map = getModel().getMap();
        final java.util.Set<Location> rivers = getModel().save().getMap()
                .getRiverMap().keySet();
        assertFalse(rivers.isEmpty());
        for (final Location at : rivers) {
            final TerrainType type = map.getTerrainTypeAt(at);
            assertFalse(type.isSea() || type == TerrainType.HILL
                    || type == TerrainType.MOUNTAIN, at.toString());
            for (final Location neighbour : at.getNeighbors()) {
                if (map.isValid(neighbour)) {
                    assertFalse(map.getTerrainTypeAt(neighbour).isSea(),
                            at.toString());
                }
            }
        }
    }

    public void verifySavedGeneratedWorld(final int expectedSeed)
            throws Exception {
        final java.io.File file = continueFile();
        final GameModelPo data = new GameModelPoDao().loadPlayerGame(file);
        assertEquals(expectedSeed,
                data.getModel().getGeneratedWorld().getSeed().intValue());
        assertArrayEquals(getModel().save().getMap().getTiles(),
                data.getModel().getMap().getTiles());
    }

    private void select(final String id, final int index) {
        final ChoiceBox<?> choice = getNodeFinder().lookup("#" + id)
                .queryAs(ChoiceBox.class);
        final String label = labelOf(choice, choice.getItems().get(index));
        getRobot().clickOn(choice);
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.Node item = getListOfNodes(".context-menu .label")
                .stream()
                .filter(node -> label.equals(
                        ((javafx.scene.control.Labeled) node).getText()))
                .findFirst().orElseThrow();
        getRobot().clickOn(item);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Localized text shown for an item; enum boxes have a converter. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String labelOf(final ChoiceBox<?> choice,
            final Object item) {
        if (choice.getConverter() == null) {
            return String.valueOf(item);
        }
        return ((javafx.util.StringConverter) choice.getConverter())
                .toString(item);
    }

    public void enterName(final String value) {
        final TextField input = getNodeFinder().lookup("#freePlayPlayerName")
                .queryAs(TextField.class);
        getRobot().clickOn(input).push(KeyCode.END)
                .eraseText(input.getText().length()).write(value);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public GamePage start() {
        clickOnButtonWithId("freePlayStart");
        return GamePage.of(getContext());
    }

    public GamePage startWithEnter() {
        getRobot().clickOn("#freePlayPlayerName").push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        return GamePage.of(getContext());
    }

    public WelcomePage back() {
        clickOnButtonWithId("freePlayBack");
        return WelcomePage.of(getContext());
    }

    public WelcomePage escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        return WelcomePage.of(getContext());
    }

    public void verifySetup(final int expectedMapIndex) {
        assertTrue(isCssIdVisible("freePlaySetup"));
        final ChoiceBox<?> map = getNodeFinder().lookup("#freePlayMap")
                .queryAs(ChoiceBox.class);
        assertEquals(MAP_CHOICES, map.getItems().size());
        verifyGeneratedControlsHidden();
        assertEquals(expectedMapIndex,
                map.getSelectionModel().getSelectedIndex());
        assertEquals("This scenario is fixed to the Netherlands.",
                getLabeledById("freePlayNationNote").getText());
        verifyNationSelection(EuropeanNation.DUTCH, false);
    }

    public void verifyNationSelection(final EuropeanNation expected,
            final boolean enabled) {
        final ChoiceBox<?> nation = getNodeFinder().lookup("#freePlayNation")
                .queryAs(ChoiceBox.class);
        assertEquals(4, nation.getItems().size());
        assertEquals(expected.ordinal(),
                nation.getSelectionModel().getSelectedIndex());
        assertEquals(enabled, !nation.isDisabled());
    }

    public void verifySelectedMap(final int expectedIndex,
            final String expectedName) {
        final ChoiceBox<?> map = getNodeFinder().lookup("#freePlayMap")
                .queryAs(ChoiceBox.class);
        assertEquals(MAP_CHOICES, map.getItems().size());
        assertEquals(expectedIndex, map.getSelectionModel().getSelectedIndex());
        assertEquals(expectedName, map.getSelectionModel().getSelectedItem());
    }

    public void verifySetupLanguage(final String expectedStart,
            final String expectedNote) {
        assertEquals(expectedStart, getButtonById("freePlayStart").getText());
        assertEquals(expectedNote,
                getLabeledById("freePlayNationNote").getText());
    }

    public void verifyNameAccepted(final boolean expected) {
        assertEquals(expected, !getButtonById("freePlayStart").isDisabled());
        assertEquals(expected,
                getLabeledById("freePlayNameError").getText().isEmpty());
    }

    public void verifyReferenceExpedition(final EuropeanNation selected,
            final String playerName, final int gold) throws Exception {
        final Player human = getModel()
                .getPlayerByName(selected.getPlayerName());
        WaitForAsyncUtils.waitFor(15, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().isGameRunning()
                        && getModel().getCurrentPlayer() == human);
        assertEquals(playerName, human.getDisplayName());
        assertTrue(human.isHuman());
        assertEquals(gold, human.getGold());
        assertEquals(8, getModel().getPlayers().size());
        assertEquals(12, getModel().getAllUnits().size());
        assertEquals(1, getModel().getPlayers().stream().filter(Player::isHuman)
                .count());
        for (org.akropia.model.EuropeanNation nation : org.akropia.model.EuropeanNation
                .values()) {
            Player participant = getModel()
                    .getPlayerByName(nation.getPlayerName());
            assertEquals(3, participant.getAllUnits().size());
            assertEquals(gold, participant.getGold());
            assertEquals(participant != human, participant.isComputer());
            org.akropia.model.PlayerKing crown = (org.akropia.model.PlayerKing) getModel()
                    .getPlayerByName(nation.getPlayerName() + "'s King");
            assertSame(participant, crown.getWhosKingThisPlayerIs());
            for (org.akropia.model.EuropeanNation other : org.akropia.model.EuropeanNation
                    .values()) {
                if (other != nation) {
                    assertEquals(org.akropia.model.EuropeanRelationState.PEACE,
                            getModel().getEuropeanRelation(participant,
                                    getModel().getPlayerByName(
                                            other.getPlayerName())));
                }
            }
        }
        for (final EuropeanNation nation : EuropeanNation.values()) {
            final Player participant = getModel()
                    .getPlayerByName(nation.getPlayerName());
            if (nation != selected) {
                continue;
            }
            final org.akropia.model.unit.Ship ship = (org.akropia.model.unit.Ship) participant
                    .getAllUnits().stream()
                    .filter(org.akropia.model.Unit::isShip).findFirst()
                    .orElseThrow();
            assertSame(
                    nation == EuropeanNation.DUTCH
                            ? org.akropia.model.UnitType.MERCHANTMAN
                            : org.akropia.model.UnitType.CARAVEL,
                    ship.getType());
            assertEquals(ship.getType().getCargoCapacity(),
                    ship.getCargo().getSlots().size());
            assertEquals(TerrainType.HIGH_SEA,
                    getModel().getMap().getTerrainTypeAt(ship.getLocation()));
            final java.util.List<org.akropia.model.unit.UnitFreeColonist> passengers = ship
                    .getCargo().getSlots().stream()
                    .flatMap(slot -> slot.getUnit().stream())
                    .map(org.akropia.model.unit.UnitFreeColonist.class::cast)
                    .collect(java.util.stream.Collectors.toList());
            assertEquals(2, passengers.size());
            assertEquals(1, passengers.stream().filter(
                    org.akropia.model.unit.UnitFreeColonist::isHoldingWeapons)
                    .count());
            assertEquals(1, passengers.stream()
                    .filter(unit -> unit.getTools() == 100).count());
            for (final org.akropia.model.unit.UnitFreeColonist unit : passengers) {
                assertSame(participant, unit.getOwner());
                assertSame(org.akropia.model.UnitType.COLONIST, unit.getType());
                assertFalse(unit.isMounted());
            }
            assertTrue(ship.getCargo().getSlots().stream()
                    .noneMatch(slot -> slot.isLoadedGood()));
        }
    }

    public java.util.List<Integer> referenceUnitIds() {
        return getModel().getAllUnits().stream()
                .map(org.akropia.model.Unit::getId).sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public void verifyReferenceUnitIds(final java.util.List<Integer> expected) {
        assertEquals(expected, referenceUnitIds());
    }

    public void verifyNoActiveGame() {
        assertFalse(getContext().getClassFromGuice(GameModelController.class)
                .isGameModelReady());
    }

    /**
     * The running world is the authored North America map: size, seed and
     * landmarks that differ from every small bundled and generated map.
     */
    public void verifyNorthAmericaGeography() {
        final WorldMap map = getModel().getMap();
        assertEquals(NorthAmericaWorld.WIDTH, map.getMaxLocationX());
        assertEquals(NorthAmericaWorld.HEIGHT, map.getMaxLocationY());
        assertEquals(NorthAmericaWorld.SEED, map.getSeed().intValue());
        // Hudson Bay, the Gulf of Mexico and Lake Superior.
        assertEquals(TerrainType.OCEAN,
                map.getTerrainTypeAt(Location.of(63, 20)));
        assertEquals(TerrainType.OCEAN,
                map.getTerrainTypeAt(Location.of(60, 58)));
        assertEquals(TerrainType.LAKE,
                map.getTerrainTypeAt(Location.of(62, 31)));
        // Florida, Cuba and the Rocky Mountains.
        assertTrue(map.getTerrainTypeAt(Location.of(67, 56)).isLand());
        assertTrue(map.getTerrainTypeAt(Location.of(68, 61)).isLand());
        assertEquals(TerrainType.MOUNTAIN,
                map.getTerrainTypeAt(Location.of(42, 30)));
        // The Mississippi.
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(57, 35)));
        assertTrue(
                map.hasRiverSegment(Location.of(57, 35), Location.of(57, 36)));
    }

    /**
     * Every European nation, its Crown and the six native nations of the North
     * America world take part; the selected nation is the player.
     */
    public void verifyNorthAmericaExpedition(final EuropeanNation selected,
            final String playerName, final int gold,
            final Location expectedShipStart) throws Exception {
        final Player human = getModel()
                .getPlayerByName(selected.getPlayerName());
        WaitForAsyncUtils.waitFor(15, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().isGameRunning()
                        && getModel().getCurrentPlayer() == human);
        assertEquals(playerName, human.getDisplayName());
        assertTrue(human.isHuman());
        assertEquals(gold, human.getGold());
        assertEquals(1, getModel().getPlayers().stream().filter(Player::isHuman)
                .count());
        assertEquals(14, getModel().getPlayers().size());
        assertEquals(6, getModel().getPlayers().stream()
                .filter(Player::isNative).count());
        final org.akropia.model.Unit ship = human.getAllUnits().stream()
                .filter(org.akropia.model.Unit::isShip).findFirst()
                .orElseThrow();
        assertEquals(expectedShipStart, ship.getLocation());
        assertEquals(TerrainType.HIGH_SEA,
                getModel().getMap().getTerrainTypeAt(ship.getLocation()));
        final ModelPo saved = getModel().save();
        assertEquals(12, saved.getNativeSettlements().size());
        final java.util.Set<Integer> homes = saved.getNativeSettlements()
                .stream().map(settlement -> settlement.getId())
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(24, saved.getUnits().stream()
                .filter(unit -> unit.getNativeHomeSettlementId() != null
                        && homes.contains(unit.getNativeHomeSettlementId()))
                .count());
    }

    /** The recovery save holds the same North America map as the game. */
    public void verifySavedNorthAmericaWorld() {
        final java.io.File file = continueFile();
        final GameModelPo data = new GameModelPoDao().loadPlayerGame(file);
        assertEquals(NorthAmericaWorld.WIDTH,
                data.getModel().getMap().getMaxX());
        assertEquals(NorthAmericaWorld.HEIGHT,
                data.getModel().getMap().getMaxY());
        assertArrayEquals(getModel().save().getMap().getTiles(),
                data.getModel().getMap().getTiles());
        assertEquals(getModel().save().getNativeSettlements().size(),
                data.getModel().getNativeSettlements().size());
    }

    public void verifyWorld(final int expectedSeed, final int expectedCityX,
            final String expectedPlayerName) {
        assertEquals(expectedSeed, getModel().getMap().getSeed());
        assertEquals(50, getModel().getMap().getMaxLocationX());
        assertEquals(30, getModel().getMap().getMaxLocationY());
        final Player human = getModel().getPlayerByName("Dutch");
        final Player france = getModel().getPlayerByName("France");
        assertTrue(human.isHuman());
        assertEquals(expectedPlayerName, human.getDisplayName());
        assertSame(human, getModel().getCurrentPlayer());
        assertTrue(france.isComputer());
        assertFalse(france.isKing());
        assertEquals(2, france.getColonies().size());
        assertEquals(3, france.getAllUnits().size());
        assertEquals(Location.of(expectedCityX, 10),
                getModel().getColonyById(0).orElseThrow().getLocation());
        assertEquals(Location.of(expectedCityX, expectedCityX == 17 ? 21 : 20),
                getModel().getColonyById(1).orElseThrow().getLocation());
        assertTrue(france.getColonies().stream()
                .allMatch(c -> c.getOwner() == france));
        assertSame(human, getModel().getUnitById(3).getOwner());
        assertEquals(Location.of(45, 15),
                getModel().getUnitById(3).getLocation());
        assertEquals(TerrainType.OCEAN,
                getModel().getMap().getTerrainTypeAt(Location.of(45, 15)));
    }
}
