package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.TerrainType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write terrain type from json.
 */
public final class GsonTerrainTypeAdapter extends TypeAdapter<TerrainType> {

    @Override
    public void write(final JsonWriter out, final TerrainType value) throws IOException {
        out.value(value.name());
    }

    @Override
    public TerrainType read(final JsonReader reader) throws IOException {
        return TerrainType.valueOf(reader.nextString());
    }

}
