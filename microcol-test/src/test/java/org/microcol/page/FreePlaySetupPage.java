package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.RiverSize;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.generation.GeneratedClimate;
import org.microcol.model.generation.GeneratedLandShare;
import org.microcol.model.generation.GeneratedWorld;
import org.microcol.model.store.ModelPo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public final class FreePlaySetupPage extends AbstractScreen {
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
                .getClassFromGuice(org.microcol.gui.LocalizationHelper.class)
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

    public void verifySavedDifficulty(final Difficulty expected) {
        final java.io.File file = getContext()
                .getClassFromGuice(
                        org.microcol.model.campaign.po.PersistingTool.class)
                .getAutoSaveFile();
        final org.microcol.model.campaign.po.GameModelPo data = new org.microcol.model.campaign.po.GameModelPoDao()
                .loadFromFile(file);
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
        final java.io.File file = getContext()
                .getClassFromGuice(PersistingTool.class).getAutoSaveFile();
        final GameModelPo data = new GameModelPoDao().loadFromFile(file);
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
        assertEquals(5, map.getItems().size());
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
        assertEquals(5, map.getItems().size());
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
        for (org.microcol.model.EuropeanNation nation : org.microcol.model.EuropeanNation
                .values()) {
            Player participant = getModel()
                    .getPlayerByName(nation.getPlayerName());
            assertEquals(3, participant.getAllUnits().size());
            assertEquals(gold, participant.getGold());
            assertEquals(participant != human, participant.isComputer());
            org.microcol.model.PlayerKing crown = (org.microcol.model.PlayerKing) getModel()
                    .getPlayerByName(nation.getPlayerName() + "'s King");
            assertSame(participant, crown.getWhosKingThisPlayerIs());
            for (org.microcol.model.EuropeanNation other : org.microcol.model.EuropeanNation
                    .values()) {
                if (other != nation) {
                    assertEquals(org.microcol.model.EuropeanRelationState.PEACE,
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
            final org.microcol.model.unit.Ship ship = (org.microcol.model.unit.Ship) participant
                    .getAllUnits().stream()
                    .filter(org.microcol.model.Unit::isShip).findFirst()
                    .orElseThrow();
            assertSame(
                    nation == EuropeanNation.DUTCH
                            ? org.microcol.model.UnitType.MERCHANTMAN
                            : org.microcol.model.UnitType.CARAVEL,
                    ship.getType());
            assertEquals(ship.getType().getCargoCapacity(),
                    ship.getCargo().getSlots().size());
            assertEquals(TerrainType.HIGH_SEA,
                    getModel().getMap().getTerrainTypeAt(ship.getLocation()));
            final java.util.List<org.microcol.model.unit.UnitFreeColonist> passengers = ship
                    .getCargo().getSlots().stream()
                    .flatMap(slot -> slot.getUnit().stream())
                    .map(org.microcol.model.unit.UnitFreeColonist.class::cast)
                    .collect(java.util.stream.Collectors.toList());
            assertEquals(2, passengers.size());
            assertEquals(1, passengers.stream().filter(
                    org.microcol.model.unit.UnitFreeColonist::isHoldingGuns)
                    .count());
            assertEquals(1, passengers.stream()
                    .filter(unit -> unit.getTools() == 100).count());
            for (final org.microcol.model.unit.UnitFreeColonist unit : passengers) {
                assertSame(participant, unit.getOwner());
                assertSame(org.microcol.model.UnitType.COLONIST,
                        unit.getType());
                assertFalse(unit.isMounted());
            }
            assertTrue(ship.getCargo().getSlots().stream()
                    .noneMatch(slot -> slot.isLoadedGood()));
        }
    }

    public java.util.List<Integer> referenceUnitIds() {
        return getModel().getAllUnits().stream()
                .map(org.microcol.model.Unit::getId).sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public void verifyReferenceUnitIds(final java.util.List<Integer> expected) {
        assertEquals(expected, referenceUnitIds());
    }

    public void verifyNoActiveGame() {
        assertFalse(getContext().getClassFromGuice(GameModelController.class)
                .isGameModelReady());
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
