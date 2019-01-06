package common

import java.io.Serializable


data class HexMove(val movements: List<Pair<HexCoord, HexCoord>>) : Serializable {
    val origin get() = movements.first().first
    val destination get() = movements.last().second
}
