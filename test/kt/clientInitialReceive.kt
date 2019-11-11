
fun main() {
    val server = createServer(12343) { client ->
        client.send("Hello world")
    }

    val client = TCPConnectionClient.connect("localhost", 12343)
    val message = client.receive()

    assert(message.content == "Hello world")

    finishTests(client, server)
}
