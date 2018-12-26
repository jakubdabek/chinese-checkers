package common

abstract class MessagingManager(
    connectionId: Int,
    onMessageReceived: (connectionId: Id, Message) -> Unit,
    onError: (connectionId: Id, ex: Exception?, fatal: Boolean) -> Boolean
) : AutoCloseable {

    val connectionId = Id(connectionId)
    protected val onMessageReceived: (Message) -> Unit =
        { onMessageReceived(this.connectionId, it) }
    protected val onError: (ex: Exception?, fatal: Boolean) -> Boolean =
        { ex, fatal -> onError(this.connectionId, ex, fatal) }

    abstract fun launch()
    abstract fun sendMessage(message: Message)

    override fun hashCode() = connectionId.hashCode()
    override fun equals(other: Any?): Boolean {
        if (super.equals(other))
            return true
        if (other is MessagingManager && other::class == this::class)
            return connectionId == other.connectionId
        return false
    }

    data class Id(val value: Int)
}
