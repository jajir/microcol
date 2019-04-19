package org.microcol.page;

/**
 * Object allows to test one unit shown in right panel.
 */
public class ScreenMarketSell extends AbastractScreenMarket {

    public static ScreenMarketSell of(final TestContext context) {
	return new ScreenMarketSell(context);
    }

    private ScreenMarketSell(final TestContext context) {
	super(context);
    }

    public void clickOnSell() {
	clickOnButtonWithId("buttonYes");
    }

}
