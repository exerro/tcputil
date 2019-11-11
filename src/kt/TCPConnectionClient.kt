import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CyclicBarrier

class TCPConnectionClient internal constructor(
        private val socket: Socket,
        private val outgoing: Boolean
) {
    fun send(data: String)
            = sendWithID(data, MessageID(messageID++, outgoing))

    fun request(data: String, fn: (TCPMessage) -> Unit)
            = requestWithID(data, MessageID(messageID++, outgoing), fn)

    fun request(data: String)
            = requestWithID(data, MessageID(messageID++, outgoing))

    fun receive(fn: (TCPMessage) -> Unit) {
        synchronized(receiveQueue) {
            receiveQueue.add(fn)
        }
    }

    fun receive(): TCPMessage {
        val receiver = ReceiveCallback()
        receive(receiver.callback)
        return receiver.await()
    }

    fun receive(n: Int) = (1 .. n).map { receive() }

    fun received(fn: (TCPMessage) -> Unit) {
        synchronized(received) {
            received.add(fn)
        }
    }

    fun disconnected(fn: () -> Unit) {
        synchronized(disconnected) {
            disconnected.add(fn)
        }
    }

    fun disconnect() {
        synchronized(this) {
            if (!socket.isClosed) socket.close()
            if (connected) synchronized(disconnected) { disconnected.forEach { it() } }
            connected = false
        }
    }

    private var connected = true
    private var messageID = 0
    private val receiveMap: MutableMap<MessageID, (TCPMessage) -> Unit> = mutableMapOf()
    private val receiveQueue: MutableList<(TCPMessage) -> Unit> = mutableListOf()
    private val disconnected: MutableList<() -> Unit> = mutableListOf()
    private val received: MutableList<(TCPMessage) -> Unit> = mutableListOf()
    private val ostr = socket.getOutputStream()
    private val istr = socket.getInputStream()

    init {
        Thread {
            while (connected) {
                val packet = decodePacket(istr)

                if (packet == null) {
                    disconnect()
                }
                else {
                    packetReceived(packet.first, packet.second)
                }
            }
        } .start()
    }

    internal fun sendWithID(data: String, id: MessageID) {
        synchronized(this) {
            val bytes = encodePacket(data, id)
            ostr.write(bytes)
        }
    }

    internal fun requestWithID(data: String, id: MessageID, fn: (TCPMessage) -> Unit) {
        synchronized(receiveMap) { receiveMap[id] = fn }
        sendWithID(data, id)
    }

    internal fun requestWithID(data: String, id: MessageID): TCPMessage {
        val receiver = ReceiveCallback()
        request(data, receiver.callback)
        return receiver.await()
    }

    private fun packetReceived(id: MessageID, content: String) {
        val message = TCPMessage(content, id, this)
        val fn = synchronized(receiveMap) {
            synchronized(receiveQueue) {
                receiveMap[id]
                        ?: receiveQueue.firstOrNull()?.also { receiveQueue.removeAt(0) }
                        ?: { message -> synchronized(received) { received.forEach { it(message) } } }
            }
        }

        fn(message)
    }

    companion object {
        fun connect(host: String, port: Int): TCPConnectionClient {
            val socket = Socket(InetAddress.getByName(host), port)
            Thread.sleep(100)
            return TCPConnectionClient(socket, true)
        }
    }
}

private fun encodePacket(data: String, id: MessageID)
        = encodeInteger(data.length) +
          encodeInteger(id.id) +
          byteArrayOf((if (id.out) 1 else 0).toByte()) +
          data.toByteArray()

private fun decodePacket(str: InputStream): Pair<MessageID, String>? {
    try {
        val lb0 = str.read().also { if (it == -1) return null }
        val lb1 = str.read().also { if (it == -1) return null }
        val length = decodeInteger(lb0, lb1)
        val ib0 = str.read().also { if (it == -1) return null }
        val ib1 = str.read().also { if (it == -1) return null }
        val id = decodeInteger(ib0, ib1)
        val out = str.read().also { if (it == -1) return null }.let { it == 1 }
        val bytes = str.readNBytes(length)
        if (bytes.size < length) return null
        return MessageID(id, out) to String(bytes)
    }
    catch (e: SocketException) {
        return null
    }
}

private fun encodeInteger(n: Int)
        = byteArrayOf((n % 256).toByte(), (n / 256).toByte())

private fun decodeInteger(b0: Int, b1: Int)
        = b0 + b1 * 256

private class ReceiveCallback {
    private lateinit var message: TCPMessage
    private val barrier = CyclicBarrier(2)

    val callback: (TCPMessage) -> Unit = {
        message = it
        barrier.await()
    }

    fun await(): TCPMessage {
        barrier.await()
        return message
    }
}
