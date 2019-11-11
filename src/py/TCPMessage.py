from typing import Optional

from TCPConnectionClient import TCPConnectionClient


class TCPMessage:
    def __init__(self, content: str, id: (int, bool), client: TCPConnectionClient):
        self.content = content
        raise NotImplementedError()

    def send(self, data: str):
        """
        Send a message back to the sender, in response to this message,
        expecting no further responses.
        """

        raise NotImplementedError()

    def request(self, data: str, fn=None):
        """
        Send a message back to the sender, in response to this message,
        expecting further responses which will be handled by `fn`, or returning
        the first response message if `fn` is None.
        """

        raise NotImplementedError()

    def __str__(self):
        return self.content
