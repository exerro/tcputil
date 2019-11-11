
fun main() {
    val server = TCPConnectionServer(1234)

    server.connected { client ->
        println("client connected")
        val message = client.receive()
        println("Received '$message'")
        message.send("Response")
        client.send("Other message")
    }

    val client = TCPConnectionClient.connect("localhost", 1234)

    client.received(::println)

    val response = client.request("Hello world")

    println("Response text: '$response'")
}
