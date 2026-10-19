package org.microcol.model;

import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Size of the river crossing a land square. A river adds its production bonus
 * only to an output that is already positive.
 */
public enum RiverSize {

    /** No river; the saved code is a blank entry. */
    NONE("-", 0),

    /** Small river: +1 to an already positive output. */
    SMALL("s", 1),

    /** Large river: +2 to an already positive output. */
    LARGE("l", 2);

    private final String code;

    private final int productionBonus;

    RiverSize(final String code, final int productionBonus) {
        this.code = code;
        this.productionBonus = productionBonus;
    }

    public String getCode() {
        return code;
    }

    /**
     * @return flat addition to an already positive output of the square
     */
    public int getProductionBonus() {
        return productionBonus;
    }

    /**
     * @param code saved river code of one square
     * @return river size with that code
     * @throws IllegalArgumentException when the code is unknown
     */
    public static RiverSize valueOfCode(final String code) {
        Preconditions.checkNotNull(code, "River code can't be null.");
        return Arrays.stream(values()).filter(size -> size.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("There is no river size for code '%s'", code)));
    }
}
