import java.util.concurrent.CyclicBarrier

/* Create a server that responds to any messages with that message prepended
   with "Received " and ensure clients get correct response. */
fun main() {
    val server = createServer(12341) { client ->
        client.received {
            it.send("Received $it")
        }
    }

    val client = TCPConnectionClient.connect("localhost", 12341)
    val barrier = CyclicBarrier(2)

    assert(client.request("Hello world").content == "Received Hello world")
    assert(client.request("Hello there").content == "Received Hello there")

    client.request("Hi there") {
        assert(it.content == "Received Hi there")
        barrier.await()
    }

    barrier.await()

    finishTests(client, server)
}
