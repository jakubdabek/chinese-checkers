package server

import common.*
import common.chinesecheckers.ChineseCheckersGameMessage
import java.lang.Thread.sleep
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.abs
import kotlin.random.Random

class BotMessagingManager(
    connectionId: Int,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> Boolean,
    private var assignedGame: GameManager
) : MessagingManager(connectionId) {

    private val onMessageReceived: (Message) -> Unit = { onMessageReceived(this.connectionId, it) }
    private val onError: (ex: Exception?, fatal: Boolean) -> Boolean =
        { ex, fatal -> onError(this.connectionId, ex, fatal) }
    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()
    private lateinit var rand: Random


    override fun close() {
        TODO("not implemented")
    }

    override fun launch() {
        rand = Random(playerId.value)
        while (true) {
            println("bot ${playerId.value} working")
            val toSend = queue.take() ?: continue
            processMessage(toSend)
        }
    }

    private fun processMessage(message: Message) {
        when (message) {
            is ChineseCheckersGameMessage.GameAssigned -> { }
            is ChineseCheckersGameMessage.GameStarted -> {
                println("BOT RECEIVED GAME STARTED")
                assignedGame.game.fillBoardCorners(message.playerCorners)
            }
            is ChineseCheckersGameMessage.AvailableMoves -> {
                chooseAndRequestMove(message.moves)
            }
            is ChineseCheckersGameMessage.TurnStarted -> {
                beginTurn()
            }
            is ChineseCheckersGameMessage.GameEnded -> {
                TODO() /*end game*/
            }
            is ChineseCheckersGameMessage.MoveDone -> { }
        }
    }

    private fun beginTurn() {
        val board = assignedGame.game.board
        val positionCoords = choosePawnToMove(board)
        onMessageReceived(ChineseCheckersGameMessage.AvailableMovesRequested(positionCoords))
    }

    private fun choosePawnToMove(board: SixSidedStarBoard): HexCoord {
        val botsCorner = assignedGame.game.corners[playerId]
        val botsFields = board.fields.entries.filter { it.value.piece?.cornerId == botsCorner }
        return botsFields[abs(rand.nextInt() % 10)].key
        //TODO() //choose fields in a different way
    }

    private fun chooseAndRequestMove(hexMoves: List<HexMove>) {
        if (hexMoves.isEmpty()) {
            beginTurn()
            return
        }
        //val chosenMove = hexMoves.sortedBy { -it.destination.z }.first()
        val movesSortedByDirection = sortByDirection(hexMoves,assignedGame.game.corners.getValue(playerId),assignedGame.maxPlayers)
        val chosenMove = movesSortedByDirection.first()
        println("Chosen moves: $hexMoves and chosen move: $chosenMove")
        sleep(500)
        onMessageReceived(ChineseCheckersGameMessage.MoveRequested(chosenMove))
    }

    private fun sortByDirection(moves: List<HexMove>, corner: Int, numberOfPlayers: Int) : List<HexMove> {
        //TODO() sort by direction
        return moves.sortedBy { -it.destination.z }
    }

    override fun sendMessage(message: Message) {
        queue.put(message)
    }

    override fun sendMessageAsync(message: Message) {
        queue.put(message)
    }


}