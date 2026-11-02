package org.akropia.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class GoodsType {

    public final static GoodsType GRAIN = GoodsTypeBuilder.make()
            .setName("GRAIN").setCanBeBought(true).build();

    public final static GoodsType OLIVES = GoodsTypeBuilder.make()
            .setName("OLIVES").setCanBeBought(true).build();

    public final static GoodsType GRAPES = GoodsTypeBuilder.make()
            .setName("GRAPES").setCanBeBought(true).build();

    public final static GoodsType WOOL = GoodsTypeBuilder.make().setName("WOOL")
            .setCanBeBought(true).build();

    public final static GoodsType CLAY = GoodsTypeBuilder.make().setName("CLAY")
            .setCanBeBought(true).build();

    public final static GoodsType LUMBER = GoodsTypeBuilder.make()
            .setName("LUMBER").setCanBeBought(true).build();

    public final static GoodsType ORE = GoodsTypeBuilder.make().setName("ORE")
            .setCanBeBought(true).build();

    public final static GoodsType SILVER = GoodsTypeBuilder.make()
            .setName("SILVER").setCanBeBought(true).build();

    public final static GoodsType HORSE = GoodsTypeBuilder.make()
            .setName("HORSE").setCanBeBought(true).build();

    public final static GoodsType OLIVE_OIL = GoodsTypeBuilder.make()
            .setName("OLIVE_OIL").setCanBeBought(true).build();

    public final static GoodsType WINE = GoodsTypeBuilder.make().setName("WINE")
            .setCanBeBought(true).build();

    public final static GoodsType CLOTH = GoodsTypeBuilder.make()
            .setName("CLOTH").setCanBeBought(true).build();

    public final static GoodsType AMPHORAE = GoodsTypeBuilder.make()
            .setName("AMPHORAE").setCanBeBought(true).build();

    public final static GoodsType GOODS = GoodsTypeBuilder.make()
            .setName("GOODS").setCanBeBought(true).build();

    public final static GoodsType TOOLS = GoodsTypeBuilder.make()
            .setName("TOOLS").setCanBeBought(true).build();

    public final static GoodsType WEAPONS = GoodsTypeBuilder.make()
            .setName("WEAPONS").setCanBeBought(true).build();

    public final static GoodsType HAMMERS = GoodsTypeBuilder.make()
            .setName("HAMMERS").setCanBeBought(false).build();

    public final static GoodsType TORCHES = GoodsTypeBuilder.make()
            .setName("TORCHES").setCanBeBought(false).build();

    public final static GoodsType OWLS = GoodsTypeBuilder.make().setName("OWLS")
            .setCanBeBought(false).build();

    public final static List<GoodsType> GOOD_TYPES = ImmutableList
            .<GoodsType>of(GRAIN, OLIVES, GRAPES, WOOL, CLAY, LUMBER, ORE,
                    SILVER, HORSE, OLIVE_OIL, WINE, CLOTH, AMPHORAE, GOODS,
                    TOOLS, WEAPONS, HAMMERS, TORCHES, OWLS);

    public final static List<GoodsType> BUYABLE_GOOD_TYPES = GOOD_TYPES.stream()
            .filter(goodsType -> goodsType.isCanBeBought())
            .collect(Collectors.toList());

    private static class GoodsTypeBuilder {

        private String name;

        private boolean canBeBought;

        public static GoodsTypeBuilder make() {
            return new GoodsTypeBuilder();
        }

        private GoodsType build() {
            return new GoodsType(name, canBeBought);
        }

        private GoodsTypeBuilder setName(String name) {
            this.name = name;
            return this;
        }

        private GoodsTypeBuilder setCanBeBought(boolean canBeBought) {
            this.canBeBought = canBeBought;
            return this;
        }

    }

    private final String name;

    private final boolean canBeBought;

    private GoodsType(final String name, final boolean canBeBought) {
        this.name = Preconditions.checkNotNull(name);
        this.canBeBought = canBeBought;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(GoodsType.class).add("name", name())
                .toString();
    }

    public static GoodsType valueOf(final String strName) {
        final Optional<GoodsType> oGoodsType = GOOD_TYPES.stream()
                .filter(goodsType -> goodsType.name().equals(strName))
                .findFirst();
        if (oGoodsType.isPresent()) {
            return oGoodsType.get();
        } else {
            throw new IllegalArgumentException(
                    "There is no such type '" + strName + "'");
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof GoodsType) {
            final GoodsType other = (GoodsType) obj;
            return name.equals(other.name);
        }
        return false;
    }

    public boolean isCanBeBought() {
        return canBeBought;
    }

    public String name() {
        return name;
    }

}
