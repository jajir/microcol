package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write good from json.
 */
public final class GsonGoodsAdapter extends TypeAdapter<Goods> {

    @Override
    public void write(final JsonWriter out, final Goods value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("type").value(value.getType().name());
        out.name("amount").value(value.getAmount());
        out.endObject();
    }

    @Override
    public Goods read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        GoodsType type = null;
        Integer amount = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
            case "type":
                type = GoodsType.valueOf(reader.nextString());
                break;
            case "amount":
                amount = reader.nextInt();
                break;
            default:
                throw new IllegalArgumentException("Invalid case");
            }
        }
        reader.endObject();

        return Goods.of(type, amount);
    }

}
