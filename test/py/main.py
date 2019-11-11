
from TCPConnectionClient import TCPConnectionClient
from TCPConnectionServer import TCPConnectionServer

server = TCPConnectionServer(12351)
server.connected(lambda client: print("Client connected"))

client = TCPConnectionClient.connect("localhost", 12351)

server.stop()
