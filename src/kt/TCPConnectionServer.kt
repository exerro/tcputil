import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.util.concurrent.CyclicBarrier

class TCPConnectionServer(port: Int) {
    var running = true
        private set

    fun stop() {
        running = false
        stopBarrier.await()
    }

    fun connected(fn: (TCPConnectionClient) -> Unit) {
        synchronized(connected) { connected.add(fn) }
    }

    init {
        Thread {
            val server = ServerSocket(port)
            server.soTimeout = 1000

            while (running) {
                try {
                    val socket = server.accept()
                    val client = TCPConnectionClient(socket, false)

                    synchronized(connected) {
                        connected.forEach { it(client) }
                    }
                }
                catch (e: SocketTimeoutException) { /* do nothing */ }
            }

            stopBarrier.await()
        } .start()
    }

    private val stopBarrier = CyclicBarrier(2)
    private val connected: MutableList<(TCPConnectionClient) -> Unit> = mutableListOf()
}
