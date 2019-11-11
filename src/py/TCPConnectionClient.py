
import socket
import time
from typing import Optional

from TCPMessage import TCPMessage


class TCPConnectionClient:
    """
    A connection used for sending messages to another remote client.
    """

    def __init__(self, so: socket, outgoing: bool):
        raise NotImplementedError()

    def send(self, data: str):
        """
        Send a string message, expecting no response.
        """

        raise NotImplementedError()

    def request(self, data: str, fn=None) -> Optional[TCPMessage]:
        """
        Send a string message, with responses handled by `fn`, or with the first
        response message returned if `fn` is None.
        """

        raise NotImplementedError()

    def receive(self, fn=None) -> Optional[TCPMessage]:
        """
        Receive a single message, calling `fn` upon receipt, or returning the
        first message received if `fn` is None.
        """

        raise NotImplementedError()

    def received(self, fn):
        """
        Register a function `fn` to be called whenever a message is received.
        Note that messages may be caught by `receive()` and therefore not be
        handled by this function.
        """

        raise NotImplementedError()

    def disconnected(self, fn):
        """
        Register a function `fn` to be called whenever the connection drops.
        Note that this includes when disconnect() is called.
        """

        raise NotImplementedError()

    def disconnect(self):
        """
        Disconnect.
        """

        raise NotImplementedError()

    @staticmethod
    def connect(host: str, port: int):
        so = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        so.connect((host, port))
        time.sleep(0.1)
        return TCPConnectionClient(so, True)
