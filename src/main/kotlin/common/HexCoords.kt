package common


data class HexCoords(val x: Int, val y: Int) {
    val z = 0 - x - y

    constructor(x: Int, y: Int, z: Int) : this(x, y) {
        if (z != this.z)
            throw IllegalArgumentException("Invalid hex coordinates")
    }

    fun move(x: Int = 0, y: Int = 0, z: Int = 0) =
        HexCoords(this.x + x, this.y + y, this.z + z)
}

val HexCoords.neighbours
    get() = listOf(
        HexCoords(x - 1, y),
        HexCoords(x + 1, y),
        HexCoords(x - 1, y - 1),
        HexCoords(x + 1, y - 1),
        HexCoords(x - 1, y + 1),
        HexCoords(x + 1, y + 1)
    )