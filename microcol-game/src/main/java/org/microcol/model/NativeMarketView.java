package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Immutable answer of the model about one visitor and one native settlement
 * (R-0159): whether the tribe knows the visitor's nation, why the visitor is or
 * is not admitted to the market, and the market itself only when admitted. A
 * refused visitor receives no stock, wanted goods or treasury.
 */
public final class NativeMarketView {

    /** The first failed admission gate, or admitted. */
    public enum Admission {

        /** The visitor may inspect and trade at this settlement. */
        ADMITTED,

        /** Only a loaded wagon or loaded ship can trade. */
        NOT_A_TRADER,

        /** The tribe does not know the visitor's nation by land yet. */
        NO_CONTACT,

        /** The carrier holds passengers only, no goods. */
        NO_GOODS,

        /** A ship needs water beside the settlement and under itself. */
        SHIP_NOT_COASTAL,

        /** Angry or hostile natives refuse trade. */
        RELATION_TENSE,

        /** Uneasy natives refuse ship trade. */
        SHIP_RELATION_UNEASY

    }

    /** One offered good and its current stock. */
    public static final class Offer {

        private final GoodsType goods;

        private final int stock;

        Offer(final GoodsType goods, final int stock) {
            this.goods = Preconditions.checkNotNull(goods);
            this.stock = stock;
        }

        public GoodsType getGoods() {
            return goods;
        }

        public int getStock() {
            return stock;
        }

    }

    /** One tradable good the carrier holds, and how many pieces. */
    public static final class Carried {

        private final GoodsType goods;

        private final int amount;

        Carried(final GoodsType goods, final int amount) {
            this.goods = Preconditions.checkNotNull(goods);
            this.amount = amount;
        }

        public GoodsType getGoods() {
            return goods;
        }

        public int getAmount() {
            return amount;
        }

    }

    /** One offered good of the second stage, with its Europe ask (R-0161). */
    public static final class PurchaseOffer {

        private final GoodsType goods;

        private final int stock;

        private final int askPrice;

        PurchaseOffer(final GoodsType goods, final int stock,
                final int askPrice) {
            this.goods = Preconditions.checkNotNull(goods);
            this.stock = stock;
            this.askPrice = askPrice;
        }

        public GoodsType getGoods() {
            return goods;
        }

        public int getStock() {
            return stock;
        }

        /**
         * @return Europe ask of one piece, the price the natives charge
         */
        public int getAskPrice() {
            return askPrice;
        }

    }

    /**
     * The second stage of a carrier (R-0161), pending after a confirmed sale or
     * gift in the same expedition, whether or not the carrier still holds
     * goods.
     */
    public static final class Purchase {

        private final List<PurchaseOffer> offers;

        private final int unavailableSlots;

        private final NativeSaleQuote.Haggle haggle;

        private final boolean open;

        private final GoodsType selectedGoods;

        private final int selectedQuantity;

        Purchase(final List<PurchaseOffer> offers,
                final NativeSaleQuote.Haggle haggle, final boolean open,
                final GoodsType selectedGoods, final int selectedQuantity) {
            this.offers = ImmutableList.copyOf(offers);
            this.unavailableSlots = NativeMarketRules.OFFER_SLOTS
                    - this.offers.size();
            this.haggle = Preconditions.checkNotNull(haggle);
            this.open = open;
            this.selectedGoods = selectedGoods;
            this.selectedQuantity = selectedQuantity;
        }

        /**
         * @return goods of the offer the request was made for, so a reopened
         *         chooser shows the same offer; empty before the request
         */
        public Optional<GoodsType> getSelectedGoods() {
            return Optional.ofNullable(selectedGoods);
        }

        /**
         * @return pieces of that offer as they were shown; zero before the
         *         request. The live limits may be lower now, so a caller
         *         clamps.
         */
        public int getSelectedQuantity() {
            return selectedQuantity;
        }

        /**
         * @return offered goods in offer order, with stock and Europe ask
         */
        public List<PurchaseOffer> getOffers() {
            return offers;
        }

        /**
         * @return sale slots with no naturally producible good behind them
         */
        public int getUnavailableSlots() {
            return unavailableSlots;
        }

        /**
         * @return request result of this stage: open, accepted, or refused
         */
        public NativeSaleQuote.Haggle getHaggle() {
            return haggle;
        }

        /**
         * @return whether a purchase may be confirmed now; a pending stage can
         *         still be declined when this is false
         */
        public boolean isOpen() {
            return open;
        }

    }

    /** The market of an admitted visitor. */
    public static final class Market {

        private final List<Offer> offers;

        private final int unavailableSlots;

        private final List<GoodsType> wanted;

        private final int treasury;

        private final List<Carried> carried;

        private final GoodsType haggledGoods;

        private final int haggledQuantity;

        Market(final List<Offer> offers, final List<GoodsType> wanted,
                final int treasury, final List<Carried> carried,
                final GoodsType haggledGoods, final int haggledQuantity) {
            this.haggledGoods = haggledGoods;
            this.haggledQuantity = haggledQuantity;
            this.offers = ImmutableList.copyOf(offers);
            this.unavailableSlots = NativeMarketRules.OFFER_SLOTS
                    - this.offers.size();
            this.wanted = ImmutableList.copyOf(wanted);
            this.treasury = treasury;
            this.carried = ImmutableList.copyOf(carried);
        }

        /**
         * @return goods of the sale offer the haggle was made for, so a
         *         reopened chooser shows the same offer; empty before the
         *         haggle
         */
        public Optional<GoodsType> getHaggledGoods() {
            return Optional.ofNullable(haggledGoods);
        }

        /**
         * @return pieces of that offer as they were shown, or zero when none
         *         was recorded; a caller clamps it to the live limit
         */
        public int getHaggledQuantity() {
            return haggledQuantity;
        }

        /**
         * @return tradable goods the carrier holds, in goods name order; these
         *         are the goods a sale or gift can name
         */
        public List<Carried> getCarried() {
            return carried;
        }

        /**
         * @return offered goods in offer order, fewer than three when the
         *         surroundings produce fewer goods
         */
        public List<Offer> getOffers() {
            return offers;
        }

        /**
         * @return sale slots with no naturally producible good behind them
         */
        public int getUnavailableSlots() {
            return unavailableSlots;
        }

        /**
         * @return goods the settlement wants to buy, in demand order
         */
        public List<GoodsType> getWanted() {
            return wanted;
        }

        /**
         * @return gold the settlement holds now
         */
        public int getTreasury() {
            return treasury;
        }

    }

    private final boolean contacted;

    private final boolean introductionOffered;

    private final Admission admission;

    private final boolean expeditionOpen;

    private final Market market;

    private final Purchase purchase;

    NativeMarketView(final boolean contacted, final boolean introductionOffered,
            final Admission admission, final boolean expeditionOpen,
            final Market market, final Purchase purchase) {
        this.contacted = contacted;
        this.introductionOffered = introductionOffered;
        this.admission = Preconditions.checkNotNull(admission);
        this.expeditionOpen = expeditionOpen;
        this.market = market;
        this.purchase = purchase;
    }

    /**
     * @return whether a land introduction has made this tribe know the nation
     */
    public boolean isContacted() {
        return contacted;
    }

    /**
     * @return whether the visitor can introduce its nation by land now
     */
    public boolean isIntroductionOffered() {
        return introductionOffered;
    }

    /**
     * @return the first failed admission gate, or {@link Admission#ADMITTED}
     */
    public Admission getAdmission() {
        return admission;
    }

    /**
     * @return whether this carrier's trading expedition is open here
     */
    public boolean isExpeditionOpen() {
        return expeditionOpen;
    }

    /**
     * @return the market, present only when the visitor is admitted
     */
    public Optional<Market> getMarket() {
        return Optional.ofNullable(market);
    }

    /**
     * @return the second stage, present only while a sale or gift of this
     *         expedition has opened it and it is not yet bought or declined
     */
    public Optional<Purchase> getPurchase() {
        return Optional.ofNullable(purchase);
    }

}
