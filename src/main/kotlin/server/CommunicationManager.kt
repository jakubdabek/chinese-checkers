package server

import common.Message
import common.MessagingManager
import common.Player
import common.StreamMessagingManager
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersClientMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import utility.nextUniqueInt
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.random.Random


class CommunicationManager {
    private data class Connection(
        val player: Player,
        val messagingManager: MessagingManager,
        val thread: Thread
    )

    private val connections = mutableMapOf<MessagingManager.Id, Connection>()
    private val games = mutableMapOf<Player.Id, GameManager>()

    fun launch(port: Int) {
        val listener = ServerSocket(port)

        try {
            while (true) {
                try {
                    logInfo("Listening for a new connection")
                    val newSocket = listener.accept()
                    logInfo("Socket accepted")
                    val connection = StreamMessagingManager(
                        Random.nextUniqueInt(connections.values.map { it.messagingManager.connectionId.value }),
                        newSocket.getInputStream(),
                        newSocket.getOutputStream(),
                        this::receiveMessage,
                        this::handleMessagingError
                    )
                    logInfo("MessagingManager[${connection.connectionId.value}] created")
                    launchConnection(connection)
                } catch (ex: IOException) {
                    logError("Error during establishing a connection", ex)
                }
            }
        } catch (ex: java.lang.Exception) {
            logError("what", ex)
        }
    }

    private fun launchConnection(messagingManager: MessagingManager, player: Player? = null) {
        val actualPlayer = player ?: run {
            val id = Random.nextUniqueInt(connections.values.map { it.player.id.value })
            Player(id, "User#$id")
        }
        val t = thread(start = false) {
            try {
                messagingManager.use { it.launch() }
            } catch (ex: InterruptedException) {
            } catch (ex: InterruptedIOException) {
            } catch (ex: Exception) {
                onErroneousConnectionTermination(messagingManager.connectionId, ex)
                return@thread
            }
            onNormalConnectionTermination(messagingManager.connectionId)
        }
        connections[messagingManager.connectionId] = Connection(actualPlayer, messagingManager, t)
        t.start()
        logInfo("Connection ${messagingManager.connectionId.value} started")
    }

    private fun onNormalConnectionTermination(connectionId: MessagingManager.Id) {
        logInfo("Connection ${connectionId.value} closed normally")
        onConnectionTermination(connectionId)
    }

    private fun onErroneousConnectionTermination(connectionId: MessagingManager.Id, ex: Exception) {
        logError("Connection ${connectionId.value} terminated due to an error", ex)
        onConnectionTermination(connectionId)
    }

    private fun onConnectionTermination(connectionId: MessagingManager.Id) {
        leaveGame(connections.getValue(connectionId))
        connections.remove(connectionId)
    }

    private fun leaveGame(connection: Connection) {
        games[connection.player.id]?.removePlayer(connection.player)
    }

    private fun receiveMessage(connectionId: MessagingManager.Id, message: Message) {
        logInfo("Message received from ${connectionId.value}: $message")
        when (message) {
            is ChineseCheckerServerMessage -> handleInternalMessage(connectionId, message)
            is ChineseCheckersGameMessage -> handleGameMessage(connectionId, message)
            else -> throw IllegalArgumentException("Server cannot handle this type of message: ${message::class.qualifiedName}")
        }
    }

    private fun handleMessagingError(connectionId: MessagingManager.Id, ex: Exception?, fatal: Boolean): Boolean {
        return when (ex) {
            is InterruptedException, is InterruptedIOException -> {
                logInfo("Connection $connectionId interrupted")
                false
            }
            else -> {
                logError("Error in connection $connectionId", ex)
                !fatal
            }
        }
    }

    private fun logError(err: String, ex: Exception? = null) {
        System.err.println(err)
        ex?.printStackTrace()
    }

    private fun logInfo(info: String) {
        println(info)
    }

    private fun handleGameMessage(connectionId: MessagingManager.Id, gameMessage: ChineseCheckersGameMessage) {
        val connection = connections.getValue(connectionId)
        games[connection.player.id]?.also {
            sendResponses(it.handleGameMessage(gameMessage, connection.player))
        } ?: run {
            logError("Game message from ${connectionId.value} not handled, no active game assigned")
        }
    }

    private fun handleInternalMessage(connectionId: MessagingManager.Id, serverMessage: ChineseCheckerServerMessage) {
        when (serverMessage) {
            is ChineseCheckerServerMessage.ConnectionRequest -> {
                if (checkHandshake(serverMessage.handshake)) {
                    val connection = connections.getValue(connectionId)
                    connection.messagingManager.sendMessage(
                        ChineseCheckersClientMessage.ConnectionEstablished(connection.player))
                }
            }
            is ChineseCheckerServerMessage.GameRequest -> {
                val connection = connections.getValue(connectionId)
                if (connection.player.id !in games) {
                    var assignedGame: GameManager? = null
                    for (game in games.values.toSet()) {
                        if (game.tryAddPlayer(connection.player)) {
                            assignedGame = game
                        }
                    }
                    if (assignedGame == null) {
                        assignedGame = GameManager(
                            serverMessage.playersCount.first(),
                            serverMessage.allowBots,
                            this::sendResponses
                        )
                        if (!assignedGame.tryAddPlayer(connection.player)) {
                            throw Exception("Something went wrong")
                        }
                    }
                    games[connection.player.id] = assignedGame
                    connection.messagingManager.sendMessage(
                        ChineseCheckersGameMessage.GameAssigned(assignedGame.game)
                    )
                } //TODO: else send error?
            }
        }
    }

    private fun sendResponses(responses: List<Response>) {
        for (response in responses) {
            val connection = connections.values.first { it.player == response.recipient }
            if (response.message is ChineseCheckersGameMessage.GameEnded) {
                games.remove(connection.player.id)
            }
            connection.messagingManager.sendMessage(response.message)
        }
    }

    private fun checkHandshake(handshake: Any?): Boolean {
        //TODO("check sensibly")
        return true
    }

}

data class Response(val message: Message, val recipient: Player)
