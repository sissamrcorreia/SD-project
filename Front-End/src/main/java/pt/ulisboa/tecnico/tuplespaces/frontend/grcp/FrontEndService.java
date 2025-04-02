package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass;
import pt.ulisboa.tecnico.tuplespaces.frontend.DelayInterceptor;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseListsCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.GetTupleSpacesStateObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.TakePhase1Observer;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.TakePhase2Observer;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass;

public class FrontEndService extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final ManagedChannel[] channels;
    private final TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stub;

    static final int numServers = 3;
    static final Metadata.Key<String> DELAY = Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER);

    // Executor service to handle requests in a single thread
    // This is used to ensure that the requests are processed in the order they are received.
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

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

    // Retrieves the delay values from the current gRPC context for each server, using default "0" if no delay is specified.
    // The delays are added to the metadata that will be attached to the requests.
    private Metadata[] createMetadataForAllServers() {
        String delay1 = DelayInterceptor.CTX_DELAY1.get(Context.current());
        String delay2 = DelayInterceptor.CTX_DELAY2.get(Context.current());
        String delay3 = DelayInterceptor.CTX_DELAY3.get(Context.current());

        delay1 = delay1 == null ? "0" : delay1;
        delay2 = delay2 == null ? "0" : delay2;
        delay3 = delay3 == null ? "0" : delay3;

        Metadata[] metadataArray = new Metadata[numServers];
        String[] delays = {delay1, delay2, delay3};
        
        for (int i = 0; i < numServers; i++) {
            Metadata metadata = new Metadata();
            metadata.put(DELAY, delays[i]);
            metadataArray[i] = metadata;
        }
        
        return metadataArray;
    }

    public void processPutRequest(TupleSpacesOuterClass.PutRequest request, StreamObserver<TupleSpacesOuterClass.PutResponse> responseObserver) {
        try {
            // Forward the request to the backend server
            FrontEndResponseCollector collector = new FrontEndResponseCollector();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded put request");
            PutObserver observer = new PutObserver(collector);

            Metadata[] metadataArray = createMetadataForAllServers();

            TupleSpacesReplicaOuterClass.PutRequest replicaRequest = TupleSpacesReplicaOuterClass.PutRequest.newBuilder()
                .setNewTuple(request.getNewTuple())
                .build();

            for (int i = 0; i < numServers; i++) {
                FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending put request to server " + i + " with delay " + metadataArray[i]);

                TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stubWithMetadata = this.stub[i]
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadataArray[i]));
                
                stubWithMetadata.put(replicaRequest, observer);
            }
            
            // Send the response back to the client
            TupleSpacesOuterClass.PutResponse response = TupleSpacesOuterClass.PutResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            // Wait all servers response
            collector.waitUntilAllReceived(numServers);

            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent put response");

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public void processTakeRequest(TupleSpacesOuterClass.TakeRequest request, StreamObserver<TupleSpacesOuterClass.TakeResponse> responseObserver) {
        try {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received take request: " + request);

            int clientId = request.getClientId();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Take Client ID: " + clientId);
            String searchPattern = request.getSearchPattern();

            // Determine the voter set for this client
            int[] voterSet = {clientId % numServers, (clientId + 1) % numServers};

            // Phase 1: Request to enter the critical section (Maekawa)
            FrontEndResponseListsCollector phase1Collector = new FrontEndResponseListsCollector();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded take request");
            TakePhase1Observer phase1Observer = new TakePhase1Observer(phase1Collector);

            Metadata[] metadataArray = createMetadataForAllServers();

            TupleSpacesReplicaOuterClass.TakePhase1Request phase1Request = TupleSpacesReplicaOuterClass.TakePhase1Request.newBuilder()
                .setSearchPattern(searchPattern)
                .setClientId(clientId)
                .build();

            // Only send phase1 requests to the voter set, not all replicas
            for (int serverIndex : voterSet) {
                Metadata metadata = new Metadata();
                FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending take request to voter server " + serverIndex + " with delay " + metadataArray[serverIndex]);

                TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stubWithMetadata = this.stub[serverIndex]
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadataArray[serverIndex]));

                FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending Phase 1 request to voter server " + serverIndex);
                
                stubWithMetadata.takePhase1(phase1Request, phase1Observer);
            }

            // Wait for responses from all voters
            phase1Collector.waitUntilAllReceived(voterSet.length);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Phase 1 completed.");

            // Find the intersection of all responses
            // If there are no responses, the intersection is empty, if there are responses, find the intersection
            List<List<String>> allResponses = phase1Collector.getAll();
            List<String> intersection = allResponses.isEmpty()
                ? Collections.emptyList()
                : allResponses.stream()
                    .collect(() -> new ArrayList<>(allResponses.get(0)),
                        (list, response) -> list.retainAll(response),
                        (list1, list2) -> list1.retainAll(list2)
                    );

            // Select a tuple to take
            String selectedTuple = intersection.isEmpty() ? "" : intersection.get(0);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Selected tuple: " + selectedTuple);
            
            // Send the response back to the client
            TupleSpacesOuterClass.TakeResponse response = TupleSpacesOuterClass.TakeResponse.newBuilder()
                .setResult(selectedTuple)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            // Phase 2: Confirm the take operation
            FrontEndResponseCollector phase2Collector = new FrontEndResponseCollector();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Starting Phase 2.");
            TakePhase2Observer phase2Observer = new TakePhase2Observer(phase2Collector);

            TupleSpacesReplicaOuterClass.TakePhase2Request phase2Request = TupleSpacesReplicaOuterClass.TakePhase2Request.newBuilder()
                .setSelectedTuple(selectedTuple)
                .setClientId(clientId)
                .build();

            // Phase 2 requests are sent to ALL replicas for consistency
            for (int i = 0; i < numServers; i++) {
                FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending Phase 2 request to server " + i);
                this.stub[i].takePhase2(phase2Request, phase2Observer);
            }

            // Wait for responses from all voters
            // phase2Collector.waitUntilAllReceived(voterSet.length);
            phase2Collector.waitUntilAllReceived(numServers);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Phase 2 completed.");

            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent take response");

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public void processGetTupleSpacesStateRequest(TupleSpacesOuterClass.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesOuterClass.getTupleSpacesStateResponse> responseObserver) {
        try {                                
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received getTupleSpacesState request");
            
            FrontEndResponseListsCollector collector = new FrontEndResponseListsCollector();
            TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest replicaRequest = TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest.newBuilder().build();
            
            // Forward the request to the backend server
            for (int i = 0; i < numServers; i++) {
                this.stub[i].getTupleSpacesState(replicaRequest, new GetTupleSpacesStateObserver(collector));
            }
            collector.waitUntilAllReceived(numServers);
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "All server responses received");

            TupleSpacesOuterClass.getTupleSpacesStateResponse.Builder responseBuilder = TupleSpacesOuterClass.getTupleSpacesStateResponse.newBuilder();

            // Add lists one by one
            for (List<String> tupleList : collector.getAll()) {
                responseBuilder.addAllTuple(tupleList);
            }

            TupleSpacesOuterClass.getTupleSpacesStateResponse response = responseBuilder.build();

            // Send the response back to the client
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded getTupleSpacesState request with response: " + response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent getTupleSpacesState response");
        
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // Override the gRPC methods with proper signatures
    @Override
    public void put(TupleSpacesOuterClass.PutRequest request, StreamObserver<TupleSpacesOuterClass.PutResponse> responseObserver) {
        singleThreadExecutor.execute(() -> processPutRequest(request, responseObserver));
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received put request: " + request);
        return;
    }

    @Override
    public void read(TupleSpacesOuterClass.ReadRequest request, StreamObserver<TupleSpacesOuterClass.ReadResponse> responseObserver) {
        try {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received read request: " + request);

            // Forward the request to the backend server
            FrontEndResponseCollector collector = new FrontEndResponseCollector();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded read request");
            ReadObserver observer = new ReadObserver(collector);

            Metadata[] metadataArray = createMetadataForAllServers();

            TupleSpacesReplicaOuterClass.ReadRequest replicaRequest = TupleSpacesReplicaOuterClass.ReadRequest.newBuilder()
                .setSearchPattern(request.getSearchPattern())
                .build();

            for (int i = 0; i < numServers; i++) {
                Metadata metadata = new Metadata();
                FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending put request to server " + i + " with delay " + metadataArray[i]);

                TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stubWithMetadata = this.stub[i]
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadataArray[i]));
                
                stubWithMetadata.read(replicaRequest, observer);
            }

            // Wait for only one server response
            collector.waitUntilAllReceived(1);

            TupleSpacesOuterClass.ReadResponse response = TupleSpacesOuterClass.ReadResponse.newBuilder().setResult(collector.getString(0)).build();
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded read request with response: " + response);

            // Send the response back to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent read response");
        
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void take(TupleSpacesOuterClass.TakeRequest request, StreamObserver<TupleSpacesOuterClass.TakeResponse> responseObserver) {
        singleThreadExecutor.execute(() -> processTakeRequest(request, responseObserver));
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received take request: " + request);
        return;
    }
    
    @Override
    public void getTupleSpacesState(TupleSpacesOuterClass.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesOuterClass.getTupleSpacesStateResponse> responseObserver) {
        singleThreadExecutor.execute(() -> processGetTupleSpacesStateRequest(request, responseObserver));
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received getTupleSpacesState request: " + request);
        return;
    }

    // A Channel should be shutdown before stopping the process.
    public void shutdown() {
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Shutting down the channel");
        for (int i = 0; i < numServers; i++) {
            this.channels[i].shutdownNow();
        }
        singleThreadExecutor.shutdownNow();
    }
}