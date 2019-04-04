package org.microcol.gui.screen.game.components;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.Loc;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.preferences.GamePreferences;
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

    private final I18n i18n = I18n.builder().setVerifyThatAllEnumKeysAreDefined(true)
            .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
            .setDefaultLocale(new Locale("cs", "CZ")).build();

    private final GamePreferences gamePreferences = mock(GamePreferences.class);

    private final LocalizationHelper localizationHelper = new LocalizationHelper(i18n);

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
    public void verify_message_that_location_was_not_discovered() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(false);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1), Optional.empty());

        assertEquals("Tato oblast je neprozkoumaná.", ret);
    }

    @Test
    public void verify_location_is_discovered_without_ships() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                new ArrayList<>(), Optional.empty());

        assertEquals("Zde je Oceán.", ret);
    }

    @Test
    public void verify_one_ship_of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1), Optional.empty());

        assertEquals("Zde je Oceán s galeonou.", ret);
    }

    @Test
    public void verify_two_ship_of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2), Optional.empty());

        assertEquals("Zde je Oceán s dvěmi galeonami.", ret);
    }

    @Test
    public void verify_five_ship_of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.GALLEON);
        when(unit5.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4, unit5), Optional.empty());

        assertEquals("Zde je Oceán s pěti galeonami.", ret);
    }

    @Test
    public void verify_six_ship_of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.GALLEON);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.GALLEON);
        when(unit5.getType()).thenReturn(UnitType.GALLEON);
        when(unit6.getType()).thenReturn(UnitType.GALLEON);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4, unit5, unit6), Optional.empty());

        assertEquals("Zde je Oceán s 6. galeonami.", ret);
    }

    @Test
    public void verify_colonist_two_galleons_one_frigate__of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit1.getType()).thenReturn(UnitType.COLONIST);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit1, unit2, unit3, unit4), Optional.empty());

        assertEquals("Zde je Oceán s dvěmi galeonami, svobodným kolonistou a frigatou.", ret);
    }

    @Test
    public void verify_two_galleons_one_frigate__of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit2, unit3, unit4), Optional.empty());

        assertEquals("Zde je Oceán s dvěmi galeonami a frigatou.", ret);
    }

    @Test
    public void verify_coloy() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(colony.getName()).thenReturn("Kuchařovice");

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                new ArrayList<>(), Optional.of(colony));

        assertEquals("Zde je Oceán s kolonií Kuchařovice.", ret);
    }

    @Test
    public void verify_coloy_two_galleons_one_frigate__of_same_type() throws Exception {
        when(gamePreferences.isDevelopment()).thenReturn(false);
        when(player.isVisible(where)).thenReturn(true);
        when(colony.getName()).thenReturn("Kuchařovice");
        when(unit2.getType()).thenReturn(UnitType.GALLEON);
        when(unit3.getType()).thenReturn(UnitType.GALLEON);
        when(unit4.getType()).thenReturn(UnitType.FRIGATE);

        final String ret = service.getStatusMessage(where, player, TerrainType.OCEAN,
                Lists.newArrayList(unit2, unit3, unit4), Optional.of(colony));

        assertEquals("Zde je Oceán s kolonií Kuchařovice, dvěmi galeonami a frigatou.", ret);
    }
    
    @BeforeEach
    private void startup() {
        service = new StatusBarMessageService(i18n, gamePreferences, localizationHelper);
    }

    @AfterEach
    private void tearDown() {
        service = null;
    }

}
