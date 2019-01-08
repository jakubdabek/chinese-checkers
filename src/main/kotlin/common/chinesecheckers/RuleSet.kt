package common.chinesecheckers

import common.GameResult
import common.HexCoord
import common.HexMove
import common.SixSidedStarBoard


class RuleSet(private val rules: List<Rule>) {
    fun checkMove(board: SixSidedStarBoard, move: HexMove) =
        !checkForbiddenMove(board, move) &&
        rules.asSequence()
            .filterIsInstance<MovementRule>()
            .any { it.validateMove(board, move) }

    private fun checkForbiddenMove(board: SixSidedStarBoard, move: HexMove) =
        rules.asSequence()
            .filterIsInstance<ForbiddenMovementRule>()
            .any { it.excludeMove(board, move) }

    fun getPossibleMoves(board: SixSidedStarBoard, position: HexCoord) =
        rules.asSequence()
            .filterIsInstance<MovementRule>()
            .map { it.checkPossibleMoves(board, position) }
            .flatten()
            .filterNot { checkForbiddenMove(board, it) }
            .toList()

    fun checkGameEnd(game: ChineseCheckersGame) =
        rules.asSequence()
            .filterIsInstance<GameEndRule>()
            .map { it.checkGameEnd(game) }
            .firstOrNull { it != null }
}

class RulesetBuilder {
    private val rules = mutableListOf<Rule>()
    val ruleset get() = RuleSet(rules.toList())

    fun addMovementRule(block: (board: SixSidedStarBoard, position: HexCoord) -> List<HexMove>) =
        MovementRule(block).also { rules.add(it) }

    fun addForbiddenMovementRule(block: (board: SixSidedStarBoard, move: HexMove) -> Boolean) =
        ForbiddenMovementRule(block).also { rules.add(it) }

    fun addGameEndRule(block: (game: ChineseCheckersGame) -> GameResult?) =
        GameEndRule(block).also { rules.add(it) }
}

inline fun buildRuleset(block: RulesetBuilder.() -> Unit): RuleSet =
    RulesetBuilder().apply(block).ruleset
