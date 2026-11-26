package org.akropia.page;

/** Main menu reached after a game ends. */
public final class MainMenuPage extends AbstractMenuScreen {
    public MainMenuPage(final TestContext context) {
        super(context);
    }

    public void verifyMainMenu() {
        verifyThatMainTitleContains("Akropia");
    }
}
