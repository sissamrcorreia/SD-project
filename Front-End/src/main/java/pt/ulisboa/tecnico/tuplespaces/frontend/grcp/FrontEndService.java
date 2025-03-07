package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;

public class FrontEndService extends TupleSpacesGrpc.TupleSpacesImplBase {
    private ManagedChannel channel;
    private final TupleSpacesBlockingStub stub;

    public FrontEndService(String tupleSpacesAddress) {
        this.channel = ManagedChannelBuilder.forTarget(tupleSpacesAddress)
                .usePlaintext()
                .build();
        this.stub = TupleSpacesGrpc.newBlockingStub(channel);
    }

    // Override the gRPC methods with proper signatures
    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received put request: " + request);
        // Forward the request to the backend server
        PutResponse response = this.stub.put(request);
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded put request");
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent put response");
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received read request: " + request);
        // Forward the request to the backend server
        ReadResponse response = this.stub.read(request);
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded read request with response: " + response);
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent read response");
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received take request: " + request);
        // Forward the request to the backend server
        TakeResponse response = this.stub.take(request);
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded take request with response: " + response);
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent take response");
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request,
                                    StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received getTupleSpacesState request");
        // Forward the request to the backend server
        getTupleSpacesStateResponse response = this.stub.getTupleSpacesState(request);
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded getTupleSpacesState request with response: " + response);
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent getTupleSpacesState response");
    }

    // A Channel should be shutdown before stopping the process.
    public void shutdown() {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Shutting down the channel");
        this.channel.shutdownNow();
    }
}