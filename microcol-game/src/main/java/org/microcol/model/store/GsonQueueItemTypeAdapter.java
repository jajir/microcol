package org.microcol.model.store;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write building queue item type from json.
 */
public final class GsonQueueItemTypeAdapter extends TypeAdapter<QueueItemType> {

    @Override
    public void write(final JsonWriter out, final QueueItemType value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.name());
    }

    @Override
    public QueueItemType read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return QueueItemType.valueOf(reader.nextString());
    }

}
