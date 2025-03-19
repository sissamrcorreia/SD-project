package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass;

// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.PutResponse;

// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.ReadRequest;
// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.ReadResponse;

// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest;
// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateResponse;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.GetTupleSpacesStateObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.ReadObserver;

public class FrontEndService extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final ManagedChannel[] channels;
    private final TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stub;

    static final int numServers = 3;

    public FrontEndService(String[] targets) {
        channels = new ManagedChannel[numServers];
        stub = new TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[numServers];

        for (int i = 0; i < numServers; i++) {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Creating channel for target: " + targets[i]);
            channels[i] = ManagedChannelBuilder.forTarget(targets[i]).usePlaintext().build();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Creating stub for target: " + targets[i]);
            stub[i] = TupleSpacesReplicaGrpc.newStub(channels[i]);
        }
    }

    // Override the gRPC methods with proper signatures
    @Override
    public void put(TupleSpacesOuterClass.PutRequest request, StreamObserver<TupleSpacesOuterClass.PutResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received put request: " + request);
        
        // Forward the request to the backend server
        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded put request");
        PutObserver observer = new PutObserver(collector);

        TupleSpacesReplicaOuterClass.PutRequest replicaRequest = TupleSpacesReplicaOuterClass.PutRequest.newBuilder()
            .setNewTuple(request.getNewTuple())
            .build();

        for (int i = 0; i < numServers; i++) {
            this.stub[i].put(replicaRequest, observer);
        }
        
        // Wait all servers response
        collector.waitUntilAllReceived(numServers);

        // TODO: msg no discord
        
        // Send the response back to the client
        TupleSpacesOuterClass.PutResponse response = TupleSpacesOuterClass.PutResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent put response");
    }

    @Override
    public void read(TupleSpacesOuterClass.ReadRequest request, StreamObserver<TupleSpacesOuterClass.ReadResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received read request: " + request);

        // Forward the request to the backend server
        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded read request");
        ReadObserver observer = new ReadObserver(collector);

        TupleSpacesReplicaOuterClass.ReadRequest replicaRequest = TupleSpacesReplicaOuterClass.ReadRequest.newBuilder()
            .setSearchPattern(request.getSearchPattern())
            .build();

        for (int i = 0; i < numServers; i++) {
            this.stub[i].read(replicaRequest, observer);
        }

        // Wait for only one server response
        collector.waitUntilAllReceived(1);

        // TODO: msg no discord

        TupleSpacesOuterClass.ReadResponse response = TupleSpacesOuterClass.ReadResponse.newBuilder().setResult(collector.getString(0)).build();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded read request with response: " + response);

        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent read response");
    }

    // @Override // Etapa B.2
    // public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
    //     FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received take request: " + request);
    //     // Forward the request to the backend server
    //     TakeResponse response = this.stub.take(request);
    //     FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded take request with response: " + response);
    //     // Send the response back to the client
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();
    //     FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent take response");
    // }

    // FIXME: não está a imprimir todos os servidores
    public void getTupleSpacesState(TupleSpacesOuterClass.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesOuterClass.getTupleSpacesStateResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received getTupleSpacesState request");
        
        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        GetTupleSpacesStateObserver observer = new GetTupleSpacesStateObserver(collector);

        TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest replicaRequest = TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest.newBuilder().build();
        
        // Forward the request to the backend server
        for (int i = 0; i < numServers; i++) {
            this.stub[i].getTupleSpacesState(replicaRequest, observer);
        }
        collector.waitUntilAllReceived(numServers);

        // TODO: msg no discord

        TupleSpacesOuterClass.getTupleSpacesStateResponse response = TupleSpacesOuterClass.getTupleSpacesStateResponse.newBuilder()
        .addAllTuple(collector.getStringList())
        .build();

        // Send the response back to the client
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded getTupleSpacesState request with response: " + response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent getTupleSpacesState response");
    }

    // A Channel should be shutdown before stopping the process.
    public void shutdown() {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Shutting down the channel");
        for (int i = 0; i < numServers; i++) {
            this.channels[i].shutdownNow();
        }
    }
}