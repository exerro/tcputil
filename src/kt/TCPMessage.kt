
/** A message received by a connection.
 *  Overrides toString(). */
class TCPMessage internal constructor(
        /** Content of the message (string data). */
        val content: String,
        private val id: MessageID,
        private val connection: TCPConnectionClient
) {
    /** Send a message back to the sender, in response to this message,
     *  expecting no further responses. */
    fun send(data: String)
            = connection.sendWithID(data, id)

    /** Send a message back to the sender, in response to this message,
     *  expecting further responses which will be handled by `fn` */
    fun request(data: String, fn: (TCPMessage) -> Unit)
            = connection.requestWithID(data, id, fn)

    /** Send a message back to the sender, in response to this message,
     *  expecting a single further response that will be returned. */
    fun request(data: String)
            = connection.requestWithID(data, id)

    override fun toString() = content
}
