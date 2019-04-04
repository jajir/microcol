package org.microcol.model;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

public class PlaceBuilderPoTest {

    private PlaceBuilderPo placeBuilder;

    private final UnitPo unitPo = mock(UnitPo.class);

    private final ModelPo modelPo = mock(ModelPo.class);

    private final Model model = mock(Model.class);

    private final Unit unit = mock(Unit.class);

    @Test
    public void test_build_verify_thatAllBuilderAreCalled() throws Exception {
        when(unitPo.getPlaceMap()).thenReturn(null);
        when(unitPo.getPlaceHighSeas()).thenReturn(null);
        when(unitPo.getPlaceEuropePort()).thenReturn(null);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            placeBuilder.apply(unit);
        });

        assertTrue(exception.getMessage().contains("Unable to process chain of command input"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_build_placeMap() throws Exception {
        final PlaceMapPo placeMapPo = new PlaceMapPo();
        placeMapPo.setLocation(Location.of(3, 4));
        when(unitPo.getPlaceMap()).thenReturn(placeMapPo);
        final Place ret = placeBuilder.apply(unit);

        assertNotNull(ret);
        assertTrue(ret instanceof PlaceLocation);
        PlaceLocation placeLoc = (PlaceLocation) ret;
        assertEquals(Location.of(3, 4), placeLoc.getLocation());
    }

    @Test
    public void test_build_cargo() throws Exception {
        final PlaceMapPo placeMapPo = new PlaceMapPo();
        placeMapPo.setLocation(Location.of(3, 4));
        when(unitPo.getPlaceMap()).thenReturn(placeMapPo);
        final Place ret = placeBuilder.apply(unit);

        assertNotNull(ret);
        assertTrue(ret instanceof PlaceLocation);
        PlaceLocation placeLoc = (PlaceLocation) ret;
        assertEquals(Location.of(3, 4), placeLoc.getLocation());
    }

    @BeforeEach
    public void setup() {
        placeBuilder = new PlaceBuilderPo(unitPo, modelPo, model);
    }

    @AfterEach
    public void tearDown() {
        placeBuilder = null;
    }
}
