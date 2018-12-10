package common.chinesecheckers

import common.Player
import common.SixSidedStarBoard


class ChineseCheckersGame(
    val board: SixSidedStarBoard,
    val players: MutableList<Player> = mutableListOf(),
    val corners: MutableMap<Int, Int> = mutableMapOf()
) {
    fun fillBoardCorners(cornerIdsMap: Map<Int, Int>) {
        if (cornerIdsMap.values.any { it !in 0..5 })
            throw IllegalArgumentException("Board has 6 corners")
        if (cornerIdsMap.keys != players.map { it.id }.toSet())
            throw IllegalArgumentException("Invalid player IDs")
        corners.putAll(cornerIdsMap)
        board.fillCorners(corners.values)
    }
}
