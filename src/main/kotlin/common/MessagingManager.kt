package common

import java.io.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


class MessagingManager(
    connectionId: Int,
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> Boolean
) : AutoCloseable {
    val connectionId = Id(connectionId)
    private val objectInput: ObjectInputStream = ObjectInputStream(inputStream)
    private val objectOutput: ObjectOutputStream = ObjectOutputStream(outputStream)
    private val onMessageReceived: (Message) -> Unit = { onMessageReceived(this.connectionId, it) }
    private val onError: (ex: Exception?, fatal: Boolean) -> Boolean = { ex, fatal -> onError(this.connectionId, ex, fatal) }
    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()


    fun launch() {
        while (true) {
            try {
                while (true) {
                    val toSend = queue.poll(100, TimeUnit.MILLISECONDS) ?: break
                    objectOutput.writeObject(toSend)
                }
                if (inputStream.available() > 0) {
                    val message = objectInput.readObject() as Message
                    onMessageReceived(message)
                }
            } catch (ex: InterruptedIOException) {
                if (!onError(ex, false))
                    return
            } catch (ex: InterruptedException) {
                if (!onError(ex, false))
                    return
            } catch (ex: ClassNotFoundException) {
                onError(ex, true)
                return
            } catch (ex: ClassCastException) {
                onError(ex, true)
                return
            } catch (ex: ObjectStreamException) {
                onError(ex, true)
                return
            } catch (ex: IOException) {
                onError(ex, true)
                return
            }
        }
    }

    fun sendMessage(message: Message) {
        objectOutput.writeObject(message)
    }

    fun sendMessageAsync(message: Message) {
        queue.put(message)
    }

    override fun close() {
        val list = mutableListOf(objectInput, objectOutput, inputStream, outputStream)
        for (closeable in list)
            closeable.close()
    }

    override fun equals(other: Any?) =
        super.equals(other) || (other is MessagingManager && connectionId == other.connectionId)

    override fun hashCode() = connectionId.hashCode()

    data class Id(val value: Int)
}
