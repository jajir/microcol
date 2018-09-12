package org.microcol.model

import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt

data class Location private constructor(val x: Int, val y: Int) {
    operator fun plus(location: Location) = Location.of(x + location.x, y + location.y)

    // Tohle tu nechávám pouze protože to je v API a nechce se mi to teď všude měnit.
    fun add(location: Location) = this + location

    // WTF CO TO JE?
    operator fun minus(location: Location) = Location.of(x - location.x, y - location.y)

    // Tohle tu nechávám pouze protože to je v API a nechce se mi to teď všude měnit.
    fun sub(location: Location) = this - location

    fun isNeighbor(location: Location) = if (equals(location)) false else abs(x - location.x) <= 1 && abs(y - location.y) <= 1

    val neighbors
        get() = Direction.getVectors().map { direction -> add(direction) }

    // WTF CO TO JE?
    val isDirection
        get() = Direction.getVectors().contains(this)

    fun getDistance(location: Location): Int {
        val (diffX, diffY) = this - location
        val tmp = (diffX * diffX + diffY * diffY).toDouble()

        return round(sqrt(tmp)).toInt()
    }

    fun getDistanceManhattan(location: Location) = abs(x - location.x) + abs(y - location.y)

    override fun hashCode() = x + (y shl Integer.SIZE / 2)

    // TAKHLE SE DĚLAJÍ STATICKÉ METODY
    companion object {
        // TOHLE TU VŮBEC NEMÁ BÝT
        const val MAP_MIN_X = 1

        // TOHLE TU VŮBEC NEMÁ BÝT
        const val MAP_MIN_Y = 1

        @JvmStatic
        fun of(x: Int, y: Int): Location {
            return Location(x, y)
        }
    }
}
