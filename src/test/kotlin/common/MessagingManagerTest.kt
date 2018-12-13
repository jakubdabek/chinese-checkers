package common

import common.chinesecheckers.ChineseCheckersGameMessage
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions
import java.io.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


internal class MessagingManagerTest {

    private lateinit var messagingManager: MessagingManager
    private lateinit var connection: ConnectionMock

    private class ConnectionMock: AutoCloseable {
        val outputPipeOutputStream = PipedOutputStream()
        val outputPipeInputStream = PipedInputStream(outputPipeOutputStream)
        val outputObjectOutputStream = ObjectOutputStream(outputPipeOutputStream)
        val inputPipeOutputStream = PipedOutputStream()
        val inputPipeInputStream = PipedInputStream(inputPipeOutputStream)
        val lazyInputObjectInputStream = lazy { ObjectInputStream(inputPipeInputStream) }
        val inputObjectInputStream: ObjectInputStream by lazyInputObjectInputStream

        override fun close() {
            outputObjectOutputStream.close()
            outputPipeOutputStream.close()
            outputPipeInputStream.close()

            if (lazyInputObjectInputStream.isInitialized())
                inputObjectInputStream.close()
            inputPipeOutputStream.close()
            inputPipeInputStream.close()
        }

        fun sendMessage(message: Message) {
            outputObjectOutputStream.writeObject(message)
        }

        fun readMessage(): Message {
            return inputObjectInputStream.readObject() as Message
        }

    }


    private var currentErrorDelegate: ((Int, Exception?, Boolean) -> Boolean)? = null
    private var currentMessageDelegate: ((Int, Message) -> Unit)? = null

    private fun onError(connectionId: Int, exception: Exception?, fatal: Boolean): Boolean {
        return currentErrorDelegate?.invoke(connectionId, exception, fatal) ?: false
    }

    private fun receiveMessage(connectionId: Int, message: Message) {
        currentMessageDelegate?.invoke(connectionId, message)
    }

    private val asynchronousExceptionsLock = Any()
    private var asynchronousExceptions = mutableListOf<Throwable>()
    private var asynchronousExceptions2 = LinkedBlockingQueue<Throwable>()

    @BeforeEach
    fun setUp() {
        connection = ConnectionMock()
        messagingManager = MessagingManager(
            123,
            connection.outputPipeInputStream,
            connection.inputPipeOutputStream,
            this::receiveMessage,
            this::onError
        )

    }

    @AfterEach
    fun tearDown() {
        connection.close()
        currentMessageDelegate = null
        currentErrorDelegate = null
        synchronized(asynchronousExceptionsLock) {
            asynchronousExceptions.clear()
            asynchronousExceptions2.clear()
        }
    }

    @Test
    fun readOneTest() {
        val usedMessage = ChineseCheckersGameMessage.TurnStarted
        val count = AtomicInteger(0)
        currentMessageDelegate = { connectionId, message ->
            count.incrementAndGet()
            Assertions.assertEquals(connectionId, messagingManager.connectionId)
            Assertions.assertEquals(message, usedMessage)
        }
        currentErrorDelegate = { _, ex, _ ->
            when(ex) {
                is InterruptedException -> false
                is InterruptedIOException -> false
                else -> Assertions.fail()
            }
        }

        val t = thread(start = true) {
            try {
                messagingManager.launch()
            } catch (ex: Throwable) {
                synchronized(asynchronousExceptionsLock) {
                    asynchronousExceptions.add(ex)
                }
                return@thread
            }
        }
        try {
            connection.sendMessage(usedMessage)
            t.join(2000)
            if (t.isAlive)
                t.interrupt()
            else
                Assertions.fail<String>("MessagingManager terminated prematurely")

            t.join(1000)
            if (t.isAlive)
                Assertions.fail<String>("MessagingManager didn't terminate after interrupt")
        } catch (ex: InterruptedException) {
            Assertions.fail<String>("Test thread interrupted")
        } finally {
            t.interrupt()
            connection.close()
        }
        synchronized(asynchronousExceptionsLock) {
            if (asynchronousExceptions.any())
                throw asynchronousExceptions.first()
        }
        Assertions.assertEquals(1, count.get())
    }

    @Test
    fun writeResponseTest() {
        val usedMessage = ChineseCheckersGameMessage.TurnStarted
        val count = AtomicInteger(0)
        currentMessageDelegate = { _, _ ->
            Assertions.fail("Unexpected message")
        }
        currentErrorDelegate = { _, ex, _ ->
            when(ex) {
                is InterruptedException -> false
                is InterruptedIOException -> false
                else -> Assertions.fail()
            }
        }

        val t = thread(start = true) {
            try {
                messagingManager.launch()
            } catch (ex: Throwable) {
                synchronized(asynchronousExceptionsLock) {
                    asynchronousExceptions.add(ex)
                }
                return@thread
            }
        }
        try {
            Thread.sleep(5000)
            messagingManager.sendMessageAsync(usedMessage)
            val actual = connection.readMessage()
            Assertions.assertEquals(usedMessage, actual)

            t.join(2000)
            if (t.isAlive)
                t.interrupt()
            else
                Assertions.fail<String>("MessagingManager terminated prematurely")

            t.join(1000)
            if (t.isAlive)
                Assertions.fail<String>("MessagingManager didn't terminate after interrupt")
        } catch (ex: InterruptedException) {
            Assertions.fail<String>("Test thread interrupted")
        } finally {
            t.interrupt()
            connection.close()
        }
        synchronized(asynchronousExceptionsLock) {
            if (asynchronousExceptions.any())
                throw asynchronousExceptions.first()
        }
    }

    @Test
    fun equalsTest() {
        val connectionMock = ConnectionMock()
        val messagingManager2 = MessagingManager(
            123,
            connectionMock.outputPipeInputStream,
            connectionMock.inputPipeOutputStream,
            { _, _ -> },
            { _, _, _ -> false }
        )

        Assertions.assertEquals(messagingManager.hashCode(), messagingManager2.hashCode())
        Assertions.assertEquals(messagingManager, messagingManager2)
    }
}