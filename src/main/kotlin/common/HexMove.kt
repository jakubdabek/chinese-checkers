package common

data class HexMove(val movements: List<Pair<HexCoord, HexCoord>>) {
    val origin = movements.first().first
    val destination = movements.last().second
}
