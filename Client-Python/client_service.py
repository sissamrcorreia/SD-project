import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import grpc
from TupleSpaces_pb2 import PutRequest, ReadRequest, ReadResponse, TakeRequest, TakeResponse, getTupleSpacesStateRequest
from TupleSpaces_pb2_grpc import TupleSpacesStub

from debug import Debugger

class ClientService:
    def __init__(self, host_port: str, client_id: int):
        self.client_id = client_id

        # Create a channel to connect to the server (plaintext communication)
        self.channel = grpc.insecure_channel(host_port)
        Debugger.debug(
            f"Client {client_id} connected to the server at {host_port}")

        # Create a blocking stub for synchronous calls
        self.stub = TupleSpacesStub(self.channel)
        Debugger.debug(
            f"Client {client_id} created a blocking stub")

    # Adds tuple t to the tuple space
    def put(self, tuple_str: str):
        try:
            Debugger.debug(
                f"Client {self.client_id} added tuple {tuple_str}")
            request = PutRequest(newTuple=tuple_str)
            self.stub.put(request)

            print("OK")
            Debugger.debug(
                f"Client {self.client_id} added tuple {tuple_str} to the tuple space")
        except:
            print("Server is down. Please try again later.")

    # Reads a tuple without removing it from the tuple space
    def read(self, pattern: str) -> str:
        try:
            Debugger.debug(
                f"Client {self.client_id} read tuple {pattern}")
            request = ReadRequest(searchPattern=pattern)
            response: ReadResponse = self.stub.read(request)
            Debugger.debug(
                f"Client {self.client_id} read tuple {pattern}")

            print("OK")
            return response.result

        except grpc.RpcError as e:
            print("Server is down. Please try again later.")

    # Takes a tuple, removing it from the tuple space
    def take(self, pattern: str) -> str:
        try:
            Debugger.debug(
                f"Client {self.client_id} took tuple {pattern}")
            request = TakeRequest(searchPattern=pattern)
            Debugger.debug(
                f"Client {self.client_id} took tuple {pattern}")
            response: TakeResponse = self.stub.take(request)
            Debugger.debug(
                f"Client {self.client_id} took tuple {pattern}")

            print("OK")
            return response.result

        except grpc.RpcError as e:
            print("Server is down. Please try again later.")

    # Gets the full tuple space state
    def get_tuple_spaces_state(self):
        try:
            Debugger.debug(f"Client {self.client_id} requested the full tuple space state")
            request = getTupleSpacesStateRequest()
            response = self.stub.getTupleSpacesState(request)
            tuples_list = list(response.tuple)

            print("OK")
            print("[", end="")
            for i in range(len(tuples_list)):
                tuples_list[i] = tuples_list[i][1:-1]
                print(f"<{tuples_list[i]}>", end="")
                if i != len(tuples_list) - 1:
                    print(", ", end="")
            print("]")
            Debugger.debug(f"Client {self.client_id} received the full tuple space state")

        except grpc.RpcError as e:
            print("Server is down. Please try again later.")

    # Shuts down the channel
    def shutdown(self):
        self.channel.close()