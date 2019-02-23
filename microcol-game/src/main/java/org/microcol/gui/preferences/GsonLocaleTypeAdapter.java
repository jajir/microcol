package org.microcol.gui.preferences;

import java.io.IOException;
import java.util.Locale;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Allows to read and write construction type from json.
 */
public final class GsonLocaleTypeAdapter extends TypeAdapter<Locale> {

    private static final String LOCALE_LANGUAGE = "language";
    private static final String LOCALE_COUNTRY = "country";

    @Override
    public void write(final JsonWriter out, final Locale locale) throws IOException {
        if (locale == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name(LOCALE_COUNTRY).value(locale.getCountry());
            out.name(LOCALE_LANGUAGE).value(locale.getLanguage());
            out.endObject();
        }
    }

    @Override
    public Locale read(final JsonReader reader) throws IOException {
        reader.beginObject();
        String country = null, language = null;
        while (reader.hasNext()) {
            switch (reader.nextName()) {
            case LOCALE_COUNTRY:
                country = reader.nextString();
                break;
            case LOCALE_LANGUAGE:
                language = reader.nextString();
                break;
            }
        }
        reader.endObject();
        return new Locale(language, country);
    }

}
