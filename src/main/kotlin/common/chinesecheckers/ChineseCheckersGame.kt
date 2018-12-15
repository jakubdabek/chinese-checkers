package common.chinesecheckers

import common.Player
import common.SixSidedStarBoard


class ChineseCheckersGame(
    val board: SixSidedStarBoard,
    val players: MutableList<Player> = mutableListOf(),
    val corners: MutableMap<Player.Id, Int> = mutableMapOf()
) {
    fun fillBoardCorners(cornerIdsMap: Map<Player.Id, Int>) {
        if (cornerIdsMap.values.any { it !in 0..5 })
            throw IllegalArgumentException("Board has 6 corners")
        if (cornerIdsMap.keys != players.map { it.id }.toSet())
            throw IllegalArgumentException("Invalid player IDs")
        corners.putAll(cornerIdsMap)
        board.fillCorners(corners.values)
    }
}
