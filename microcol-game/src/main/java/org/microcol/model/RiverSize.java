package org.microcol.model;

import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Size of the river crossing a land square. Fishing replaces ordinary
 * production on a river square.
 */
public enum RiverSize {

    /** No river; the saved code is a blank entry. */
    NONE("-", 0),

    /** Small river: three food before worker modifiers. */
    SMALL("s", 3),

    /** Large river: four food before worker modifiers. */
    LARGE("l", 4);

    private final String code;

    private final int foodProduction;

    RiverSize(final String code, final int foodProduction) {
        this.code = code;
        this.foodProduction = foodProduction;
    }

    public String getCode() {
        return code;
    }

    /**
     * @return fishing output where the bare terrain supports food
     */
    public int getFoodProduction() {
        return foodProduction;
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
