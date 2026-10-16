package org.microcol.model;

import java.util.List;

/** An immutable quote for completing one particular queue head. */
public final class ConstructionRushQuote {

    private final ColonyBuildingQueue queue;
    private final ColonyBuildingItemProgress<?> head;
    private final List<ColonyBuildingItemProgress<?>> order;
    private final Player owner;
    private final int revision;
    private final int hammersStock;
    private final int toolsStock;
    private final int gold;
    private final EuropeTradeQuote toolsQuote;
    private final int hammersUsed;
    private final int toolsUsed;
    private final int missingHammers;
    private final int missingTools;
    private final int price;

    ConstructionRushQuote(final ColonyBuildingQueue queue,
            final ColonyBuildingItemProgress<?> head,
            final List<ColonyBuildingItemProgress<?>> order, final Player owner,
            final int revision, final int hammersStock, final int toolsStock,
            final int gold, final EuropeTradeQuote toolsQuote,
            final int hammersUsed, final int toolsUsed,
            final int missingHammers, final int missingTools, final int price) {
        this.queue = queue;
        this.head = head;
        this.order = List.copyOf(order);
        this.owner = owner;
        this.revision = revision;
        this.hammersStock = hammersStock;
        this.toolsStock = toolsStock;
        this.gold = gold;
        this.toolsQuote = toolsQuote;
        this.hammersUsed = hammersUsed;
        this.toolsUsed = toolsUsed;
        this.missingHammers = missingHammers;
        this.missingTools = missingTools;
        this.price = price;
    }

    public ColonyBuildingItem getItem() {
        return head.getItem();
    }

    public int getHammersUsed() {
        return hammersUsed;
    }

    public int getToolsUsed() {
        return toolsUsed;
    }

    public int getMissingHammers() {
        return missingHammers;
    }

    public int getMissingTools() {
        return missingTools;
    }

    public int getPrice() {
        return price;
    }

    public int getGoldAfter() {
        return gold - price;
    }

    boolean matches(final ConstructionRushQuote current) {
        return current != null && queue == current.queue && head == current.head
                && owner == current.owner && revision == current.revision
                && order.equals(current.order)
                && hammersStock == current.hammersStock
                && toolsStock == current.toolsStock && gold == current.gold
                && toolsQuote.equals(current.toolsQuote)
                && hammersUsed == current.hammersUsed
                && toolsUsed == current.toolsUsed
                && missingHammers == current.missingHammers
                && missingTools == current.missingTools
                && price == current.price;
    }

    boolean belongsTo(final ColonyBuildingQueue candidate) {
        return queue == candidate;
    }
}
