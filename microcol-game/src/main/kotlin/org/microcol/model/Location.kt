package org.microcol.model

import com.google.common.base.MoreObjects
import com.google.common.base.Preconditions

class Location private constructor(val x: Int, val y: Int) {

    val neighbors: List<Location>
        get() = Direction.getVectors()
                .map { direction -> add(direction) }

    val isDirection: Boolean
        get() = Direction.getVectors().contains(this)

    /**
     * Return distance between two locations. It use Euclidean distance.
     *
     * @param location
     * required location
     * @return distance between two locations
     */
    fun getDistance(location: Location): Int {
        Preconditions.checkNotNull(location)

        val diffX = x - location.x
        val diffY = y - location.y
        return Math.round(Math.sqrt((diffX * diffX + diffY * diffY).toDouble())).toInt()
    }

    /**
     * Return distance between two locations. It use Manhattan distance. It's
     * described at [https://en.wiktionary.org/wiki/Manhattan_distance](https://en.wiktionary.org/wiki/Manhattan_distance).
     *
     *
     * This distance is better for path calculations because it nicely work with
     * integers.
     *
     *
     * @param location
     * required location
     * @return distance between two locations
     */
    fun getDistanceManhattan(location: Location): Int {
        Preconditions.checkNotNull(location)

        return Math.abs(x - location.x) + Math.abs(y - location.y)
    }

    fun add(location: Location): Location {
        Preconditions.checkNotNull(location)

        return Location.of(x + location.x, y + location.y)
    }

    fun sub(location: Location): Location {
        Preconditions.checkNotNull(location)

        return Location.of(x - location.x, y - location.y)
    }

    fun isNeighbor(location: Location): Boolean {
        Preconditions.checkNotNull(location)

        return if (equals(location)) {
            false
        } else Math.abs(x - location.x) <= 1 && Math.abs(y - location.y) <= 1

    }

    override fun hashCode(): Int {
        return x + (y shl Integer.SIZE / 2)
    }

    override fun equals(`object`: Any?): Boolean {
        if (`object` == null) {
            return false
        }

        if (`object` !is Location) {
            return false
        }

        val location = `object` as Location?

        return x == location!!.x && y == location.y
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString()
    }

    companion object {

        /**
         * Minimal map x-axe value.
         */
        const val MAP_MIN_X = 1

        /**
         * Minimal map y-axe value.
         */
        const val MAP_MIN_Y = 1

        @JvmStatic
        fun of(x: Int, y: Int): Location {
            return Location(x, y)
        }
    }
}
