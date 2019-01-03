package client.model

import common.Message
import common.OnErrorBehaviour
import common.StreamMessagingManager
import common.chinesecheckers.ChineseCheckerServerMessage
import java.io.InterruptedIOException
import java.net.Socket
import kotlin.concurrent.thread


class CommunicationManager private constructor() {
    private lateinit var streamMessagingManager: StreamMessagingManager
    private lateinit var thread: Thread
    lateinit var gameManager: GameManager
        private set
    private var onMessageReceived: ((Message) -> Unit)? = null
    private var onError: ((Exception?, Boolean) -> OnErrorBehaviour)? = null

    fun registerHandlers(onMessageReceived: (Message) -> Unit, onError: (Exception?, Boolean) -> OnErrorBehaviour) {
        this.onMessageReceived = onMessageReceived
        this.onError = onError
    }

    fun clearHandlers() {
        onMessageReceived = null
        onError = null
    }

    private fun handleError(ex: Exception?, fatal: Boolean): OnErrorBehaviour {
        println("communication error: ${ex?.message ?: "unknown"}")
        if (ex !is InterruptedException && ex !is InterruptedIOException)
            ex?.printStackTrace()
        return onError?.invoke(ex, fatal) ?: OnErrorBehaviour.DIE
    }

    private fun handleMessage(message: Message) {
        println("on message received called ${message::class.simpleName}")
        onMessageReceived?.invoke(message)
    }

    fun sendMessageToServer(message: Message) {
        streamMessagingManager.sendMessage(message)
    }

    fun close() {
        thread.interrupt()
    }

    companion object {
        fun launch(
            onMessageReceived: ((Message) -> Unit),
            onError: ((Exception?, Boolean) -> OnErrorBehaviour),
            ip: String,
            port: Int = DEFAULT_PORT,
            handshake: Any
        ) = CommunicationManager().apply {
            registerHandlers(onMessageReceived, onError)
            try {
                val socket = Socket(ip, port)
                streamMessagingManager = StreamMessagingManager(
                    CONNECTION_ID,
                    socket.getInputStream(),
                    socket.getOutputStream(),
                    { _, message -> handleMessage(message) },
                    { _, ex, fatal -> handleError(ex, fatal) }
                )
                thread = thread {
                    try {
                        streamMessagingManager.use { it.launch() }
                    } catch (ex: InterruptedException) {
                    } catch (ex: InterruptedIOException) {
                    } catch (ex: Exception) {
                        handleError(ex, true)
                    }
                }
                streamMessagingManager.sendMessage(ChineseCheckerServerMessage.ConnectionRequest(handshake))
            } catch (ex: Exception) {
                handleError(ex, true)
            }
        }

        const val DEFAULT_PORT = 8888
        const val CONNECTION_ID = 1
    }
}
