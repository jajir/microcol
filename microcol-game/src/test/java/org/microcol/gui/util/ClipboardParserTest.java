package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.gui.util.ClipboardConst.KEY_CONSTRUCTION_TYPE;
import static org.microcol.gui.util.ClipboardConst.KEY_FROM;
import static org.microcol.gui.util.ClipboardConst.RECORD_SEPARATOR;
import static org.microcol.gui.util.ClipboardConst.SEPARATOR;

import org.junit.jupiter.api.Test;
import org.microcol.model.ConstructionType;

public class ClipboardParserTest {

    @Test
    public void test_just_from() throws Exception {
        final String clipboard = KEY_FROM + SEPARATOR
                + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name();
        final ClipboardParser parser = new ClipboardParser(clipboard);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
    }

    @Test
    public void test_just_from_with_trailing_separator() throws Exception {
        final String clipboard = KEY_FROM + SEPARATOR
                + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name() + RECORD_SEPARATOR;
        final ClipboardParser parser = new ClipboardParser(clipboard);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
    }

    @Test
    public void test_just_multiple_key_value_pairs() throws Exception {
        final String clipboard = KEY_FROM + SEPARATOR
                + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name() + RECORD_SEPARATOR
                + KEY_CONSTRUCTION_TYPE + SEPARATOR + ConstructionType.ARMORY.name()
                + RECORD_SEPARATOR;
        final ClipboardParser parser = new ClipboardParser(clipboard);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
        assertEquals(ConstructionType.ARMORY.name(), parser.get(KEY_CONSTRUCTION_TYPE));
    }

}
