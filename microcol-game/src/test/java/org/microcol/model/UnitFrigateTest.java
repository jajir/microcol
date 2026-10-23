package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFrigate;

import com.google.common.base.Preconditions;

public class UnitFrigateTest {

    private UnitFrigate unit;

    @SuppressWarnings("unchecked")
    private final Function<Unit, Cargo> cargoProvider = mock(Function.class);

    private final Model model = mock(Model.class);

    private final WorldMap map = mock(WorldMap.class);

    private final Integer id = 4;

    @SuppressWarnings("unchecked")
    private final Function<Unit, Place> placeBuilder = mock(Function.class);

    private final Player owner = mock(Player.class);

    private final int availableMoves = 3;

    private final UnitAction unitAction = new UnitActionNoAction();

    private final PlaceLocation placeMap = mock(PlaceLocation.class);

    private final Cargo cargo = mock(Cargo.class);

    @Test
    public void test_verifyThatUnitCouldBePlacedIntoHighseas()
            throws Exception {
        when(placeMap.getLocation()).thenReturn(Location.of(30, 30));
        when(model.getMap()).thenReturn(map);
        when(map.getMaxLocationX()).thenReturn(40);
        when(map.isValid(Location.of(30, 30))).thenReturn(true);
        when(map.getTerrainTypeAt(Location.of(30, 30)))
                .thenReturn(TerrainType.HIGH_SEA);
        when(model.getHighSea()).thenReturn(new HighSea(model));
        when(owner.getAllUnits()).thenReturn(List.of(unit));
        when(owner.canTradeInEurope()).thenReturn(true);
        unit.placeToHighSeas(true);

        assertTrue(unit.isAtHighSea());
        assertEquals(availableMoves - 1, unit.getActionPoints());
        assertEquals(Location.of(30, 30),
                unit.getDepartureLocation().orElseThrow());
    }

    @Test
    public void test_closedHomeEuropeRefusesSailingToEuropeWithoutChange()
            throws Exception {
        when(placeMap.getLocation()).thenReturn(Location.of(30, 30));
        when(model.getMap()).thenReturn(map);
        when(map.getMaxLocationX()).thenReturn(40);
        when(map.isValid(Location.of(30, 30))).thenReturn(true);
        when(map.getTerrainTypeAt(Location.of(30, 30)))
                .thenReturn(TerrainType.HIGH_SEA);
        when(model.getHighSea()).thenReturn(new HighSea(model));
        when(owner.getAllUnits()).thenReturn(List.of(unit));
        when(owner.canTradeInEurope()).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> unit.placeToHighSeas(true));

        assertFalse(unit.isAtHighSea());
        assertEquals(availableMoves, unit.getActionPoints());
    }

    @Test
    public void test_placeToEuropePortPier() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            unit.placeToEuropePortPier();
        });
    }

    @Test
    public void test_visibleArea_visibility_1() throws Exception {
        when(placeMap.getLocation()).thenReturn(Location.of(30, 30));
        when(model.getMap()).thenReturn(map);
        when(map.isValid(any(Location.class))).thenReturn(true);

        final List<Location> visible = unit.getVisibleLocations();

        Preconditions.checkNotNull(visible);
        assertEquals(2, unit.getSightRadius());
        assertEquals(25, visible.size());
        assertTrue(visible.contains(Location.of(28, 28)));
        assertTrue(visible.contains(Location.of(32, 32)));
        assertFalse(visible.contains(Location.of(33, 30)));
    }

    @BeforeEach
    public void setup() {
        /*
         * Following expectations will be used for unit constructor
         */
        when(cargoProvider.apply((Unit) any())).thenReturn(cargo);
        when(placeBuilder.apply((Unit) any())).thenReturn(placeMap);

        unit = new UnitFrigate(cargoProvider, model, id, placeBuilder, owner,
                MovementPoints.ofPoints(availableMoves), unitAction);
    }

    @AfterEach
    public void tearDown() {
        unit = null;
    }

}
