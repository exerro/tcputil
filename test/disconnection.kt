
fun main() {
    val server = createServer(12344) { client ->
        client.receive(::println)
        println("Client connected")
        Thread.sleep(100)
        client.disconnect()
    }

    val client = TCPConnectionClient.connect("localhost", 12344)

    client.disconnected {
        println("Disconnected")
    }

    client.send("Hello world")

    Thread.sleep(100)

    finishTests(client, server)
}
