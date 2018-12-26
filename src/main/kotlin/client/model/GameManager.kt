package client.model

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage

class GameManager(val player: Player) {
    lateinit var game: ChineseCheckersGame
        private set
    val playerId get() = player.id
    var possibleMoves: List<HexMove>? = null
    var moveToBePerformed: HexMove? = null
    var leaderBoard: List<Player>? = null
    private var messageProducedHandler: ((Message) -> Unit)? = null

    enum class Event {
        GameStarted,
        TurnStarted,
        AvailableMovesChanged,
        GameEndedInterrupted,
        GameEndedConcluded,
        PlayerLeft,
        MoveDone,
        PlayerJoined
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
            is ChineseCheckersGameMessage.GameAssigned -> game = message.game
            is ChineseCheckersGameMessage.MoveRequested,
            is ChineseCheckersGameMessage.AvailableMovesRequested -> TODO("error")
            is ChineseCheckersGameMessage.MoveDone -> {
                game.board.applyMove(message.move)
                moveToBePerformed = message.move
                onGameEvent(Event.MoveDone)
            }
            is ChineseCheckersGameMessage.GameStarted -> {
                game.corners.putAll(message.playerCorners)
                onGameEvent(Event.GameStarted)
            }
            is ChineseCheckersGameMessage.PlayerJoined -> {
                game.players.add(message.player)
                onGameEvent(Event.PlayerJoined)
            }
            is ChineseCheckersGameMessage.PlayerLeft -> onGameEvent(Event.PlayerLeft)
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

    fun endTurn(move: HexMove) {
        requestMove(move)
    }

    fun requestAvailableMoves(coord: HexCoord) {
        onMessageProduced(ChineseCheckersGameMessage.AvailableMovesRequested(coord))
    }

    fun requestMove(move: HexMove) {
        onMessageProduced(ChineseCheckersGameMessage.MoveRequested(move))
    }

    fun pass() {
        onMessageProduced(ChineseCheckersGameMessage.PlayerPassed)
    }

    fun exitGame() {
        //onMessageProduced(ChineseCheckersGameMessage.GameEnded(GameResult.Interrupted))
    }

}