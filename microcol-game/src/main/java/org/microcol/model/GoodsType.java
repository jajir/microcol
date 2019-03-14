package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class GoodsType {

    public final static GoodsType CORN = GoodsTypeBuilder.make().setName("CORN").setCanBeBought(true)
            .build();

    public final static GoodsType SUGAR = GoodsTypeBuilder.make().setName("SUGAR")
            .setCanBeBought(true).build();

    public final static GoodsType TOBACCO = GoodsTypeBuilder.make().setName("TOBACCO")
            .setCanBeBought(true).build();

    public final static GoodsType COTTON = GoodsTypeBuilder.make().setName("COTTON")
            .setCanBeBought(true).build();

    public final static GoodsType FUR = GoodsTypeBuilder.make().setName("FUR").setCanBeBought(true)
            .build();

    public final static GoodsType LUMBER = GoodsTypeBuilder.make().setName("LUMBER")
            .setCanBeBought(true).build();

    public final static GoodsType ORE = GoodsTypeBuilder.make().setName("ORE").setCanBeBought(true)
            .build();

    public final static GoodsType SILVER = GoodsTypeBuilder.make().setName("SILVER")
            .setCanBeBought(true).build();

    public final static GoodsType HORSE = GoodsTypeBuilder.make().setName("HORSE")
            .setCanBeBought(true).build();

    public final static GoodsType RUM = GoodsTypeBuilder.make().setName("RUM").setCanBeBought(true)
            .build();

    public final static GoodsType CIGARS = GoodsTypeBuilder.make().setName("CIGARS")
            .setCanBeBought(true).build();

    public final static GoodsType SILK = GoodsTypeBuilder.make().setName("SILK").setCanBeBought(true)
            .build();

    public final static GoodsType COAT = GoodsTypeBuilder.make().setName("COAT").setCanBeBought(true)
            .build();

    public final static GoodsType GOODS = GoodsTypeBuilder.make().setName("GOODS")
            .setCanBeBought(true).build();

    public final static GoodsType TOOLS = GoodsTypeBuilder.make().setName("TOOLS")
            .setCanBeBought(true).build();

    public final static GoodsType MUSKET = GoodsTypeBuilder.make().setName("MUSKET")
            .setCanBeBought(true).build();

    public final static GoodsType HAMMERS = GoodsTypeBuilder.make().setName("HAMMERS")
            .setCanBeBought(false).build();

    public final static GoodsType CROSS = GoodsTypeBuilder.make().setName("CROSS")
            .setCanBeBought(false).build();

    public final static GoodsType BELL = GoodsTypeBuilder.make().setName("BELL").setCanBeBought(false)
            .build();

    public final static List<GoodsType> GOOD_TYPES = ImmutableList.<GoodsType>of(CORN, SUGAR, TOBACCO,
            COTTON, FUR, LUMBER, ORE, SILVER, HORSE, RUM, CIGARS, SILK, COAT, GOODS, TOOLS, MUSKET,
            HAMMERS, CROSS, BELL);

    public final static List<GoodsType> BUYABLE_GOOD_TYPES = GOOD_TYPES.stream()
            .filter(goodsType -> goodsType.isCanBeBought()).collect(Collectors.toList());

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
        return MoreObjects.toStringHelper(GoodsType.class).add("name", name()).toString();
    }

    public static GoodsType valueOf(final String strName) {
        final Optional<GoodsType> oGoodsType = GOOD_TYPES.stream()
                .filter(goodsType -> goodsType.name().equals(strName)).findFirst();
        if (oGoodsType.isPresent()) {
            return oGoodsType.get();
        } else {
            throw new IllegalArgumentException("There is no such type '" + strName + "'");
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
