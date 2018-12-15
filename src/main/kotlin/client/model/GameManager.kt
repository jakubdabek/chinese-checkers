package client.model

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage

class GameManager(val player: Player, val game: ChineseCheckersGame) {
    val playerId = player.id
    var possibleMoves: List<HexMove>? = null
    var leaderBoard: List<Player>? = null
    private var messageProducedHandler: ((Message) -> Unit)? = null
    enum class Event {
        GameStarted,
        TurnStarted,
        AvailableMovesChanged,
        GameEndedInterrupted,
        GameEndedConcluded,
    }
    private var gameEventHandler: ((Event) -> Unit)? = null

    private fun onMessageProduced(message: Message) {
        messageProducedHandler?.invoke(message)
    }

    private fun onGameEvent(event: Event) {
        gameEventHandler?.invoke(event)
    }

    fun onMessageReceived(message: Message) {
        if (message !is ChineseCheckersGameMessage)
            TODO("error")
        //interpret mess
        //inform board view
        when (message) {
            is ChineseCheckersGameMessage.GameAssigned,
            is ChineseCheckersGameMessage.MoveRequested,
            is ChineseCheckersGameMessage.AvailableMovesRequested -> TODO("error")
            is ChineseCheckersGameMessage.GameStarted -> {
                println(message.playerCorners)
                game.corners.putAll(message.playerCorners)
                onGameEvent(Event.GameStarted)
            }
            is ChineseCheckersGameMessage.PlayerJoined -> {}//TODO()
            is ChineseCheckersGameMessage.PlayerLeft -> TODO()

            is ChineseCheckersGameMessage.GameEnded -> {
                when (message.result) {
                    is GameResult.Interrupted -> onGameEvent(Event.GameEndedInterrupted)
                    is GameResult.Ended -> {
                        leaderBoard = message.result.leaderboard
                        onGameEvent(Event.GameEndedConcluded)
                    }
                }
            }
            is ChineseCheckersGameMessage.TurnStarted -> {
                onGameEvent(Event.TurnStarted)
            }
            is ChineseCheckersGameMessage.AvailableMoves -> {
                possibleMoves = message.moves
                onGameEvent(Event.AvailableMovesChanged)
            }
        }
    }

    fun setMessageProducedHandler(function: (Message) -> Unit) {
        messageProducedHandler = function
    }

    fun setGameEventHandler(function: (Event) -> Unit) {
        gameEventHandler = function
    }

    fun endTurn(target: HexCoord?) {
        target?.let {
            //onMessageProduced(ChineseCheckersGameMessage.MoveRequested(HexMove(listOf(it))))
        }

    }

    fun requestMoves(coord: HexCoord) {
        onMessageProduced(ChineseCheckersGameMessage.AvailableMovesRequested(coord))
    }
    fun pass() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun exitGame() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}