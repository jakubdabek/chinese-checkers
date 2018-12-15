package client.model

import common.Message
import common.MessagingManager
import java.net.Socket
import kotlin.concurrent.thread


class CommunicationManager {
    val DEFAULT_PORT = 8888
    val CONNECTION_ID = 1

    lateinit var messagingManager: MessagingManager
    lateinit var gameManager: GameManager
        private set
    private lateinit var thread: Thread
    private val observersFunctions = mutableListOf<(Message) -> Unit>()

    fun addObserverFunction(function: (Message) -> Unit) {
        observersFunctions.add(function)
    }

    fun removeObserverFunction(function: (Message) -> Unit) {
        observersFunctions.remove(function)
    }

    private fun notifyAllObservers(message: Message) {
        for (func in observersFunctions) {
            func.invoke(message)
        }
    }

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
        notifyAllObservers(message)
    }

    fun sendMessageToServer(message: Message) {
        messagingManager.sendMessageAsync(message)
    }
}