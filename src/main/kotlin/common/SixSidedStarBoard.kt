package common

import java.awt.Color

class SixSidedStarBoard(val innerHexagonSizeLength: Int) {
    val fields: Map<HexCoord, Field>
    val conditions = mapOf(
        0 to { c: HexCoord -> c.z >=  innerHexagonSizeLength },
        1 to { c: HexCoord -> c.y <= -innerHexagonSizeLength },
        2 to { c: HexCoord -> c.x >=  innerHexagonSizeLength },
        3 to { c: HexCoord -> c.z <= -innerHexagonSizeLength },
        4 to { c: HexCoord -> c.y >=  innerHexagonSizeLength },
        5 to { c: HexCoord -> c.x <= -innerHexagonSizeLength }
    )

    init {
        if (innerHexagonSizeLength < 2)
            throw IllegalArgumentException("Side length too low")
        val n = (innerHexagonSizeLength - 1) * 2
        val m = mutableMapOf<HexCoord, Field>()
        for (x in -n..n) {
            for (y in -n..n) {
                if ((x < innerHexagonSizeLength && y < innerHexagonSizeLength && x + y > -innerHexagonSizeLength) ||
                    (x > -innerHexagonSizeLength && y > -innerHexagonSizeLength && x + y < innerHexagonSizeLength)
                ) {
                    m[HexCoord(x, y)] = Field()
                }
            }
        }
        fields = m
    }

    fun fillCorners(corners: Collection<Int>) {
        for (corner in corners) {
            for (field in fields) {
                if (conditions[corner]?.invoke(field.key) == true) {
                    field.value.piece = Piece(corner)
                }
            }
        }
    }

    data class Piece(val cornerId: Int)
    class Field(var piece: Piece? = null)
}
