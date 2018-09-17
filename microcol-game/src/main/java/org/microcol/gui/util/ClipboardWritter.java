package org.microcol.gui.util;

import org.microcol.model.CargoSlot;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsAmount;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;

/**
 * Helps to store informations about dragged object and set dragging icon.
 */
public final class ClipboardWritter implements Clipboard {

    private final ClipboardContent content;

    private final Dragboard db;

    private final StringBuilder buff;

    public static ClipboardWritter make(final Dragboard db) {
        return new ClipboardWritter(db);
    }

    private ClipboardWritter(final Dragboard db) {
        this.db = Preconditions.checkNotNull(db);
        content = new ClipboardContent();
        buff = new StringBuilder(50);
    }

    public ClipboardWritter addImage(final Image image) {
        content.putImage(image);
        return this;
    }

    public ClipboardWritter addUnit(final Unit unit) {
        addKeyValue(KEY_UNIT_ID, unit.getId());
        return this;
    }

    public ClipboardWritter addGoodAmount(final GoodsAmount goodsAmount) {
        Preconditions.checkState(goodsAmount != null, "Goods amount is null.");
        addKeyValue(KEY_GOODS, goodsAmount.getGoodType().name());
        addKeyValue(KEY_GOODS_AMOUNT, goodsAmount.getAmount());
        return this;
    }

    public ClipboardWritter addTransferFromUnit(final Unit unit, final CargoSlot cargoSlot) {
        addFrom(From.VALUE_FROM_UNIT);
        addKeyValue(KEY_SOURCE_UNIT_ID, unit.getId());
        addKeyValue(KEY_CARGO_SLOT_INDEX, cargoSlot.getIndex());
        return this;
    }

    public ClipboardWritter addTransferFromEuropePortPier() {
        addFrom(From.VALUE_FROM_EUROPE_PORT_PIER);
        return this;
    }

    public ClipboardWritter addTransferFromEuropeShop() {
        addFrom(From.VALUE_FROM_EUROPE_SHOP);
        return this;
    }

    public ClipboardWritter addTransferFromOutsideColony() {
        addFrom(From.VALUE_FROM_OUTSIDE_COLONY);
        return this;
    }

    public ClipboardWritter addTransferFromColonyWarehouse() {
        addFrom(From.VALUE_FROM_COLONY_WAREHOUSE);
        return this;
    }

    public ClipboardWritter addTransferFromColonyField(final Location fieldDirection) {
        addFrom(From.VALUE_FROM_COLONY_FIELD);
        addKeyValue(KEY_X, fieldDirection.getX());
        addKeyValue(KEY_Y, fieldDirection.getY());
        return this;
    }

    public ClipboardWritter addTransferFromConstructionSlot() {
        addFrom(From.VALUE_FROM_CONSTRUCTION_SLOT);
        return this;
    }

    public ClipboardWritter fromBuildingQueue(final UnitType unitType) {
        addFrom(From.VALUE_FROM_BUILDING_QUEUE);
        addKeyValue(KEY_UNIT_TYPE, unitType.name());
        return this;
    }

    public ClipboardWritter fromBuildingQueue(final ConstructionType constructionType) {
        addFrom(From.VALUE_FROM_BUILDING_QUEUE);
        addKeyValue(KEY_CONSTRUCTION_TYPE, constructionType.name());
        return this;
    }

    public ClipboardWritter fromBuildingQueueUnit(final UnitType unitType) {
        addFrom(From.VALUE_FROM_BUILDING_QUEUE_UNIT);
        addKeyValue(KEY_UNIT_TYPE, unitType.name());
        return this;
    }

    public ClipboardWritter fromBuildingQueueConstruction(final ConstructionType constructionType) {
        addFrom(From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION);
        addKeyValue(KEY_CONSTRUCTION_TYPE, constructionType.name());
        return this;
    }

    public ClipboardWritter addFrom(final From from) {
        addKeyValue(KEY_FROM, from.name());
        return this;
    }

    public ClipboardWritter addKeyValue(final String key, final Object value) {
        Preconditions.checkNotNull(key, "Key can't be null.");
        Preconditions.checkNotNull(value, "Value can't be null.");
        buff.append(key);
        buff.append(SEPARATOR);
        buff.append(String.valueOf(value));
        buff.append(RECORD_SEPARATOR);
        return this;
    }

    public void build() {
        content.putString(buff.toString());
        db.setContent(content);
    }

}
