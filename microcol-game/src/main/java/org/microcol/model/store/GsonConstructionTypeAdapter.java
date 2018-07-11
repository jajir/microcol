package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.ConstructionType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class GsonConstructionTypeAdapter extends TypeAdapter<ConstructionType> {

    @Override
    public void write(final JsonWriter out, final ConstructionType value) throws IOException {
	//TODO make it required, remove null check throw exception
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.name());
    }

    @Override
    public ConstructionType read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return ConstructionType.valueOf(reader.nextString());
    }

}
