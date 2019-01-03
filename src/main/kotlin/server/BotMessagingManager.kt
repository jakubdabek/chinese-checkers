package server

import common.*
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage
import java.lang.Thread
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random


class BotMessagingManager(
    connectionId: Int,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> OnErrorBehaviour,
    private val player: Player,
    private val assignedGame: ChineseCheckersGame
) : MessagingManager(connectionId, onMessageReceived, onError) {

    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()
    private val rand = Random(player.id.value)

    override fun launch() {
        while (!Thread.currentThread().isInterrupted) {
            println("[Bot ${connectionId.value}] working")
            val toSend = queue.take()
            processMessage(toSend)
        }
    }

    private var errorCounter: Int = 0
    private var currentPositionAsked: HexCoord? = null
    private val maxPositions = 10
    private var currentMoves = mutableMapOf<HexCoord, List<HexMove>>()

    private fun processMessage(message: Message) {
        println("[Bot ${connectionId.value}] message received: ${message::class.simpleName}")
        if (message is ChineseCheckersGameMessage) {
            val gameMessage: ChineseCheckersGameMessage = message
            when (gameMessage) {
                is ChineseCheckersGameMessage.AvailableMoves -> {
                    addMoves(gameMessage.moves)
                    if (currentMoves.size < maxPositions) {
                        askForAvailableMoves()
                    } else {
                        chooseAndRequestMove()
                    }
                }
                is ChineseCheckersGameMessage.TurnStarted -> {
                    errorCounter = 0
                    currentMoves.clear()
                    askForAvailableMoves()
                }
                is ChineseCheckersGameMessage.GameEnded -> {
                    Thread.currentThread().interrupt()
                }
                is ChineseCheckersGameMessage.MoveRejected -> {
                    if (errorCounter++ > 10)
                        assert(false) { "Move rejected too many times" }
                    askForAvailableMoves()
                }

                is ChineseCheckersGameMessage.PlayerLeftLobby,
                is ChineseCheckersGameMessage.GameStarted,
                is ChineseCheckersGameMessage.MoveDone,
                is ChineseCheckersGameMessage.GameAssigned,
                is ChineseCheckersGameMessage.PlayerJoined -> Unit

                is ChineseCheckersGameMessage.MoveRequested,
                is ChineseCheckersGameMessage.AvailableMovesRequested,
                is ChineseCheckersGameMessage.ExitRequested,
                is ChineseCheckersGameMessage.PlayerPassed -> TODO("error")
            }
        } else {
            TODO("error")
        }
    }

    private fun addMoves(moves: List<HexMove>) {
        if (moves.isEmpty()) {
            currentMoves[currentPositionAsked!!] = listOf()
        } else {
            val origin = moves.asSequence().map { it.origin }.distinctBy { it }.single()
            assert(origin == currentPositionAsked)
            currentMoves[origin] = moves
        }
        currentPositionAsked = null
    }

    private fun askForAvailableMoves() {
        currentPositionAsked = chooseRandomPawn(assignedGame.board)
        onMessageReceived(ChineseCheckersGameMessage.AvailableMovesRequested(currentPositionAsked!!))
    }

    private fun chooseRandomPawn(board: SixSidedStarBoard): HexCoord {
        val botCorner = assignedGame.corners[player.id]
        val piecePositions = board.fields.entries
            .filter { it.value.piece?.cornerId == botCorner }
            .map { it.key }
            .filter { it !in currentMoves }
        return piecePositions[rand.nextInt(piecePositions.size)]
    }

    private fun chooseAndRequestMove() {
        //val chosenMove = availableMoves.sortedBy { -it.destination.z }.first()
        val bestMove = getBestMove(
            currentMoves.flatMap { it.value },
            assignedGame.corners.getValue(player.id),
            assignedGame.players.size
        )
        println("[Bot ${connectionId.value}] available moves: ${currentMoves.flatMap { it.value }}")
        println("[Bot ${connectionId.value}] chosen move: $bestMove")
        Thread.sleep(150)
        if (bestMove != null)
            onMessageReceived(ChineseCheckersGameMessage.MoveRequested(bestMove))
        else
            onMessageReceived(ChineseCheckersGameMessage.PlayerPassed)
    }

    private fun getBestMove(moves: List<HexMove>, corner: Int, numberOfPlayers: Int) : HexMove? {
        return moves.groupBy { opposites.getValue(corner).run { invoke(it.origin) - invoke(it.destination) } }
            .maxBy { it.key }?.value
            ?.let { bestMoves -> bestMoves[rand.nextInt(bestMoves.size)] }
    }

    override fun sendMessage(message: Message) {
        queue.put(message)
    }

    override fun close() = Unit

    companion object {
        val opposites = mapOf(
            0 to { it: HexCoord -> it.z },
            1 to { it: HexCoord -> -it.y },
            2 to { it: HexCoord -> it.x },
            3 to { it: HexCoord -> -it.z },
            4 to { it: HexCoord -> it.y },
            5 to { it: HexCoord -> -it.x }
        )
    }
}
