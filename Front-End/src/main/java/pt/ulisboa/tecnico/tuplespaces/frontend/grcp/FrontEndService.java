package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseCollector;
// import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseListsCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.GetTupleSpacesStateObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.DelayInterceptor;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.ReadObserver;
// import pt.ulisboa.tecnico.tuplespaces.frontend.util.TakePhase1Observer;
// import pt.ulisboa.tecnico.tuplespaces.frontend.util.TakePhase2Observer;
// import pt.ulisboa.tecnico.tuplespaces.frontend.util.TakePhase3Observer;

public class FrontEndService extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final ManagedChannel[] channels;
    private final TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stub;

    static final int numServers = 3;
    static final Metadata.Key<String> DELAY = Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER);

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

        String delay1 = DelayInterceptor.CTX_DELAY1.get(Context.current());
        String delay2 = DelayInterceptor.CTX_DELAY2.get(Context.current());
        String delay3 = DelayInterceptor.CTX_DELAY3.get(Context.current());

        delay1 = delay1 == null ? "0" : delay1;
        delay2 = delay2 == null ? "0" : delay2;
        delay3 = delay3 == null ? "0" : delay3;

        String[] delays = {delay1, delay2, delay3};

        TupleSpacesReplicaOuterClass.PutRequest replicaRequest = TupleSpacesReplicaOuterClass.PutRequest.newBuilder()
            .setNewTuple(request.getNewTuple())
            .build();

        for (int i = 0; i < numServers; i++) {
            Metadata metadata = new Metadata();
            metadata.put(Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER), delays[i]);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending put request to server " + i + " with delay " + delays[i]);

            TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stubWithMetadata = this.stub[i]
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));

            stubWithMetadata.put(replicaRequest, observer);
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

        String delay1 = DelayInterceptor.CTX_DELAY1.get(Context.current());
        String delay2 = DelayInterceptor.CTX_DELAY2.get(Context.current());
        String delay3 = DelayInterceptor.CTX_DELAY3.get(Context.current());

        delay1 = delay1 == null ? "0" : delay1;
        delay2 = delay2 == null ? "0" : delay2;
        delay3 = delay3 == null ? "0" : delay3;

        String[] delays = {delay1, delay2, delay3};

        TupleSpacesReplicaOuterClass.ReadRequest replicaRequest = TupleSpacesReplicaOuterClass.ReadRequest.newBuilder()
            .setSearchPattern(request.getSearchPattern())
            .build();

        for (int i = 0; i < numServers; i++) {
            Metadata metadata = new Metadata();
            metadata.put(Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER), delays[i]);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending put request to server " + i + " with delay " + delays[i]);

            TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stubWithMetadata = this.stub[i]
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));

            stubWithMetadata.read(replicaRequest, observer);
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

    // @Override
    // public void take(TupleSpacesOuterClass.TakeRequest request, StreamObserver<TupleSpacesOuterClass.TakeResponse> responseObserver) {
    //     FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received take request: " + request);

    //     // Phase 1: Reserve tuples that match the search pattern
    //     FrontEndResponseListsCollector phase1Collector = executeTakePhase1(request);

    //     // Phase 2: Confirm the reservation of the tuples
    //     FrontEndResponseCollector phase2Collector = executeTakePhase2(request, phase1Collector);

    //     // Phase 3: Remove the tuple from the TupleSpace
    //     FrontEndResponseCollector phase3Collector = executeTakePhase3(request, phase1Collector);

    //     // Build and send the response to the client
    //     TupleSpacesOuterClass.TakeResponse response = TupleSpacesOuterClass.TakeResponse.newBuilder()
    //         .setResult(phase1Collector.getList(0).get(0))
    //         .build(); //FIXME

    //     // TupleSpacesOuterClass.TakeResponse response = TupleSpacesOuterClass.TakeResponse.newBuilder()
    //     // .setResult(phase1Collector.getStringList(0).get(0))
    //     // .build();

    //     // Send the response to the client
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();

    //     FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent take response");
    // }

    // // Executes Phase 1 of the take operation: Reserve tuples that match the search pattern.
    // private FrontEndResponseListsCollector executeTakePhase1(TupleSpacesOuterClass.TakeRequest request) {
    //     FrontEndResponseListsCollector phase1Collector = new FrontEndResponseListsCollector();
    //     TakePhase1Observer phase1Observer = new TakePhase1Observer(phase1Collector);

    //     TupleSpacesReplicaOuterClass.TakePhase1Request phase1Request = TupleSpacesReplicaOuterClass.TakePhase1Request.newBuilder()
    //         .setSearchPattern(request.getSearchPattern())
    //         .build();

    //     // Send phase1Request to all servers
    //     for (int i = 0; i < numServers; i++) {
    //         this.stub[i].takePhase1(phase1Request, phase1Observer);
    //     }

    //     // Wait for all servers to respond
    //     phase1Collector.waitUntilAllReceived(numServers);
    //     return phase1Collector; // collector containing the reserved tuples from all servers
    // }

    // // Executes Phase 2 of the take operation: Confirm the reservation of the tuples.
    // private FrontEndResponseCollector executeTakePhase2(TupleSpacesOuterClass.TakeRequest request, FrontEndResponseListsCollector phase1Collector) {
    //     FrontEndResponseCollector phase2Collector = new FrontEndResponseCollector();
    //     TakePhase2Observer phase2Observer = new TakePhase2Observer(phase2Collector);

    //     // phase2Request with the reserved tuples from Phase 1
    //     TupleSpacesReplicaOuterClass.TakePhase2Request phase2Request = TupleSpacesReplicaOuterClass.TakePhase2Request.newBuilder()
    //         .setSearchPattern(request.getSearchPattern()) //FIXME
    //         .addAllReservedTuples(phase1Collector.getList(0))
    //         .build();

    //     // Send phase2Request to all servers
    //     for (int i = 0; i < numServers; i++) {
    //         this.stub[i].takePhase2(phase2Request, phase2Observer);
    //     }

    //     // Wait for all servers to respond
    //     phase2Collector.waitUntilAllReceived(numServers);
    //     return phase2Collector; // collector containing the confirmation responses from all servers
    // }

    // // Executes Phase 3 of the take operation: Remove the tuple from the TupleSpace.
    // private FrontEndResponseCollector executeTakePhase3(TupleSpacesOuterClass.TakeRequest request, FrontEndResponseListsCollector phase1Collector) {
    //     FrontEndResponseCollector phase3Collector = new FrontEndResponseCollector();
    //     TakePhase3Observer phase3Observer = new TakePhase3Observer(phase3Collector);

    //     // phase3Request with the reserved tuples from Phase 1
    //     TupleSpacesReplicaOuterClass.TakePhase3Request phase3Request = TupleSpacesReplicaOuterClass.TakePhase3Request.newBuilder()
    //         .setSearchPattern(request.getSearchPattern()) //FIXME
    //         .addAllReservedTuples(phase1Collector.getList(0))
    //         .build();

    //     // Send phase3Request to all servers
    //     for (int i = 0; i < numServers; i++) {
    //         this.stub[i].takePhase3(phase3Request, phase3Observer);
    //     }

    //     // Wait for all servers to respond
    //     phase3Collector.waitUntilAllReceived(numServers);
    //     return phase3Collector; // collector containing the removal confirmation responses from all servers
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