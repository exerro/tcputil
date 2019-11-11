
class TCPMessage internal constructor(
        val content: String,
        private val id: MessageID,
        private val connection: TCPConnectionClient
) {
    fun send(data: String)
            = connection.sendWithID(data, id)

    fun request(data: String, fn: (TCPMessage) -> Unit)
            = connection.requestWithID(data, id, fn)

    fun request(data: String)
            = connection.requestWithID(data, id)

    override fun toString() = content
}
