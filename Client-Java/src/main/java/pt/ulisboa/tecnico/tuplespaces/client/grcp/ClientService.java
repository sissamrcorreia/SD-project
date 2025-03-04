package pt.ulisboa.tecnico.tuplespaces.client.grcp;

import com.google.protobuf.ProtocolStringList;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;

public class ClientService {
  private ManagedChannel channel;
  private final TupleSpacesBlockingStub stub;

  public ClientService(String host_port, int client_id) {
    // Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
    // change here when frontend fixme
    this.channel = ManagedChannelBuilder.forTarget(host_port).usePlaintext().build();

		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub, but an async stub.
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  // Adds tuple t to the tuple space
  public void put(String tuple) {
    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    this.stub.put(request);
  }

// Accepts a tuple description and returns one tuple that matches the description, if it exists.
// This operation blocks the client until a tuple that satisfies the description exists. The tuple is not removed from the tuple space.
  public String read(String pattern) {
    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    ReadResponse response = this.stub.read(request);
    return response.getResult();
  }

  // Accepts a tuple description and returns one tuple that matches the description.
  // This operation blocks the client until a tuple that satisfies the description exists. The tuple is removed from the tuple space.
  public String take(String pattern) {
    TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
    TakeResponse response = this.stub.take(request);
    return response.getResult();
  }

  // Does not take arguments and returns a list of all tuples on each server.
  public void getTupleSpacesState() {
    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();
    getTupleSpacesStateResponse response = this.stub.getTupleSpacesState(request);
    ProtocolStringList TupleList = response.getTupleList();

    System.out.println(TupleList);
  }

  // A Channel should be shutdown before stopping the process.
  public void shutdown() {
    this.channel.shutdownNow();
  }
}