package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Colony advisor report (R-0200) of one colonial nation. For each owned colony
 * it shows the residents, the goods that the next update leaves behind, the
 * expected new colonists or famine and the active project. The projections read
 * the same owners as the update: food and production from
 * {@link Colony#getGoodsStats()}, the project rule from
 * {@link ColonyBuildingQueue}, and customs and warehouse limits from
 * {@link Colony}. The report is a snapshot of the live state; nothing is kept
 * between calls and nothing is spent, moved or reminded.
 */
public final class ColonyAdvisorReport {

    /** Where the active project of a colony stands at the next update. */
    public enum ProjectState {
        /** No project is queued. */
        NONE,
        /** Hammers are short; the stored progress stays in the colony. */
        WAITING_HAMMERS,
        /** Hammers are enough and tools are short; the turn notice repeats. */
        WAITING_TOOLS,
        /** The project completes at the next update and takes its cost. */
        COMPLETES,
        /** The project cannot be finished now; its progress stays. */
        BLOCKED
    }

    /** The active project of one colony with its stock and missing tools. */
    public static final class Project {

        private final Optional<ColonyBuildingItem> item;
        private final ProjectState state;
        private final int hammers;
        private final int requiredHammers;
        private final int tools;
        private final int requiredTools;
        private final int missingTools;

        private Project(final Optional<ColonyBuildingItem> item,
                final ProjectState state, final int hammers,
                final int requiredHammers, final int tools,
                final int requiredTools, final int missingTools) {
            this.item = item;
            this.state = state;
            this.hammers = hammers;
            this.requiredHammers = requiredHammers;
            this.tools = tools;
            this.requiredTools = requiredTools;
            this.missingTools = missingTools;
        }

        /**
         * @param colony required colony
         * @param stats  goods of the colony for the next update
         * @return the head project, read with the rule of the update
         */
        private static Project of(final Colony colony,
                final ColonyProductionStats stats) {
            final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
            final Optional<BuildingStatus<ColonyBuildingItem>> head = queue
                    .getActuallyBuildingStat();
            if (head.isEmpty()) {
                return new Project(Optional.empty(), ProjectState.NONE, 0, 0, 0,
                        0, 0);
            }
            final BuildingStatus.Stat hammers = head.get().getHammers();
            final BuildingStatus.Stat tools = head.get().getTools();
            // The update reads the warehouse after its own production, which
            // is the same figure as the projected stock before spending.
            final int hammersAfter = stats.getStatsByType(GoodsType.HAMMERS)
                    .getInWarehouseAfter();
            final int toolsAfter = stats.getStatsByType(GoodsType.TOOLS)
                    .getInWarehouseAfter();
            final ProjectState state;
            if (queue.getCompletionUnavailableReason().isPresent()) {
                state = ProjectState.BLOCKED;
            } else {
                switch (ColonyBuildingQueue.stageAt(hammers.getRequired(),
                        tools.getRequired(), hammersAfter, toolsAfter)) {
                case WAITING_HAMMERS:
                    state = ProjectState.WAITING_HAMMERS;
                    break;
                case WAITING_TOOLS:
                    state = ProjectState.WAITING_TOOLS;
                    break;
                default:
                    state = ProjectState.COMPLETES;
                    break;
                }
            }
            return new Project(Optional.of(head.get().getItem()), state,
                    hammers.getAlreadyHave(), hammers.getRequired(),
                    tools.getAlreadyHave(), tools.getRequired(),
                    state == ProjectState.WAITING_TOOLS
                            ? tools.getRequired() - toolsAfter
                            : 0);
        }

        /**
         * @return the building or unit at the head of the queue, empty when no
         *         project is queued
         */
        public Optional<ColonyBuildingItem> getItem() {
            return item;
        }

        /** @return state of the project at the next update */
        public ProjectState getState() {
            return state;
        }

        /** @return hammers stored in the colony now */
        public int getHammers() {
            return hammers;
        }

        /** @return hammers the project needs */
        public int getRequiredHammers() {
            return requiredHammers;
        }

        /** @return tools stored in the colony now */
        public int getTools() {
            return tools;
        }

        /** @return tools the project needs */
        public int getRequiredTools() {
            return requiredTools;
        }

        /**
         * @return tools still missing for the notice of R-0086; zero unless the
         *         state is {@link ProjectState#WAITING_TOOLS}
         */
        public int getMissingTools() {
            return missingTools;
        }
    }

    /**
     * One good of a colony: the stock now and what the next update does with
     * it. The change is the difference between the next and the current stock,
     * so it includes customs exports, project costs and warehouse limits.
     */
    public static final class GoodsLine {

        private final GoodsType type;
        private final int stock;
        private final int produced;
        private final int blocked;
        private final int consumed;
        private final int spent;
        private final int exported;
        private final int lost;
        private final int next;
        private final int limit;

        private GoodsLine(final GoodsType type, final int stock,
                final int produced, final int blocked, final int consumed,
                final int spent, final int exported, final int lost,
                final int next, final int limit) {
            this.type = type;
            this.stock = stock;
            this.produced = produced;
            this.blocked = blocked;
            this.consumed = consumed;
            this.spent = spent;
            this.exported = exported;
            this.lost = lost;
            this.next = next;
            this.limit = limit;
        }

        /**
         * @param colony required colony
         * @param stats  goods of the colony for the next update
         * @param spent  amount the project takes at the next update
         * @return the line with the figures the update will leave
         */
        private static GoodsLine of(final Colony colony,
                final GoodsProductionStats stats, final int spent) {
            final GoodsType type = stats.getType();
            final int limit = colony.getWarehouse().getStorageCapacity(type)
                    .getAmount();
            // Famine leaves the food at zero; no other good goes below zero.
            // Food has no warehouse limit, so nothing is capped before exports.
            final int afterUpdate = Math.max(0, stats.getInWarehouseAfter());
            final int afterSpending = afterUpdate - spent;
            final int exported = colony.getCustomHouseExport(type,
                    afterSpending);
            final int beforeLimit = afterSpending - exported;
            final int next = Math.min(beforeLimit, limit);
            return new GoodsLine(type,
                    colony.getWarehouse().getGoods(type).getAmount(),
                    stats.getRowProduction(), stats.getBlockedProduction(),
                    stats.getConsumed(), spent, exported, beforeLimit - next,
                    next, limit);
        }

        /**
         * @return whether anything of the good is stored, produced, used or
         *         changed at the next update
         */
        private boolean isShown() {
            return stock != 0 || produced != 0 || blocked != 0 || consumed != 0
                    || spent != 0 || exported != 0 || lost != 0 || next != 0;
        }

        /** @return type of the good */
        public GoodsType getType() {
            return type;
        }

        /** @return amount stored in the colony now */
        public int getStock() {
            return stock;
        }

        /**
         * @return gross output of the next update, limited by the inputs that
         *         are really available
         */
        public int getProduced() {
            return produced;
        }

        /** @return output that the missing inputs block at the next update */
        public int getBlocked() {
            return blocked;
        }

        /**
         * @return amount used at the next update, including the food of new
         *         colonists and of breeding horses
         */
        public int getConsumed() {
            return consumed;
        }

        /** @return amount the project takes at the next update */
        public int getSpent() {
            return spent;
        }

        /** @return amount the custom house exports at the next update */
        public int getExported() {
            return exported;
        }

        /** @return amount that the warehouse limit throws away */
        public int getLost() {
            return lost;
        }

        /** @return amount stored after the next update */
        public int getNext() {
            return next;
        }

        /** @return warehouse limit of the good */
        public int getLimit() {
            return limit;
        }

        /** @return projected change of the stock, next minus current */
        public int getChange() {
            return next - stock;
        }
    }

    /**
     * How exactly the next update of a colony can be read. The goods and
     * project figures are computed as if nothing else changed; a value other
     * than {@link #NONE} means the update may differ from them, so they must
     * not be presented as the exact next state.
     */
    public enum Uncertainty {
        /** The figures are the figures of the next update. */
        NONE,
        /**
         * Food runs short, so a random resident is lost before output and
         * project are recomputed.
         */
        LOST_COLONIST,
        /** Food runs short and the lost resident is the last one. */
        COLONY_LOST,
        /**
         * A completing warehouse or custom house changes storage in the same
         * update, before exports and limits are applied.
         */
        STORAGE_CHANGE
    }

    /** One owned colony with its residents, its next update and its project. */
    public static final class ColonyStatus {

        private final Colony colony;
        private final int population;
        private final int newColonists;
        private final boolean famine;
        private final Uncertainty uncertainty;
        private final List<GoodsLine> goods;
        private final Project project;

        private ColonyStatus(final Colony colony, final int population,
                final int newColonists, final boolean famine,
                final Uncertainty uncertainty, final List<GoodsLine> goods,
                final Project project) {
            this.colony = colony;
            this.population = population;
            this.newColonists = newColonists;
            this.famine = famine;
            this.uncertainty = uncertainty;
            this.goods = goods;
            this.project = project;
        }

        /**
         * @param colony required valid colony of the report
         * @return the status read from the same figures as the next update
         */
        private static ColonyStatus of(final Colony colony) {
            final ColonyProductionStats stats = colony.getGoodsStats();
            // Births and famine read the food before the update takes its own
            // births and breeding out of it; the goods keep the adjusted
            // figures.
            final int foodAfterResidents = colony.getRawGoodsStats()
                    .getStatsByType(GoodsType.CORN).getInWarehouseAfter();
            final Project project = Project.of(colony, stats);
            final List<GoodsLine> goods = new ArrayList<>();
            for (final GoodsType type : GoodsType.GOOD_TYPES) {
                final GoodsLine line = GoodsLine.of(colony,
                        stats.getStatsByType(type), spentBy(project, type));
                if (line.isShown()) {
                    goods.add(line);
                }
            }
            final int population = colony.getPopulation();
            final boolean famine = foodAfterResidents < 0;
            return new ColonyStatus(colony, population,
                    Colony.newColonistsFor(foodAfterResidents), famine,
                    uncertaintyOf(famine, population, project),
                    List.copyOf(goods), project);
        }

        private static Uncertainty uncertaintyOf(final boolean famine,
                final int population, final Project project) {
            if (famine) {
                return population == 1 ? Uncertainty.COLONY_LOST
                        : Uncertainty.LOST_COLONIST;
            }
            if (project.getState() == ProjectState.COMPLETES
                    && project.getItem().isPresent()
                    && changesStorage(project.getItem().get())) {
                return Uncertainty.STORAGE_CHANGE;
            }
            return Uncertainty.NONE;
        }

        private static boolean changesStorage(final ColonyBuildingItem item) {
            if (!(item instanceof ColonyBuildingItemConstruction)) {
                return false;
            }
            final ConstructionType type = ((ColonyBuildingItemConstruction) item)
                    .getConstructionType();
            return ConstructionType.WAREHOUSES.contains(type)
                    || type == ConstructionType.CUSTOM_HOUSE;
        }

        private static int spentBy(final Project project,
                final GoodsType type) {
            if (project.getState() != ProjectState.COMPLETES) {
                return 0;
            }
            if (type == GoodsType.HAMMERS) {
                return project.getRequiredHammers();
            }
            if (type == GoodsType.TOOLS) {
                return project.getRequiredTools();
            }
            return 0;
        }

        /**
         * @return the live colony this status was read from; the panel uses it
         *         to open the colony after revalidating it
         */
        public Colony getColony() {
            return colony;
        }

        /** @return stable colony ID */
        public int getColonyId() {
            return colony.getId();
        }

        /** @return current name of the colony */
        public String getName() {
            return colony.getName();
        }

        /** @return residents working in the colony; visitors are excluded */
        public int getPopulation() {
            return population;
        }

        /**
         * @return new colonists that the next update creates outside the
         *         colony; zero while food is short
         */
        public int getNewColonists() {
            return newColonists;
        }

        /** @return whether the next update loses one resident to famine */
        public boolean isFamine() {
            return famine;
        }

        /**
         * @return how exactly the goods and project figures describe the next
         *         update; read it before presenting them as exact
         */
        public Uncertainty getUncertainty() {
            return uncertainty;
        }

        /**
         * @return goods with something stored or changing, in goods order; the
         *         output, use, spending and next stock are as if nothing else
         *         changed, see {@link #getUncertainty()}
         */
        public List<GoodsLine> getGoods() {
            return goods;
        }

        /** @return the active project of the colony */
        public Project getProject() {
            return project;
        }
    }

    private final List<ColonyStatus> colonies;

    private ColonyAdvisorReport(final List<ColonyStatus> colonies) {
        this.colonies = colonies;
    }

    /**
     * Build the report of the current state of one colonial nation. Only valid
     * colonies of the owner are listed, ordered by stable ID; colonies of other
     * nations and their private stock are never read.
     *
     * @param owner required colonial nation; the caller has checked that it
     *              belongs to the model
     * @return immutable report of the current state
     */
    static ColonyAdvisorReport calculate(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkArgument(owner.isColonialNation(),
                "Colony advisor belongs to a colonial nation");
        final List<ColonyStatus> statuses = owner.getColonies().stream()
                .filter(Colony::isValid).map(ColonyStatus::of)
                .sorted(Comparator.comparingInt(ColonyStatus::getColonyId))
                .collect(Collectors.toUnmodifiableList());
        return new ColonyAdvisorReport(statuses);
    }

    /** @return the valid colonies of the nation, ordered by stable ID */
    public List<ColonyStatus> getColonies() {
        return colonies;
    }
}
