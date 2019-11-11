
fun createServer(port: Int, fn: (TCPConnectionClient) -> Unit): TCPConnectionServer {
    val server = TCPConnectionServer(port)
    server.connected(fn)
    return server
}

fun finishTests(client: TCPConnectionClient, server: TCPConnectionServer) {
    println("Done, disconnecting")
    client.disconnect()
    server.stop()
    println("Done")
}
