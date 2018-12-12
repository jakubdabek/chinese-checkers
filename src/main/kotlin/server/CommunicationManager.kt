package server

import common.Message
import common.MessagingManager
import common.Player
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import utility.nextUniqueInt
import java.io.*
import java.net.ServerSocket
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
                    Random.nextUniqueInt(connections.map { it.connectionId }),
                    newSocket.getInputStream(),
                    newSocket.getOutputStream(),
                    this::receiveMessage,
                    this::handleMessagingError
                )
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

    inner class GameManager {
        init {
            TODO("not implemented")
        }
    }
}
