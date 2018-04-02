package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class GoodType {

    public final static GoodType CORN = GoodTypeBuilder.make().setName("CORN").setCanBeBought(true)
            .build();

    public final static GoodType SUGAR = GoodTypeBuilder.make().setName("SUGAR")
            .setCanBeBought(true).build();

    public final static GoodType TOBACCO = GoodTypeBuilder.make().setName("TABACCO")
            .setCanBeBought(true).build();

    public final static GoodType COTTON = GoodTypeBuilder.make().setName("COTTON")
            .setCanBeBought(true).build();

    public final static GoodType FUR = GoodTypeBuilder.make().setName("FUR").setCanBeBought(true)
            .build();

    public final static GoodType LUMBER = GoodTypeBuilder.make().setName("LUMBER")
            .setCanBeBought(true).build();

    public final static GoodType ORE = GoodTypeBuilder.make().setName("ORE").setCanBeBought(true)
            .build();

    public final static GoodType SILVER = GoodTypeBuilder.make().setName("SILVER")
            .setCanBeBought(true).build();

    public final static GoodType HORSE = GoodTypeBuilder.make().setName("HORSE")
            .setCanBeBought(true).build();

    public final static GoodType RUM = GoodTypeBuilder.make().setName("RUM").setCanBeBought(true)
            .build();

    public final static GoodType CIGARS = GoodTypeBuilder.make().setName("CIGARS")
            .setCanBeBought(true).build();

    public final static GoodType SILK = GoodTypeBuilder.make().setName("SILK").setCanBeBought(true)
            .build();

    public final static GoodType COAT = GoodTypeBuilder.make().setName("COAT").setCanBeBought(true)
            .build();

    public final static GoodType GOODS = GoodTypeBuilder.make().setName("GOODS")
            .setCanBeBought(true).build();

    public final static GoodType TOOLS = GoodTypeBuilder.make().setName("TOOLS")
            .setCanBeBought(true).build();

    public final static GoodType MUSKET = GoodTypeBuilder.make().setName("MUSKET")
            .setCanBeBought(true).build();

    public final static GoodType HAMMERS = GoodTypeBuilder.make().setName("HAMMERS")
            .setCanBeBought(false).build();

    public final static GoodType CROSS = GoodTypeBuilder.make().setName("CROSS")
            .setCanBeBought(false).build();

    public final static GoodType BELL = GoodTypeBuilder.make().setName("BELL").setCanBeBought(false)
            .build();

    public final static List<GoodType> GOOD_TYPES = ImmutableList.<GoodType>of(CORN, SUGAR, TOBACCO,
            COTTON, FUR, LUMBER, ORE, SILVER, HORSE, RUM, CIGARS, SILK, COAT, GOODS, TOOLS, MUSKET,
            HAMMERS, CROSS, BELL);

    public final static List<GoodType> BUYABLE_GOOD_TYPES = GOOD_TYPES.stream()
            .filter(goodType -> goodType.isCanBeBought()).collect(Collectors.toList());

    private static class GoodTypeBuilder {

        private String name;

        private boolean canBeBought;

        public static GoodTypeBuilder make() {
            return new GoodTypeBuilder();
        }

        private GoodType build() {
            return new GoodType(name, canBeBought);
        }

        private GoodTypeBuilder setName(String name) {
            this.name = name;
            return this;
        }

        private GoodTypeBuilder setCanBeBought(boolean canBeBought) {
            this.canBeBought = canBeBought;
            return this;
        }

    }

    private final String name;

    private final boolean canBeBought;

    private GoodType(final String name, final boolean canBeBought) {
        this.name = Preconditions.checkNotNull(name);
        this.canBeBought = canBeBought;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(GoodType.class).add("name", name()).toString();
    }

    public static GoodType valueOf(final String strName) {
        final Optional<GoodType> oGoodType = GOOD_TYPES.stream()
                .filter(goodType -> goodType.name().equals(strName)).findFirst();
        if (oGoodType.isPresent()) {
            return oGoodType.get();
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
        if (obj instanceof GoodType) {
            final GoodType other = (GoodType) obj;
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
