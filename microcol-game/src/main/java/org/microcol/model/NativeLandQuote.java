package org.microcol.model;

/**
 * Immutable offer to buy one native-claimed square for one colonial European.
 * It names the claim owner, price and status seen when it was made. The model
 * checks the live state again before any gold, treasury or right changes, so a
 * stale quote is refused rather than repriced.
 */
public final class NativeLandQuote {

    private final Player player;
    private final Location location;
    private final NativeSettlement claimant;
    private final NativeLandStatus status;
    private final int price;

    NativeLandQuote(final Player player, final Location location,
            final NativeSettlement claimant, final NativeLandStatus status,
            final int price) {
        this.player = player;
        this.location = location;
        this.claimant = claimant;
        this.status = status;
        this.price = price;
    }

    /**
     * @return the European who would pay
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the native-claimed square
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the settlement whose treasury would receive the payment
     */
    public NativeSettlement getClaimant() {
        return claimant;
    }

    /**
     * @return status of the square for this European when quoted
     */
    public NativeLandStatus getStatus() {
        return status;
    }

    /**
     * @return gold this quote would charge; zero unless the status is unpaid
     */
    public int getPrice() {
        return price;
    }

}
