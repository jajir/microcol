package org.akropia.gui.screen.market;

import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.gui.screen.menu.ScreenMenu;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

/**
 * Screen with market in Europe. It allows to buy various goods.
 */
public abstract class AbstractScreenMarket
        implements JavaFxComponent, UpdatableLanguage {

    private final MarketView marketView;

    private final I18n i18n;

    public static final String STYLE_SHEET_MARKET = ScreenMenu.class
            .getResource("/gui/Market.css").toExternalForm();

    @Inject
    AbstractScreenMarket(final I18n i18n, final MarketView marketView) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.marketView = Preconditions.checkNotNull(marketView);
        marketView.getContent().getStylesheets().add(STYLE_SHEET_MARKET);
    }

    @Override
    public Region getContent() {
        return marketView.getContent();
    }

    public void showAndWait() {
        marketView.showAndWait();
    }

    protected I18n getI18n() {
        return i18n;
    }

    protected MarketView getMarketView() {
        return marketView;
    }

}
