package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.ConstructionType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write construction type from json.
 */
public final class GsonConstructionTypeAdapter extends TypeAdapter<ConstructionType> {

    @Override
    public void write(final JsonWriter out, final ConstructionType value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name());
        }
    }

    @Override
    public ConstructionType read(final JsonReader reader) throws IOException {
        return ConstructionType.valueOf(reader.nextString());
    }

}
