package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.GoodType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GsonGoodTypeAdapter extends TypeAdapter<GoodType> {

    @Override
    public void write(final JsonWriter out, final GoodType value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.name());
    }

    @Override
    public GoodType read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return GoodType.valueOf(reader.nextString());
    }

}
