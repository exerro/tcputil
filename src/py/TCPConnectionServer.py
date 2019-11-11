import socket
import threading

from TCPConnectionClient import TCPConnectionClient


class TCPConnectionServer:
    """
    A server accepting connections from remote clients.
    """

    @property
    def running(self): return self.__running

    ############################################################################

    def stop(self):
        """
        Stop the server.
        """

        self.__running = False
        self.__thread.join()

    def connected(self, fn):
        """
        Register a function to be called whenever a client connects.
        """

        self.__connected.append(fn)

    def client(self):
        """
        Expect a single client to connect and return it.
        """

        raise NotImplementedError()

    ############################################################################

    def __init__(self, port: int):
        self.__so = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__so.settimeout(1)
        self.__so.bind(("127.0.0.1", port))
        self.__so.listen(1)

        self.__connected = []

        self.__running = True

        def fn():
            while self.__running:
                try:
                    conn, addr = self.__so.accept()
                    client = TCPConnectionClient(conn, False)

                    [fn(client) for fn in self.__connected]
                except socket.timeout:
                    pass

        self.__thread = threading.Thread(target=fn)
        self.__thread.start()
