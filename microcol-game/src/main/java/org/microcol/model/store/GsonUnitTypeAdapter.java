package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.UnitType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write unit type from json.
 */
public final class GsonUnitTypeAdapter extends TypeAdapter<UnitType> {

    @Override
    public void write(final JsonWriter out, final UnitType value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name());
        }
    }

    @Override
    public UnitType read(final JsonReader reader) throws IOException {
        return UnitType.valueOf(reader.nextString());
    }

}
