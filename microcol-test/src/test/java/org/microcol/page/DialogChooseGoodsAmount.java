package org.microcol.page;

/**
 * Object allows to test one unit shown in right panel.
 */
public class DialogChooseGoodsAmount extends AbastractScreenMarket {

    public static DialogChooseGoodsAmount of(final TestContext context) {
	return new DialogChooseGoodsAmount(context);
    }

    private DialogChooseGoodsAmount(final TestContext context) {
	super(context);
    }

    public void clickOnOk() {
	clickOnButtonWithId("buttonOk");
    }

}
