package client.model

import common.Message
import common.MessagingManager
import common.StreamMessagingManager
import java.io.InterruptedIOException
import java.net.Socket
import kotlin.concurrent.thread


class CommunicationManager {
    val DEFAULT_PORT = 8888
    val CONNECTION_ID = 1

    lateinit var streamMessagingManager: StreamMessagingManager
    lateinit var gameManager: GameManager
        private set
    private lateinit var thread: Thread
    private val onMessageReceivedHandlers = mutableListOf<(Message) -> Unit>()

    fun addObserverFunction(function: (Message) -> Unit) {
        onMessageReceivedHandlers.add(function)
    }

    fun removeObserverFunction(function: (Message) -> Unit) {
        onMessageReceivedHandlers.remove(function)
    }

    private fun notifyAllObservers(message: Message) {
        for (func in onMessageReceivedHandlers.toList()) {
            func.invoke(message)
        }
    }

    fun launch(ip: String, port: Int = DEFAULT_PORT) {
        thread = thread {
            try {
                val socket = Socket(ip, port)
                streamMessagingManager = StreamMessagingManager(
                    CONNECTION_ID,
                    socket.getInputStream(),
                    socket.getOutputStream(),
                    this::onMessageReceived,
                    this::onError
                )
                streamMessagingManager.use { it.launch() }
            } catch (ex: InterruptedException) {
            } catch (ex: InterruptedIOException) {
            } catch (ex: Exception) {
                System.err.println("Messaging manager error")
                ex.printStackTrace()
            }
        }
    }

    fun close() {
        thread.interrupt()
    }

    private fun onError(connectionId: MessagingManager.Id, ex: Exception?, fatal: Boolean): Boolean {
        println("On error called")
        ex?.printStackTrace()
        return false
    }

    private fun onMessageReceived(connectionId: MessagingManager.Id, message: Message) {
        println("on message received called ${message::class.simpleName}")
        notifyAllObservers(message)
    }

    fun sendMessageToServer(message: Message) {
        streamMessagingManager.sendMessage(message)
    }
}