package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Immutable net European sale revenue of one nation over its last ten owner
 * turns. Each owner turn owns one bucket. Starting the owner's next turn moves
 * to the following bucket and clears it, so a sale recorded in one owner turn
 * counts for that turn and the nine after it, then expires. Global rounds and
 * loads never move the window.
 */
final class EuropeSaleWindow {

    /** Number of owner turns a sale stays inside the window. */
    static final int OWNER_TURNS = 10;

    /** Window without any recorded sale. */
    static final EuropeSaleWindow EMPTY = new EuropeSaleWindow(0,
            new int[OWNER_TURNS]);

    /** Index of the bucket of the current owner turn. */
    private final int slot;

    /** Net gold of committed sales per owner turn; never shared or mutated. */
    private final int[] buckets;

    private EuropeSaleWindow(final int slot, final int[] buckets) {
        this.slot = slot;
        this.buckets = buckets;
    }

    /**
     * Restores a saved window of {@code [current slot, ten buckets]}. The
     * all-zero window is not written, so it is rejected as a noncanonical save.
     *
     * @param saved saved slot and buckets
     * @return validated window
     * @throws IllegalArgumentException when the slot, count or gold is invalid
     */
    static EuropeSaleWindow restore(final int[] saved) {
        Preconditions.checkArgument(
                saved != null && saved.length == OWNER_TURNS + 1,
                "Sale window must hold a slot and %s buckets", OWNER_TURNS);
        final int slot = saved[0];
        Preconditions.checkArgument(slot >= 0 && slot < OWNER_TURNS,
                "Sale window slot is out of range");
        final int[] out = new int[OWNER_TURNS];
        for (int index = 0; index < OWNER_TURNS; index++) {
            Preconditions.checkArgument(saved[index + 1] >= 0,
                    "Sale revenue must not be negative");
            out[index] = saved[index + 1];
        }
        final EuropeSaleWindow window = new EuropeSaleWindow(slot, out);
        Preconditions.checkArgument(window.getRevenue() > 0,
                "Empty sale window is not saved");
        return window;
    }

    /**
     * @return the saved form: the current slot followed by every bucket; an
     *         empty window is never written
     */
    int[] save() {
        final int[] out = new int[OWNER_TURNS + 1];
        out[0] = slot;
        System.arraycopy(buckets, 0, out, 1, OWNER_TURNS);
        return out;
    }

    /**
     * @return the window after the owner's next turn starts, with the bucket of
     *         that turn cleared
     */
    EuropeSaleWindow advanceOwnerTurn() {
        final int next = (slot + 1) % OWNER_TURNS;
        final int[] out = buckets.clone();
        out[next] = 0;
        return new EuropeSaleWindow(next, out);
    }

    /**
     * @param received net gold of one committed sale, after tax
     * @return the window including that sale in the current owner turn
     * @throws ArithmeticException when the bucket would leave the int range
     */
    EuropeSaleWindow withSale(final int received) {
        Preconditions.checkArgument(received >= 0, "Sale must not be negative");
        final int[] out = buckets.clone();
        out[slot] = Math.addExact(out[slot], received);
        return new EuropeSaleWindow(slot, out);
    }

    /**
     * @return net gold of committed sales in the last ten owner turns,
     *         including the current one
     * @throws ArithmeticException when the total would leave the int range
     */
    int getRevenue() {
        int total = 0;
        for (final int bucket : buckets) {
            total = Math.addExact(total, bucket);
        }
        return total;
    }

}
