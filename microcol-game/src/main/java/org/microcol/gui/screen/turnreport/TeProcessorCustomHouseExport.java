package org.microcol.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.screen.colony.CustomHouseText;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventCustomHouseExport;

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
            return new TeItemSimple(getI18n().get(CustomHouseText.exported,
                    sale.getColonyName(), sale.getGoods().getAmount(),
                    getI18n().get(GoodsTypeName
                            .getNameForGoodsType(sale.getGoods().getType())),
                    sale.getIncome()));
        }
        return null;
    }
}
