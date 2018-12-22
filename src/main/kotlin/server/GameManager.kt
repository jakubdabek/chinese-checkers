package server

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage
import common.chinesecheckers.buildRuleset
import kotlin.random.Random


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
    var currentPlayerTurn: Int = -1

    fun tryAddPlayer(player: Player): Boolean {
        if (game.players.size > maxPlayers || (allowBots && game.players.size > 0))
            return false
        addPlayer(player)
        return true
    }

    fun tryAddBot(bot: Bot) : Boolean {
        //TODO() check things?
        if (game.players.size > maxPlayers)
            return false
        addPlayer(bot)
        return true
    }

    private fun getPlayerForCorner(corner: Int): Player =
        game.players.first { it.id == game.corners.entries.first { entry -> entry.value == corner }.key }

    private fun addPlayer(player: Player) {
        onPlayerJoined(game.players.map { Response(ChineseCheckersGameMessage.PlayerJoined(player), it) })
        game.players.add(player)
        if (game.players.size == maxPlayers) {
            prepareCorners()
            val possibleCorners = usableCorners[maxPlayers]!!
            currentPlayerTurn = Random.nextInt(possibleCorners.size)
            onPlayerJoined(game.players.map { Response(ChineseCheckersGameMessage.GameStarted(game.corners.toMap()), it) })
            onPlayerJoined(listOf(
                Response(ChineseCheckersGameMessage.TurnStarted,
                getPlayerForCorner(possibleCorners[currentPlayerTurn])
            )))
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
                    game.board.applyMove(message.move)
                    game.players.map { Response(ChineseCheckersGameMessage.MoveDone(message.move), it) } +
                    usableCorners[maxPlayers]!!.let {
                        currentPlayerTurn = (currentPlayerTurn + 1) % it.size
                        Response(ChineseCheckersGameMessage.TurnStarted, getPlayerForCorner(it[currentPlayerTurn]))
                    }
                } else {
                    respond(ChineseCheckersGameMessage.MoveRejected, sender)
                }
            is ChineseCheckersGameMessage.PlayerPassed -> listOf( //TODO: check sender for current turn
                usableCorners[maxPlayers]!!.let {
                    currentPlayerTurn = (currentPlayerTurn + 1) % it.size
                    Response(ChineseCheckersGameMessage.TurnStarted, getPlayerForCorner(it[currentPlayerTurn]))
                })

        }
    }


    private fun respond(message: ChineseCheckersGameMessage, sender: Player) =
        listOf(Response(message, sender))

    private fun checkMove(move: HexMove, sender: Player): Boolean {
        val ret = ruleset.checkMove(game.board, move)
        val allMoves = ruleset.getPossibleMoves(game.board, move.origin)
        println("move: $move, all moves: $allMoves, move in all moves: ${move in allMoves}, compare: $ret")
        return ret && game.board.fields[move.origin]!!.piece!!.cornerId == game.corners[sender.id]
    }

    private fun checkAvailableMoves(position: HexCoord): List<HexMove> {
        return ruleset.getPossibleMoves(game.board, position)
    }

    companion object {
        val usableCorners = mapOf(
            2 to listOf(0, 3),
            3 to listOf(0, 2, 4),
            4 to listOf(0, 1, 4, 5),
            6 to (0..5).toList()
        )

        val ruleset = buildRuleset {
            // move to adjacent field
            addMovementRule { board, position ->
                position.neighbours
                .filter { pos -> board.fieldEmpty(pos) }
                .map { HexMove(listOf(position to it)) }
            }

            // move with jump
            addMovementRule { board, position ->
                val moves = mutableListOf<HexMove>()
                fun dfs(current: HexCoord, visited: MutableList<HexCoord>) {
                    for (dir in HexCoord.directions) {
                        val adjacent = current + dir
                        if (board.fieldTaken(adjacent)) {
                            val dest = adjacent + dir
                            if (board.fieldEmpty(dest) && dest !in visited) {
                                visited.add(dest)
                                // val move1 = HexMove(visited zip visited.drop(1))
                                // val move2 = HexMove(visited.asSequence().let { it zip it.drop(1) }.toList())
                                // val move3 = HexMove(visited.windowed(2, 1) { it.first() to it.last() })
                                val move = HexMove(visited.zipWithNext())
                                moves.add(move)
                                dfs(dest, visited)
                                visited.removeAt(visited.size - 1)
                            }
                        }
                    }
                }
                dfs(position, mutableListOf(position))
                moves
            }

            addGameEndRule { game ->
                val leaderboard = mutableListOf<Player>()
                for (corner in game.corners) {
                    if (game.board.fields
                            .filter { it.value.piece?.cornerId == corner.value }
                            .all { game.board.conditions[(corner.value + 3) % 6]!!.invoke(it.key) }
                    ) {
                        val playerId = game.corners.entries.first { it.value == corner.value }.key
                        val player = game.players.first { it.id == playerId }
                        leaderboard.add(0, player)
                    }
                }

                if (leaderboard.size > 0)
                    GameResult.Ended(leaderboard)
                else
                    null
            }
        }
    }
}
