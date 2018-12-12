package server

import common.Message
import common.Player
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import utility.nextUniqueInt
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CommunicationManager {
    val connections = mutableListOf<MessagingManager>()
    val players = mutableMapOf<Int, Player>()
    val games = mutableListOf<GameManager>()

    fun launch(port: Int) {
        val listener = ServerSocket(port)

        try {
            while (true) {
                val newSocket = listener.accept()
                val connection = MessagingManager(
                    newSocket.getInputStream(),
                    newSocket.getOutputStream(),
                    this::receiveMessage,
                    this::handleMessagingError)
                    TODO("map connections to players")
                    // val id = Random.nextUniqueInt(playerMap.keys.map { it.id })
                    // val player = Player(id, "User#$id")
            }
        } catch (ex: IOException) {

        }
    }

    private fun receiveMessage(connectionId: Int, message: Message) {
        when (message) {
            is ChineseCheckerServerMessage -> handleMessageInternal(connectionId, message)
            is ChineseCheckersGameMessage -> handleGameMessage(connectionId, message)
        }
    }

    private fun handleMessagingError(connectionId: Int, ex: Exception?, fatal: Boolean): Boolean {
        TODO("error handling")
    }

    private fun handleGameMessage(connectionId: Int, gameMessage: ChineseCheckersGameMessage) {
        TODO("message handling")
    }

    private fun handleMessageInternal(connectionId: Int, serverMessage: ChineseCheckerServerMessage) {
        when (serverMessage) {
            is ChineseCheckerServerMessage.ConnectionRequest -> TODO()
            is ChineseCheckerServerMessage.GameRequest -> TODO()
        }
    }

    inner class MessagingManager(
        private val inputStream: InputStream,
        private val outputStream: OutputStream,
        onMessageReceived: (connectionId: Int, Message) -> Unit,
        onError: (connectionId: Int, ex: Exception?, fatal: Boolean) -> Boolean
    ) {
        val connectionId: Int
        private val objectInput: ObjectInputStream
        private val objectOutput: ObjectOutputStream
        val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()
        private val onMessageReceived: (Message) -> Unit
        private val onError: (ex: Exception?, fatal: Boolean) -> Boolean

        init {
            connectionId = Random.nextUniqueInt(connections.map { it.connectionId})
            objectInput = ObjectInputStream(inputStream)
            objectOutput = ObjectOutputStream(outputStream)
            this.onMessageReceived = { onMessageReceived(connectionId, it) }
            this.onError = { ex, fatal -> onError(connectionId, ex, fatal) }
        }

        fun launch() {
            while (true) {
                try {
                    while (true) {
                        val toSend = queue.poll(100, TimeUnit.MILLISECONDS) ?: break
                        objectOutput.writeObject(toSend)
                    }
                    val message = objectInput.readObject() as Message
                    onMessageReceived(message)
                } catch (ex: InterruptedIOException) {

                } catch (ex: IOException) {
                    onError(ex, true)
                }
            }
        }

        override fun equals(other: Any?) =
            super.equals(other) || (other is MessagingManager && connectionId == other.connectionId)

        override fun hashCode() = connectionId.hashCode()

    }

    inner class GameManager {
        init {
            TODO("not implemented")
        }
    }
}
