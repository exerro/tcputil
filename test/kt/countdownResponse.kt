
fun handleMessage(message: TCPMessage) {
    when {
        message.content == "1" -> message.send("0")
        message.content == "0" -> println("Got to 0")
        else -> message.request((message.content.toInt() - 1).toString(), ::handleMessage)
    }
}

fun main() {
    val server = createServer(12343) { client ->
        client.received(::handleMessage)
    }

    val client = TCPConnectionClient.connect("localhost", 12343)

    client.request("6", ::handleMessage)

    Thread.sleep(100)

    finishTests(client, server)
}
