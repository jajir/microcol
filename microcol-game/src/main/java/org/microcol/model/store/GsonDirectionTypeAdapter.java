package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.Direction;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write direction from json.
 */
public final class GsonDirectionTypeAdapter extends TypeAdapter<Direction> {

    @Override
    public void write(final JsonWriter out, final Direction value) throws IOException {
	out.value(value.name());
    }

    @Override
    public Direction read(final JsonReader reader) throws IOException {
	return Direction.valueOf(reader.nextString());
    }

}
