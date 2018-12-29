package server

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage
import java.lang.Thread.sleep
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random


class BotMessagingManager(
    connectionId: Int,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> Boolean,
    private val player: Player,
    private val assignedGame: ChineseCheckersGame
) : MessagingManager(connectionId, onMessageReceived, onError) {

    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()
    private val rand = Random(player.id.value)

    override fun launch() {
        while (true) {
            println("[Bot ${connectionId.value}] working")
            val toSend = queue.take()
            processMessage(toSend)
        }
    }

    private var errorCounter: Int = 0

    private fun processMessage(message: Message) {
        println("[Bot ${connectionId.value}] message received: ${message::class.simpleName}")
        if (message is ChineseCheckersGameMessage) {
            val gameMessage: ChineseCheckersGameMessage = message
            when (gameMessage) {
                is ChineseCheckersGameMessage.AvailableMoves -> {
                    chooseAndRequestMove(gameMessage.moves)
                }
                is ChineseCheckersGameMessage.TurnStarted -> {
                    errorCounter = 0
                    beginTurn()
                }
                is ChineseCheckersGameMessage.GameEnded -> {
                    TODO("end game")
                }
                is ChineseCheckersGameMessage.MoveRejected -> {
                    if (errorCounter++ > 10)
                        assert(false) { "Move rejected too many times" }
                    beginTurn()
                }

                is ChineseCheckersGameMessage.PlayerLeftLobby,
                is ChineseCheckersGameMessage.GameStarted,
                is ChineseCheckersGameMessage.MoveDone,
                is ChineseCheckersGameMessage.GameAssigned,
                is ChineseCheckersGameMessage.PlayerJoined -> Unit

                is ChineseCheckersGameMessage.MoveRequested,
                is ChineseCheckersGameMessage.AvailableMovesRequested,
                is ChineseCheckersGameMessage.PlayerPassed -> TODO("error")
            }
        } else {
            TODO("error")
        }
    }

    private fun beginTurn() {
        val board = assignedGame.board
        val positionCoords = choosePawnToMove(board)
        onMessageReceived(ChineseCheckersGameMessage.AvailableMovesRequested(positionCoords))
    }

    private fun choosePawnToMove(board: SixSidedStarBoard): HexCoord {
        val botCorner = assignedGame.corners[player.id]
        val piecePositions = board.fields.entries.filter { it.value.piece?.cornerId == botCorner }.map { it.key }
        return piecePositions[rand.nextInt(piecePositions.size)]
        //TODO("choose fields in a logical way")
    }

    private fun chooseAndRequestMove(availableMoves: List<HexMove>) {
        if (availableMoves.isEmpty()) {
            beginTurn()
            return
        }
        //val chosenMove = availableMoves.sortedBy { -it.destination.z }.first()
        val movesSortedByDirection = sortByDirection(
            availableMoves,
            assignedGame.corners.getValue(player.id),
            assignedGame.players.size
        )
        val chosenMove = movesSortedByDirection.first()
        println("[Bot ${connectionId.value}] available moves: $availableMoves")
        println("[Bot ${connectionId.value}] chosen move: $chosenMove")
        sleep(500)
        onMessageReceived(ChineseCheckersGameMessage.MoveRequested(chosenMove))
    }

    private fun sortByDirection(moves: List<HexMove>, corner: Int, numberOfPlayers: Int) : List<HexMove> {
        //TODO("sort by direction")
        return moves.sortedBy { -it.destination.z }
    }

    override fun sendMessage(message: Message) {
        queue.put(message)
    }

    override fun close() = Unit
}
