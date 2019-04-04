package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Class help to draw building progress.
 *
 * @param <I>
 *            building item construction or unit
 */
public class BuildingStatus<I extends ColonyBuildingItem> {

    private final I item;

    private final Stat hammers;

    private final Stat tools;

    BuildingStatus(final I item, final int hammersAlreadyHave, final int hammersProduction,
            final int toolsAlreadyHave, final int toolsProduction) {
        this.item = Preconditions.checkNotNull(item);
        hammers = new Stat(hammersAlreadyHave, hammersProduction, item.getRequiredHammers());
        tools = new Stat(toolsAlreadyHave, toolsProduction, item.getRequiredTools());
    }

    public String getName() {
        return item.getName();
    }

    public Optional<Integer> getTurnsToComplete() {
        if (hammers.getTurnsToComplete().isPresent() && tools.getTurnsToComplete().isPresent()) {
            return Optional.of(
                    Math.max(hammers.getTurnsToComplete().get(), tools.getTurnsToComplete().get()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Class holds statistics about one resource.
     */
    public static class Stat {

        private final int required;
        private final int alreadyHave;
        private final int buildPerTurn;

        Stat(final int alreadyHave, final int buildPerTurn, final int required) {
            this.alreadyHave = alreadyHave;
            this.buildPerTurn = buildPerTurn;
            this.required = required;
        }

        /**
         * @return the turnsToComplete
         */
        public Optional<Integer> getTurnsToComplete() {
            if (buildPerTurn == 0) {
                return Optional.empty();
            } else {
                final int produce = required - alreadyHave;
                if (produce % buildPerTurn == 0) {
                    return Optional.of(produce / buildPerTurn);
                } else {
                    return Optional.of(produce / buildPerTurn + 1);
                }
            }
        }

        /**
         * @return the alreadyHave
         */
        public int getAlreadyHave() {
            return alreadyHave;
        }

        /**
         * @return the buildPerTurn
         */
        public int getBuildPerTurn() {
            return buildPerTurn;
        }

        /**
         * @return the required
         */
        public int getRequired() {
            return required;
        }

    }

    /**
     * @return the item
     */
    public I getItem() {
        return item;
    }

    /**
     * @return the tools
     */
    public Stat getTools() {
        return tools;
    }

    /**
     * @return the hammers
     */
    public Stat getHammers() {
        return hammers;
    }

}
