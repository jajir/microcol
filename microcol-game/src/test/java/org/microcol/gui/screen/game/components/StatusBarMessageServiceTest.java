package org.microcol.gui.screen.game.components;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.Loc;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.preferences.DeveloperMode;
import org.microcol.gui.util.StreamReader;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.collect.Lists;

public class StatusBarMessageServiceTest {

    private StatusBarMessageService service;

    private final I18n i18n = I18n.builder()
            .setVerifyThatAllEnumKeysAreDefined(true)
            .setStreamReader(new StreamReader())
            .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
            .setDefaultLocale(new Locale("cs", "CZ")).build();

    private DeveloperMode developerMode;

    private final LocalizationHelper localizationHelper = new LocalizationHelper(
            i18n);

    private final Player player = mock(Player.class);

    private final Colony colony = mock(Colony.class);

    private final Unit unit1 = mock(Unit.class);

    private final Unit unit2 = mock(Unit.class);

    private final Unit unit3 = mock(Unit.class);

    private final Unit unit4 = mock(Unit.class);

    private final Unit unit5 = mock(Unit.class);

    private final Unit unit6 = mock(Unit.class);

    private final Location where = Location.of(22, 18);

    @Test
    public void verify_cźech_locale_is_set() {
        assertEquals("Zlato", i18n.get(Loc.statusBar_gold),
                "Seems that Czech language wasn't set.");
    }

    @Test
    public void verify_message_that_location_was_not_discovered()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(false);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit1), Optional.empty());

        assertEquals("Tato oblast je neprozkoumaná.", ret);
    }

    @Test
    public void verify_location_is_discovered_without_ships() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, new ArrayList<>(), Optional.empty());

        assertEquals("Zde je Oceán.", ret);
    }

    @Test
    public void verify_one_ship_of_same_type() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit1), Optional.empty());

        assertEquals("Zde je Oceán s galeonou.", ret);
    }

    @Test
    public void verify_two_ship_of_same_type() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit1, unit2),
                Optional.empty());

        assertEquals("Zde je Oceán s dvěmi galeonami.", ret);
    }

    @Test
    public void verify_five_ship_of_same_type() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.GALLEON);
        when(unit5.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4, unit5),
                Optional.empty());

        assertEquals("Zde je Oceán s pěti galeonami.", ret);
    }

    @Test
    public void verify_six_ship_of_same_type() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.GALLEON);
        when(unit5.getType()).thenReturn(UnitType.GALLEON);
        when(unit6.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4, unit5, unit6),
                Optional.empty());

        assertEquals("Zde je Oceán s 6. galeonami.", ret);
    }

    @Test
    public void verify_colonist_two_galleons_one_frigate__of_same_type()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.COLONIST);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4),
                Optional.empty());

        assertEquals(
                "Zde je Oceán s dvěmi galeonami, svobodným kolonistou a frigatou.",
                ret);
    }

    @Test
    public void verify_two_galleons_one_frigate__of_same_type()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit2, unit3, unit4),
                Optional.empty());

        assertEquals("Zde je Oceán s dvěmi galeonami a frigatou.", ret);
    }

    @Test
    public void verify_coloy() throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(colony.getName()).thenReturn("Kuchařovice");

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, new ArrayList<>(), Optional.of(colony));

        assertEquals("Zde je Oceán s kolonií Kuchařovice.", ret);
    }

    @Test
    public void verify_coloy_two_galleons_one_frigate__of_same_type()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);
        when(colony.getName()).thenReturn("Kuchařovice");
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit2, unit3, unit4),
                Optional.of(colony));

        assertEquals(
                "Zde je Oceán s kolonií Kuchařovice, dvěmi galeonami a frigatou.",
                ret);
    }

    @Test
    public void verify_explored_tile_outside_present_sight_hides_objects()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(false);
        when(colony.getName()).thenReturn("Kuchařovice");
        when(unit1.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player,
                TerrainType.OCEAN, Lists.newArrayList(unit1),
                Optional.of(colony));

        assertEquals("Zde je Oceán.", ret);
    }

    @Test
    public void verify_lost_city_rumor_is_named_in_present_sight_only()
            throws Exception {
        developerMode.setEnabled(false);
        when(player.isVisible(where)).thenReturn(true);
        when(player.isCurrentlyVisible(where)).thenReturn(true);

        assertEquals(
                "Zde je Zelená země. Pověst o ztraceném městě: zvěd, který sem vstoupí, zjistí, co skrývá.",
                service.getStatusMessage(where, player, TerrainType.GRASSLAND,
                        new ArrayList<>(), Optional.empty(), Optional.empty(),
                        true));
        assertEquals("Zde je Zelená země.",
                service.getStatusMessage(where, player, TerrainType.GRASSLAND,
                        new ArrayList<>(), Optional.empty(), Optional.empty(),
                        false));

        // A remembered square never tells whether its rumor still waits.
        when(player.isCurrentlyVisible(where)).thenReturn(false);
        assertEquals("Zde je Zelená země.",
                service.getStatusMessage(where, player, TerrainType.GRASSLAND,
                        new ArrayList<>(), Optional.empty(), Optional.empty(),
                        true));

        // An unexplored square says nothing about a rumor either.
        when(player.isVisible(where)).thenReturn(false);
        assertEquals("Tato oblast je neprozkoumaná.",
                service.getStatusMessage(where, player, TerrainType.GRASSLAND,
                        new ArrayList<>(), Optional.empty(), Optional.empty(),
                        true));
    }

    @Test
    void developerCoordinatesToggleWithoutRevealingUnknownTile() {
        when(player.isVisible(where)).thenReturn(false);
        developerMode.setEnabled(true);
        assertEquals("x=22, y=18 · Tato oblast je neprozkoumaná.",
                service.getStatusMessage(where, player, TerrainType.OCEAN,
                        Lists.newArrayList(unit1), Optional.of(colony)));
        org.mockito.Mockito.verifyZeroInteractions(unit1, colony);
        developerMode.setEnabled(false);
        assertEquals("Tato oblast je neprozkoumaná.",
                service.getStatusMessage(where, player, TerrainType.OCEAN,
                        Lists.newArrayList(unit1), Optional.of(colony)));
    }

    @Test
    void coordinatesUseModelValuesAndPreserveOrdinaryDescription() {
        when(player.isVisible(org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);
        when(player.isCurrentlyVisible(org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);
        for (final Location location : List.of(Location.of(1, 1),
                Location.of(60, 40))) {
            developerMode.setEnabled(false);
            final String ordinary = service.getStatusMessage(location, player,
                    TerrainType.OCEAN, List.of(), Optional.empty());
            developerMode.setEnabled(true);
            assertEquals(
                    "x=" + location.getX() + ", y=" + location.getY() + " · "
                            + ordinary,
                    service.getStatusMessage(location, player,
                            TerrainType.OCEAN, List.of(), Optional.empty()));
        }
    }

    @BeforeEach
    private void startup() {
        final org.microcol.gui.preferences.SettingService settings = mock(
                org.microcol.gui.preferences.SettingService.class);
        when(settings.load())
                .thenReturn(new org.microcol.gui.preferences.Setting());
        developerMode = new DeveloperMode(settings);
        service = new StatusBarMessageService(i18n, developerMode,
                localizationHelper);
    }

    @AfterEach
    private void tearDown() {
        service = null;
    }

}
