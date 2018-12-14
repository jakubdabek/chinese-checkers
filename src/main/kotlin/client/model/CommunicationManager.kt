package client.model

import common.Message
import common.MessagingManager
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGame
import java.net.Socket
import kotlin.concurrent.thread



//val comms = CommunicationManager()
//
//fun main(args: Array<String>) {
//    val t = thread {
//        comms.launch("localhost")
//    }
//
//    //comms.messagingManager...
//}

class CommunicationManager {
    val DEFAULT_PORT = 8888
    val CONNECTION_ID = 1

    lateinit var messagingManager: MessagingManager
    lateinit var gameManager: GameManager
        private set
    private lateinit var thread: Thread
    fun launch(ip: String, port: Int = DEFAULT_PORT) {
        thread = thread {
            val socket = Socket(ip, port)
            messagingManager = MessagingManager(
                CONNECTION_ID,
                socket.getInputStream(),
                socket.getOutputStream(),
                this::onMessageReceived,
                this::onError
            )
            messagingManager.launch()
        }
    }

    private fun onError(connectionId: Int, ex: Exception?, fatal: Boolean): Boolean {
        TODO("not implemented")
    }

    private fun onMessageReceived(connectionId: Int, message: Message) {
        TODO("not implemented")
    }
}