package client.model

import common.Message
import common.MessagingManager
import java.net.Socket
import kotlin.concurrent.thread

const val DEFAULT_PORT = 8888
const val CONNECTION_ID = 1

//val comms = CommunicationManager()
//
//fun main(args: Array<String>) {
//    val t = thread {
//        comms.launch("localhost")
//    }
//
//    //comms.messagingManager...
//}

class CommunicationManager() {
    lateinit var messagingManager: MessagingManager
    fun launch(ip: String) {
        val socket = Socket(ip, DEFAULT_PORT)
        messagingManager = MessagingManager(
            CONNECTION_ID,
            socket.getInputStream(),
            socket.getOutputStream(),
            this::onMessageReceived,
            this::onError
        )
//        messagingManager.launch()
//
//        val t = thread {
//
//        }
    }

    private fun onError(connectionId: Int, ex: Exception?, fatal: Boolean): Boolean {
        TODO("not implemented")
    }

    private fun onMessageReceived(connectionId: Int, message: Message) {
        TODO("not implemented")
    }
}