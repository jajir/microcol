package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.microcol.gui.util.Clipboard.KEY_CONSTRUCTION_TYPE;
import static org.microcol.gui.util.Clipboard.KEY_FROM;
import static org.microcol.gui.util.Clipboard.RECORD_SEPARATOR;
import static org.microcol.gui.util.Clipboard.SEPARATOR;

import org.junit.Test;
import org.microcol.gui.util.ClipboardParser;
import org.microcol.gui.util.From;
import org.microcol.model.ConstructionType;

import javafx.scene.input.Dragboard;
import mockit.Expectations;
import mockit.Mocked;

public class ClipboardParserTest {

    @Mocked
    private Dragboard db;

    @Test
    public void test_just_from() throws Exception {
        recordDb(KEY_FROM + SEPARATOR + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name());
        final ClipboardParser parser = ClipboardParser.make(db);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
    }

    @Test
    public void test_just_from_with_trailing_separator() throws Exception {
        recordDb(KEY_FROM + SEPARATOR + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name() + RECORD_SEPARATOR);
        final ClipboardParser parser = ClipboardParser.make(db);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
    }

    @Test
    public void test_just_multiple_key_value_pairs() throws Exception {
        recordDb(KEY_FROM + SEPARATOR + From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION.name() + RECORD_SEPARATOR
                + KEY_CONSTRUCTION_TYPE + SEPARATOR + ConstructionType.ARMORY.name()
                + RECORD_SEPARATOR);
        final ClipboardParser parser = ClipboardParser.make(db);

        assertEquals(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION, parser.getFrom().get());
        assertEquals(ConstructionType.ARMORY.name(), parser.get(KEY_CONSTRUCTION_TYPE));
    }

    private void recordDb(final String code) {
        new Expectations() {
            {
                db.getString();
                result = code;
            }
        };
    }

}
