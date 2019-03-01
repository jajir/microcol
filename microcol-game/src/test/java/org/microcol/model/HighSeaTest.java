package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

public class HighSeaTest {

    private HighSea highSea;

    private final Model model = mock(Model.class);

    private final Unit unit1 = mock(Unit.class);
    private final PlaceHighSea placeHighSea1 = mock(PlaceHighSea.class);
    private final Unit unit2 = mock(Unit.class);
    private final PlaceHighSea placeHighSea2 = mock(PlaceHighSea.class);

    @Test
    public void test_getHighSeasAll() throws Exception {
        when(model.getAllUnits()).thenReturn(Lists.newArrayList(unit1));
        when(unit1.getPlace()).thenReturn(placeHighSea1);

        List<PlaceHighSea> ret = highSea.getHighSeasAll();

        assertNotNull(ret);
        assertEquals(1, ret.size());
    }

    @Test
    public void test_getHighSeasAll_two() throws Exception {
        when(model.getAllUnits()).thenReturn(Lists.newArrayList(unit1, unit2));
        when(unit1.getPlace()).thenReturn(placeHighSea1);
        when(unit2.getPlace()).thenReturn(placeHighSea2);

        List<PlaceHighSea> ret = highSea.getHighSeasAll();

        assertNotNull(ret);
        assertEquals(2, ret.size());
    }

    @BeforeEach
    public void startUp() {
        highSea = new HighSea(model);
    }

    @AfterEach
    public void tearDown() {
        highSea = null;
    }

}
