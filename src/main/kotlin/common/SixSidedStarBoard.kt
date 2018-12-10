package common

class SixSidedStarBoard(val innerHexagonSizeLength: Int) {
    val fields: Map<HexCoord, Field>

    init {
        if (innerHexagonSizeLength < 2)
            throw IllegalArgumentException("Side length too low")
        val n = (innerHexagonSizeLength - 1) * 2
        val m = mutableMapOf<HexCoord, Field>()
        for (x in -n..n) {
            for (y in -n..n) {
                if ((x <  innerHexagonSizeLength && y <  innerHexagonSizeLength && x + y > -innerHexagonSizeLength) ||
                    (x > -innerHexagonSizeLength && y > -innerHexagonSizeLength && x + y <  innerHexagonSizeLength)) {
                    m[HexCoord(x, y)] = Field()
                }
            }
        }
        fields = m
    }

    fun fillCorners(corners: Collection<Int>) {
        TODO("Put pieces in specified corners")
    }

    data class Piece(val cornerId: Int)
    class Field(var piece: Piece? = null)
}
