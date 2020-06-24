import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.util.concurrent.CyclicBarrier

/** A server accepting connections from remote clients. */
class TCPConnectionServer(port: Int) {
    /** Whether the server is currently active. */
    var running = true
        private set

    /** Stop the server. */
    fun stop() {
        running = false
        stopBarrier.await()
    }

    /** Register a function to be called whenever a client connects. */
    fun connected(fn: (TCPConnectionClient) -> Unit) {
        synchronized(connected) { connected.add(fn) }
    }

    /** Expect a single client to connect and return it. */
    fun client(): TCPConnectionClient {
        val barrier = CyclicBarrier(2)
        lateinit var client: TCPConnectionClient

        synchronized(awaiting) {
            awaiting.add {
                client = it
                barrier.await()
            }
        }

        barrier.await()

        return client
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        Thread {
            val server = ServerSocket(port)
            server.soTimeout = 1000

            while (running) {
                try {
                    val socket = server.accept()
                    val fn = synchronized(awaiting) {
                        awaiting.firstOrNull()?.also { awaiting.removeAt(0) } ?:
                        { client -> synchronized(connected) { connected.toList() }
                                .forEach { it(client) } }
                    }

                    fn(TCPConnectionClient(socket, false))
                }
                catch (e: SocketTimeoutException) { /* do nothing */ }
            }

            stopBarrier.await()
        } .start()
    }

    ////////////////////////////////////////////////////////////////////////////

    private val stopBarrier = CyclicBarrier(2)
    private val connected: MutableSet<(TCPConnectionClient) -> Unit> = mutableSetOf()
    private val awaiting: MutableList<(TCPConnectionClient) -> Unit> = mutableListOf()
}
