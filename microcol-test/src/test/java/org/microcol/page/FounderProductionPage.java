package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.model.*;

/** Fur field output of a worker in the founder production scenario. */
public final class FounderProductionPage extends AbstractScreen {
    public FounderProductionPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void verifyFieldFur(final int workerId, final int expectedFur) {
        final Goods production = colony().getColonyFields().stream()
                .filter(field -> field.getUnit().map(Unit::getId)
                        .orElse(-1) == workerId)
                .findFirst().orElseThrow().getProduction().orElseThrow();
        assertEquals(GoodsType.FUR, production.getType());
        assertEquals(expectedFur, production.getAmount());
    }
}
