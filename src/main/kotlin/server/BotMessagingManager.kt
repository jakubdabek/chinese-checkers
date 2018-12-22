package server

import common.Message
import common.MessagingManager
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class BotMessagingManager(
    connectionId: Int,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> Boolean,
    private var assignedGame: GameManager
) : MessagingManager(connectionId) {

    private val onMessageReceived: (Message) -> Unit = { onMessageReceived(this.connectionId, it) }
    private val onError: (ex: Exception?, fatal: Boolean) -> Boolean =
        { ex, fatal -> onError(this.connectionId, ex, fatal) }
    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()


    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun launch() {
        while (true) {
            val toSend = queue.poll(100, TimeUnit.MILLISECONDS) ?: continue
            TODO() //handle message to send
            processMessage(toSend)

        }
    }

    private fun processMessage(message: Message) {
        when (message) {
            is ChineseCheckersGameMessage.GameStarted -> { TODO("check if should do sth") }
            is ChineseCheckersGameMessage.TurnStarted -> { beginTour() }
            is ChineseCheckersGameMessage.GameEnded -> { TODO() /*end game*/ }
            is ChineseCheckersGameMessage.MoveDone -> {}
        }
    }

    private fun beginTour() {

    }

    override fun sendMessage(message: Message) {
        queue.put(message)
    }

    override fun sendMessageAsync(message: Message) {
        queue.put(message)
    }


}