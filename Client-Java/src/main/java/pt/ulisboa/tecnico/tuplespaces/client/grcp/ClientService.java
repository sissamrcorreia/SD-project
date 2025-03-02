package pt.ulisboa.tecnico.tuplespaces.client.grcp;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;

public class ClientService {
  private final TupleSpacesBlockingStub stub;

  public ClientService(String host_port, int client_id) {
    // Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		//final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(host_port).usePlaintext().build();

		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub, but an async stub.
    //TupleSpacesBlockingStub stub = TupleSpaceServiceGrpc.newBlockingStub(channel);
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  // adds tuple t to the tuple space
  public void put(String tuple) {
    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    this.stub.put(request);
  }
  
// accepts a tuple description and returns one tuple that matches the description, if it exists. 
// This operation blocks the client until a tuple that satisfies the description exists. The tuple is not removed from the tuple space.
  public String read(String pattern) {
    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    System.out.println("read request: " + request);
    ReadResponse response = this.stub.read(request); // TODO: Fix this
    System.out.println("read response: " + response);
    return response.getResult();
  }

  // accepts a tuple description and returns one tuple that matches the description. 
  // This operation blocks the client until a tuple that satisfies the description exists. The tuple is removed from the tuple space.
  public void take(String pattern) {
    TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
    this.stub.take(request);
  }

  // does not take arguments and returns a list of all tuples on each server
  public void getTupleSpacesState() {
    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();
    getTupleSpacesStateResponse response = this.stub.getTupleSpacesState(request);
    for (String tuple : response.getTupleList()) {
      System.out.println(tuple);
    }
  }
}