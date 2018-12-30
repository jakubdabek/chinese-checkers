package common

import java.io.Serializable
import kotlin.math.abs


data class HexCoord(val x: Int, val y: Int) : Serializable {
    val z = 0 - x - y

    constructor(x: Int, y: Int, z: Int) : this(x, y) {
        if (z != this.z)
            throw IllegalArgumentException("Invalid hex coordinates")
    }

    fun move(x: Int = 0, y: Int = 0, z: Int = 0) =
        HexCoord(this.x + x, this.y + y, this.z + z)

    operator fun plus(other: HexCoord) =
        HexCoord(x + other.x, y + other.y, z + other.z)

    override fun toString() = "HexCoord(x=$x, y=$y, z=$z)"

    companion object {
        val directions
            get() = listOf(
                HexCoord(-1,  0),
                HexCoord( 1,  0),
                HexCoord(-1,  1),
                HexCoord( 1, -1),
                HexCoord( 0,  1),
                HexCoord( 0, -1)
            )
    }
}

infix fun HexCoord.dist(other: HexCoord) =
    listOf(abs(x - other.x), abs(y - other.y), abs(z - other.z)).max()

val HexCoord.neighbours
    get() = HexCoord.directions.map { this + it }
