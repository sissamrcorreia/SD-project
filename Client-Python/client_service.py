import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import grpc
from TupleSpaces_pb2 import PutRequest, ReadRequest, ReadResponse, TakeRequest, TakeResponse, getTupleSpacesStateRequest
from TupleSpaces_pb2_grpc import TupleSpacesStub

from debug import Debugger

class ClientService:
    def __init__(self, host_port: str, client_id: int):
        # Create a channel to connect to the server (plaintext communication)
        self.channel = grpc.insecure_channel(host_port)

        # Create a blocking stub for synchronous calls
        self.stub = TupleSpacesStub(self.channel)
        Debugger.debug(f"Client {client_id} connected to the server at {host_port}")

    # Adds tuple t to the tuple space
    def put(self, tuple_str: str):
        Debugger.debug(f"Client {tuple_str} added tuple {tuple_str} to the tuple space")
        request = PutRequest(newTuple=tuple_str)
        self.stub.put(request)
        Debugger.debug(f"Client {tuple_str} added tuple {tuple_str} to the tuple space")

    # Reads a tuple without removing it from the tuple space
    def read(self, pattern: str) -> str:
        Debugger.debug(f"Client {pattern} read tuple {pattern} from the tuple space")
        request = ReadRequest(searchPattern=pattern)
        response: ReadResponse = self.stub.read(request)
        Debugger.debug(f"Client {pattern} read tuple {pattern} from the tuple space")
        return response.result

    # Takes a tuple, removing it from the tuple space
    def take(self, pattern: str) -> str:
        Debugger.debug(f"Client {pattern} took tuple {pattern} from the tuple space")
        request = TakeRequest(searchPattern=pattern)
        Debugger.debug(f"Client {pattern} took tuple {pattern} from the tuple space")
        response: TakeResponse = self.stub.take(request)
        Debugger.debug(f"Client {pattern} took tuple {pattern} from the tuple space")
        return response.result

    # Gets the full tuple space state
    def get_tuple_spaces_state(self):
        Debugger.debug("Client requested the full tuple space state")
        request = getTupleSpacesStateRequest()
        response = self.stub.getTupleSpacesState(request)
        tuples_list = list(response.tuple)

        print("[", end="")
        for i in range(len(tuples_list)):
            tuples_list[i] = tuples_list[i][1:-1]
            print(f"<{tuples_list[i]}>", end="")
            if i != len(tuples_list) - 1:
                print(", ", end="")
        print("]")
        Debugger.debug("Client received the full tuple space state")

    # Shuts down the channel
    def shutdown(self):
        self.channel.close()