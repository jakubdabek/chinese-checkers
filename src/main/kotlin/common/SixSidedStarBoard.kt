package common


class SixSidedStarBoard(val innerHexagonSizeLength: Int) {

    val fields: Map<HexCoords, Field>

    init {
        if (innerHexagonSizeLength < 2)
            throw IllegalArgumentException("Side length out of bounds")
        val n = (innerHexagonSizeLength - 1) * 2
        val m = mutableMapOf<HexCoords, Field>()
        for (x in -n..n) {
            for (y in -n..n) {
                if ((x <  innerHexagonSizeLength && y <  innerHexagonSizeLength && x + y > -innerHexagonSizeLength) ||
                    (x > -innerHexagonSizeLength && y > -innerHexagonSizeLength && x + y <  innerHexagonSizeLength)) {
                    m[HexCoords(x, y)] = Field()
                }
            }
        }
        fields = m
    }


    data class Piece(val playerId: Int)
    class Field(var piece: Piece? = null)

}