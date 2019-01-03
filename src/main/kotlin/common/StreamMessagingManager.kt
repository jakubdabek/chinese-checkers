package common

import java.io.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


class StreamMessagingManager(
    connectionId: Int,
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> OnErrorBehaviour
) : MessagingManager(connectionId, onMessageReceived, onError) {

    private val objectOutput: ObjectOutputStream = ObjectOutputStream(outputStream)
    private val objectInput: ObjectInputStream = ObjectInputStream(inputStream)
    private val queue: BlockingQueue<Message> = LinkedBlockingQueue<Message>()

    override fun launch() {
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
                if (onError(ex, false) != OnErrorBehaviour.CONTINUE)
                    return
            } catch (ex: InterruptedException) {
                if (onError(ex, false) != OnErrorBehaviour.CONTINUE)
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

    override fun sendMessage(message: Message) {
        queue.put(message)
    }

    override fun close() {
        val list = mutableListOf(objectInput, objectOutput, inputStream, outputStream)
        for (closeable in list)
            closeable.close()
    }
}
