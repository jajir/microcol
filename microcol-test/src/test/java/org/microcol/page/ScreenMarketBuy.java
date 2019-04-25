package org.microcol.page;

/**
 * Object allows to test one unit shown in right panel.
 */
public class ScreenMarketBuy extends AbstractScreenMarket {

    public static ScreenMarketBuy of(final TestContext context) {
	return new ScreenMarketBuy(context);
    }

    private ScreenMarketBuy(final TestContext context) {
	super(context);
    }

    public void clickOnBuy() {
	clickOnButtonWithId("buttonYes");
    }

}
