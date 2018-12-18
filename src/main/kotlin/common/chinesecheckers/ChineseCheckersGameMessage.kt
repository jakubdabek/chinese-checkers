package common.chinesecheckers

import common.*


sealed class ChineseCheckersGameMessage : Message {
    data class GameAssigned(val game: ChineseCheckersGame) : ChineseCheckersGameMessage() {
        override val content = game
    }
    data class GameStarted(val playerCorners: Map<Player.Id, Int>) : ChineseCheckersGameMessage() {
        override val content = playerCorners
    }
    data class PlayerJoined(val player: Player) : ChineseCheckersGameMessage() {
        override val content = player
    }
    data class PlayerLeft(val player: Player) : ChineseCheckersGameMessage() {
        override val content = player
    }
    data class GameEnded(val result: GameResult) : ChineseCheckersGameMessage() {
        override val content = result
    }
    object TurnStarted : ChineseCheckersGameMessage() {
        override val content: Nothing? = null
        override fun equals(other: Any?) = other is TurnStarted
    }
    data class MoveRequested(val move: HexMove): ChineseCheckersGameMessage() {
        override val content = move
    }
    data class AvailableMovesRequested(val position: HexCoord): ChineseCheckersGameMessage() {
        override val content = position
    }
    data class AvailableMoves(val moves: List<HexMove>): ChineseCheckersGameMessage() {
        override val content = moves
    }
    data class MoveDone(val move: HexMove): ChineseCheckersGameMessage() {
        override val content = move
    }
    data class TurnEnded(val player: Player): ChineseCheckersGameMessage() {
        override val content = player
    }
    object MoveRejected: ChineseCheckersGameMessage() {
        override val content: Nothing? = null
        override fun equals(other: Any?) = other is MoveRejected
    }
}
