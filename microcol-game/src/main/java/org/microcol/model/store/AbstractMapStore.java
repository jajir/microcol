package org.microcol.model.store;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.microcol.model.Location;

public abstract class AbstractMapStore {

    protected void iterate(final String[] rows, final BiConsumer<Location, String> consumer) {
        for (int y = 1; y < rows.length; y++) {
            final String row = rows[y].substring("row-0001:".length());
            final String[] parts = row.split(",");
            for (int x = 0; x < parts.length; x++) {
                final String charCode = parts[x];
                final Location loc = Location.of(x + 1, y);
                consumer.accept(loc, charCode);
            }
        }
    }

    protected String[] generateString(final Function<Location, String> charProducer, final int maxX,
            final int maxY) {
        final String[] out = new String[maxY + 1];
        final StringBuilder buff1 = new StringBuilder();
        buff1.append("column  :");
        for (int x = 0; x < maxX; x++) {
            if (x > 0) {
                buff1.append(",");
            }
            buff1.append(limit(x + 1, 1));
        }
        out[0] = buff1.toString();

        for (int y = 0; y < maxY; y++) {
            final StringBuilder buff = new StringBuilder();
            buff.append("row-");
            buff.append(limit(y + 1, 4));
            buff.append(":");
            for (int x = 0; x < maxX; x++) {
                if (x > 0) {
                    buff.append(",");
                }
                final Location loc = Location.of(x + 1, y + 1);
                buff.append(charProducer.apply(loc));
            }
            out[y + 1] = buff.toString();
        }
        return out;
    }

    private String limit(final int count, final int lengthLimit) {
        String out = String.valueOf(count);
        // cut from beginning of number
        if (out.length() > lengthLimit) {
            out = out.substring(out.length() - lengthLimit, out.length());
        }
        // add zeros to required length
        while (out.length() < lengthLimit) {
            out = "0" + out;
        }
        return out;
    }

}
