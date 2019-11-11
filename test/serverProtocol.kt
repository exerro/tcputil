
fun main() {
    val server = createServer(12342) { client ->
        val initial = client.receive()
        val addition = initial.content.toInt()

        client.received {
            it.send((it.content.toInt() + addition).toString())
        }
    }

    val client = TCPConnectionClient.connect("localhost", 12342)

    client.send("15")

    assert(client.request("5").content == "20")
    assert(client.request("7").content == "22")

    finishTests(client, server)
}
