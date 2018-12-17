package common.chinesecheckers

import common.GameResult
import common.HexCoord
import common.HexMove
import common.SixSidedStarBoard


sealed class Rule

class MovementRule(
    val checkPossibleMoves: (board: SixSidedStarBoard, position: HexCoord) -> List<HexMove>
) : Rule() {
    fun validateMove(board: SixSidedStarBoard, move: HexMove): Boolean {
        return move in checkPossibleMoves(board, move.origin)
    }
}

class GameEndRule(
    val checkGameEnd: (game: ChineseCheckersGame) -> GameResult?
) : Rule()
