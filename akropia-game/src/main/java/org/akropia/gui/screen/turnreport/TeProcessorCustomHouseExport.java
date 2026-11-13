package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.screen.colony.CustomHouseText;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventCustomHouseExport;

/** Display the recorded sale without recomputing prices or moving stock. */
public final class TeProcessorCustomHouseExport extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorCustomHouseExport(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventCustomHouseExport) {
            TurnEventCustomHouseExport sale = (TurnEventCustomHouseExport) event;
            return new TeItemColony(getI18n().get(CustomHouseText.exported,
                    sale.getColonyName(), sale.getGoods().getAmount(),
                    getI18n().get(GoodsTypeName
                            .getNameForGoodsType(sale.getGoods().getType())),
                    sale.getIncome()), sale.getColonyId());
        }
        return null;
    }
}
