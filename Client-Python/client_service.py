import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import grpc
from TupleSpaces_pb2 import PutRequest, ReadRequest, ReadResponse, TakeRequest, TakeResponse, getTupleSpacesStateRequest
from TupleSpaces_pb2_grpc import TupleSpacesStub

class ClientService:
    def __init__(self, host_port: str, client_id: int):
        # Create a channel to connect to the server (plaintext communication)
        self.channel = grpc.insecure_channel(host_port)

        # Create a blocking stub for synchronous calls
        self.stub = TupleSpacesStub(self.channel)

    # Adds tuple t to the tuple space
    def put(self, tuple_str: str):
        request = PutRequest(newTuple=tuple_str)
        self.stub.put(request)

    # Reads a tuple without removing it from the tuple space
    def read(self, pattern: str) -> str:
        request = ReadRequest(searchPattern=pattern)
        response: ReadResponse = self.stub.read(request)
        return response.result

    # Takes a tuple, removing it from the tuple space
    def take(self, pattern: str) -> str:
        request = TakeRequest(searchPattern=pattern)
        response: TakeResponse = self.stub.take(request)
        return response.result

    # Gets the full tuple space state
    def get_tuple_spaces_state(self):
        request = getTupleSpacesStateRequest()
        response = self.stub.getTupleSpacesState(request)
        # FIXME: Remover as aspas simples do início e do fim de cada item da lista
        tuples_list = list(response.tuple)

        # Imprimir a lista completa
        print(tuples_list)
        # print(list(response.tuple))

    # Shuts down the channel
    def shutdown(self):
        self.channel.close()

