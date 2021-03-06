package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.GoodsType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write good type from json.
 */
public final class GsonGoodsTypeAdapter extends TypeAdapter<GoodsType> {

    @Override
    public void write(final JsonWriter out, final GoodsType value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.name());
    }

    @Override
    public GoodsType read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return GoodsType.valueOf(reader.nextString());
    }

}
