package org.microcol.gui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.microcol.gui.MicroColException;
import org.microcol.model.GoodsType;
import org.microcol.model.Goods;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import javafx.scene.input.Clipboard;

/**
 * Classes working with clipboard data should extends this abstract class.
 */
public class ClipboardParser implements ClipboardConst {

    private final String originalString;

    private final Map<String, String> map;

    public static ClipboardParser make(final Clipboard db) {
        return new ClipboardParser(db.getString());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("map", map).toString();
    }

    protected ClipboardParser(final @Nullable String originalString) {
        if (originalString == null) {
            this.originalString = "";
            this.map = Maps.newHashMap();
        } else {
            this.originalString = Preconditions.checkNotNull(originalString);
            this.map = new HashMap<>(Splitter.on(RECORD_SEPARATOR).trimResults().omitEmptyStrings()
                    .withKeyValueSeparator(Splitter.on(SEPARATOR).limit(2).trimResults())
                    .split(originalString));
        }
    }

    public Optional<From> getFrom() {
        final String fromStr = map.get(KEY_FROM);
        if (fromStr == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(From.valueOf(fromStr));
        }
    }

    public String get(final String key) {
        return map.get(key);
    }

    public void removeKey(final String key) {
        map.remove(key);
    }

    public Optional<Goods> getGoods() {
        if (get(KEY_GOODS) == null) {
            return Optional.empty();
        } else {
            final GoodsType goodsType = GoodsType.valueOf(get(KEY_GOODS));
            final int amount = getInt(KEY_GOODS_AMOUNT);
            return Optional.of(new Goods(goodsType, amount));
        }
    }

    public Optional<Unit> getUnit(final Model model) {
        return getUnit(model, KEY_UNIT_ID);
    }

    protected Optional<Unit> getUnit(final Model model, final String unitIdKey) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(unitIdKey);
        if (get(unitIdKey) == null) {
            return Optional.empty();
        } else {
            final Unit unit = model.getUnitById(getInt(unitIdKey));
            return Optional.of(unit);
        }
    }

    public int getInt(final String key) {
        final String val = map.get(key);
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException e) {
            throw new MicroColException(String.format("Can't convert '%s' to int", val), e);
        }
    }

    /**
     * @return the originalString
     */
    public String getOriginalString() {
        return originalString;
    }

}
