package common

data class HexCoord(val x: Int, val y: Int) {
    val z = 0 - x - y

    constructor(x: Int, y: Int, z: Int) : this(x, y) {
        if (z != this.z)
            throw IllegalArgumentException("Invalid hex coordinates")
    }

    fun move(x: Int = 0, y: Int = 0, z: Int = 0) =
        HexCoord(this.x + x, this.y + y, this.z + z)

    override fun toString() = "HexCoord(x=$x, y=$y, z=$z)"
}

val HexCoord.neighbours
    get() = listOf(
        HexCoord(x - 1, y),
        HexCoord(x + 1, y),
        HexCoord(x - 1, y - 1),
        HexCoord(x + 1, y - 1),
        HexCoord(x - 1, y + 1),
        HexCoord(x + 1, y + 1)
    )
