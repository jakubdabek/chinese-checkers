package server

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage


class GameManager(
    val maxPlayers: Int,
    val allowBots: Boolean,
    private val onPlayerJoined: (List<Response>) -> Unit,
    private val onPlayerLeft: (List<Response>) -> Unit
) {

    constructor(
        maxPlayers: Int,
        allowBots: Boolean,
        onPlayersChanged: (List<Response>) -> Unit
    ) : this(maxPlayers, allowBots, onPlayersChanged, onPlayersChanged)

    val game: ChineseCheckersGame = ChineseCheckersGame(SixSidedStarBoard(5))

    fun tryAddPlayer(player: Player): Boolean {
        if (game.players.size > maxPlayers || (allowBots && game.players.size > 0))
            return false
        addPlayer(player)
        return true
    }

    private fun addPlayer(player: Player) {
        onPlayerJoined(game.players.map { Response(ChineseCheckersGameMessage.PlayerJoined(player), it) })
        game.players.add(player)
        if (game.players.size == maxPlayers) {
            prepareCorners()
            onPlayerJoined(game.players.map { Response(ChineseCheckersGameMessage.GameStarted(game.corners.toMap()), it) })
        }
    }

    private fun prepareCorners() {
        game.fillBoardCorners(
            game.players.map { it.id }.zip(usableCorners[maxPlayers]!!).toMap()
        )
    }

    fun handleGameMessage(message: ChineseCheckersGameMessage, sender: Player): List<Response> {
        return when (message) {
            is ChineseCheckersGameMessage.GameAssigned,
            is ChineseCheckersGameMessage.TurnStarted,
            is ChineseCheckersGameMessage.GameStarted,
            is ChineseCheckersGameMessage.PlayerJoined,
            is ChineseCheckersGameMessage.PlayerLeft,
            is ChineseCheckersGameMessage.GameEnded,
            is ChineseCheckersGameMessage.MoveDone,
            is ChineseCheckersGameMessage.MoveRejected,
            is ChineseCheckersGameMessage.AvailableMoves -> TODO("error")

            is ChineseCheckersGameMessage.AvailableMovesRequested ->
                respond(ChineseCheckersGameMessage.AvailableMoves(checkAvailableMoves(message.position)), sender)
            is ChineseCheckersGameMessage.MoveRequested ->
                if (checkMove(message.move, sender)) {
                    game.players.map { Response(ChineseCheckersGameMessage.MoveDone(message.move), it) }
                } else {
                    respond(ChineseCheckersGameMessage.MoveRejected, sender)
                }
        }
    }


    private fun respond(message: ChineseCheckersGameMessage, sender: Player) =
        listOf(Response(message, sender))
    private fun checkMove(move: HexMove, sender: Player): Boolean {
        return move in checkAvailableMoves(move.origin) &&
                game.board.fields[move.origin]!!.piece!!.cornerId == game.corners[sender.id]
    }

    private fun checkAvailableMoves(position: HexCoord): List<HexMove> {
        return position.neighbours
            .filter { pos -> game.board.fields[pos]?.let { it.piece == null } ?: false }
            .map { HexMove(listOf(position to it)) }
    }

    companion object {
        val usableCorners = mapOf(
            2 to listOf(0, 3),
            3 to listOf(0, 2, 5),
            4 to listOf(0, 1, 4, 5),
            6 to (0..5).toList()
        )
    }
}
