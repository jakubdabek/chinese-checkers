package common

abstract class MessagingManager(connectionId: Int) : AutoCloseable {
    abstract fun launch()
    abstract fun sendMessage(message: Message)
    abstract fun sendMessageAsync(message: Message)
    override fun hashCode() = connectionId.hashCode()
    override fun equals(other: Any?) =
        super.equals(other) || (other is SocketMessagingManager && connectionId == other.connectionId)

    fun initPlayerID(id: Player.Id) {
        playerId = id
    }

    internal lateinit var playerId: Player.Id
    val connectionId = Id(connectionId)

    data class Id(val value: Int)
}
