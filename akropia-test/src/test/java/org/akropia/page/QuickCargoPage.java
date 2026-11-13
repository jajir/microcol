package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Real quick load and unload controls of the colony port, with their
 * confirmation and refusal feedback.
 */
public final class QuickCargoPage extends AbstractScreen {

    private static final String PLAYER_NAME = "Dutch";

    public static QuickCargoPage of(final TestContext context) {
        return new QuickCargoPage(context);
    }

    private QuickCargoPage(final TestContext context) {
        super(context);
    }

    /** Use the visible quick load control for the selected carrier. */
    public void verifyTreasury(final int expectedGold) {
        assertEquals(expectedGold,
                getModel().getPlayerByName(PLAYER_NAME).getGold(),
                "Quick cargo must not change the treasury");
    }
}
