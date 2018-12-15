package common

import java.io.Serializable


data class HexMove(val movements: List<Pair<HexCoord, HexCoord>>) : Serializable {
    val origin = movements.first().first
    val destination = movements.last().second
}
