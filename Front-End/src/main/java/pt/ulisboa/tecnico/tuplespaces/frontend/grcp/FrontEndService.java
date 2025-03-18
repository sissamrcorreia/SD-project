package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

import java.net.ConnectException;
import java.util.ArrayList;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.GetTupleSpacesStateObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.ReadObserver;

public class FrontEndService extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final ManagedChannel[] channels;
    private final TupleSpacesGrpc.TupleSpacesStub[] stub;
    static final int numServers = 3;

    public FrontEndService(String[] targets) {
        channels = new ManagedChannel[numServers];
        stub = new TupleSpacesGrpc.TupleSpacesStub[numServers];

        for (int i = 0; i < numServers; i++) {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Creating channel for target: " + targets[i]);
            channels[i] = ManagedChannelBuilder.forTarget(targets[i]).usePlaintext().build();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Creating stub for target: " + targets[i]);
            stub[i] = TupleSpacesGrpc.newStub(channels[i]);
        }
    }

    // Override the gRPC methods with proper signatures
    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        int numSv = numServers;
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received put request: " + request);
        
        // Forward the request to the backend server
        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded put request");
        PutObserver observer = new PutObserver(collector);
        
        for (int i = 0; i < numServers; i++) {
            this.stub[i].put(request, observer);
        }
        
        PutResponse response = PutResponse.newBuilder().build();
        
        // Wait all servers response
        collector.waitUntilAllReceived(numSv);
        // TODO: Check if all servers are alive, if not decrease numSv
        
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent put response");
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received read request: " + request);
        
        // Forward the request to the backend server
        FrontEndResponseCollector collector = new FrontEndResponseCollector();

        for (int i = 0; i < numServers; i++) {
            this.stub[i].read(request, new ReadObserver(collector));
        }
        
        // Wait for only one server response
        collector.waitUntilAllReceived(1);
        // TODO: If none is alive, return an empty response ?
        
        ReadResponse response = ReadResponse.newBuilder().setResult(collector.getString(0)).build();
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

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request,
                                    StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received getTupleSpacesState request");
        
        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        
        // Forward the request to the backend server

        for (int i = 0; i < numServers; i++) {
            this.stub[i].getTupleSpacesState(request, new GetTupleSpacesStateObserver(collector));
        }
        collector.waitUntilAllReceived(numServers);
        // TODO: Check if all servers are alive, if not decrease numSv

        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder()
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
        this.channels[0].shutdownNow();
    }
}