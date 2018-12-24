package server

import common.Message
import common.MessagingManager
import common.SocketMessagingManager
import common.Player
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
        val MessagingManager: MessagingManager,
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
                    val connection = SocketMessagingManager(
                        Random.nextUniqueInt(connections.values.map { it.MessagingManager.connectionId.value }),
                        newSocket.getInputStream(),
                        newSocket.getOutputStream(),
                        this::receiveMessage,
                        this::handleMessagingError
                    )
                    logInfo("SocketMessagingManager[${connection.connectionId.value}] created")
                    launchConnection(connection)
                } catch (ex: IOException) {
                    logError("Error during establishing a connection", ex)
                }
            }
        } catch (ex: java.lang.Exception) {
            logError("what", ex)
        }
    }

    private fun launchConnection(messagingManager: MessagingManager) {
        val id = Random.nextUniqueInt(connections.values.map { it.player.id.value })
        val player = Player(id, "User#$id")
        val t = thread(false) {
            try {
                messagingManager.use { it.launch() }
            } catch (ex: Exception) {
                onErroneousConnectionTermination(messagingManager.connectionId, ex)
                return@thread
            }
            onNormalConnectionTermination(messagingManager.connectionId)
        }
        messagingManager.initPlayerID(player.id)
        connections[messagingManager.connectionId] = Connection(player, messagingManager, t)
        t.start()
        logInfo("Connection ${messagingManager.connectionId.value} started")
    }

    private fun onNormalConnectionTermination(connectionId: MessagingManager.Id) {
        logInfo("Connection ${connectionId.value} closed normally")
    }

    private fun onErroneousConnectionTermination(connectionId: MessagingManager.Id, ex: Exception) {
        logError("Connection ${connectionId.value} terminated due to an error", ex)
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
        val connection = connections[connectionId]!!
        sendResponses(games[connection.player.id]!!.handleGameMessage(gameMessage, connection.player))
    }

    private fun handleInternalMessage(connectionId: MessagingManager.Id, serverMessage: ChineseCheckerServerMessage) {
        when (serverMessage) {
            is ChineseCheckerServerMessage.ConnectionRequest -> {
                if (checkHandshake(serverMessage.handshake)) {
                    val connection = connections[connectionId]!!
                    connection.MessagingManager.sendMessage(
                        ChineseCheckersClientMessage.ConnectionEstablished(connection.player)
                    )
                }
            }
            is ChineseCheckerServerMessage.GameRequest -> {
                val connection = connections[connectionId]!!
                var assignedGame: GameManager? = null
                if (!serverMessage.allowBots) {
                    for (game in games.values.toSet()) {
                        if (game.tryAddPlayer(connection.player)) {
                            assignedGame = game
                        }
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
                if (serverMessage.allowBots) {
                    for (i in 0 until serverMessage.playersCount.first() - 1) {
                        val id = Random.nextUniqueInt(connections.values.map { it.MessagingManager.connectionId.value })
                        val botConnection = BotMessagingManager(
                            id,
                            this::receiveMessage,
                            this::handleMessagingError,
                            assignedGame
                        )
                        launchConnection(botConnection)
                        if (!assignedGame.tryAddBot(connections[botConnection.connectionId]!!.player)) {
                            throw Exception("Something went wrong, error while adding bot")
                        }
                        games[botConnection.playerId] = assignedGame
                    }
                }
                games[connection.player.id] = assignedGame
                connection.MessagingManager.sendMessage(
                    ChineseCheckersGameMessage.GameAssigned(assignedGame.game)
                )
            }
        }
    }

    private fun sendResponses(responses: List<Response>) {
        for (response in responses) {
            val connection = connections.values.first { it.player == response.recipient }
            connection.MessagingManager.sendMessageAsync(response.message)
        }
    }

    private fun checkHandshake(handshake: Any?): Boolean {
        //TODO("check sensibly")
        return true
    }

}

data class Response(val message: Message, val recipient: Player)
