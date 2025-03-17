package pt.ulisboa.tecnico.tuplespaces.frontend.grcp;

//import java.util.Arrays;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
//import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.*;

import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.FrontEndResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.frontend.util.PutObserver;

public class FrontEndService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {
    int numServers = 3;

    private final ManagedChannel[] channels;
    private final TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stub;
    OrderedDelayer delayer;
    private final int port;

    public FrontEndService(String[] targets, int port) {
        //System.out.println("Targets: " + Arrays.toString(targets));
        this.port = port;
        channels = new ManagedChannel[numServers];
        stub = new TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[numServers];

        for (int i = 0; i < numServers; i++) {
            //System.out.println("Creating channel for target: " + targets[i]);
            channels[i] = ManagedChannelBuilder.forTarget(targets[i]).usePlaintext().build();
            //System.out.println("Channel created: " + channels[i]);

            //System.out.println("Creating stub for target: " + targets[i]);
            stub[i] = TupleSpacesReplicaGrpc.newStub(channels[i]);
            //System.out.println("Stub created: " + stub[i]);
        }

        delayer = new OrderedDelayer(numServers);
    }

    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);

        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "After setting the delay, I'll test it");
        for (Integer i : delayer) {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Now I can send request to stub[" + i + "]");
        }
        FrontEndMain.debug(FrontEndService.class.getSimpleName(),"Done.");
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) { // FIXME: NÃO ENTRA AQUI
        //PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
        String tuple = request.getNewTuple();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Received put request: " + tuple);

        FrontEndResponseCollector collector = new FrontEndResponseCollector();
        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Forwarded put request");

        for (Integer id : delayer) {
            FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sending request to stub[" + id + "]");
            this.stub[id].put(request, new PutObserver(collector));
        }
        collector.waitUntilAllReceived(numServers);

        PutResponse response = PutResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        FrontEndMain.debug(FrontEndService.class.getSimpleName(), "Sent put response");
    }

    // public String read(String pattern) {
    //     ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    //     ResponseCollector collector = new ResponseCollector();
    //     for (Integer id : delayer) {
    //         this.stub[id].read(request, new ReadObserver(collector));
    //     }
    //     collector.waitUntilAllReceived(1);
    //     return collector.getString(0);
    // }

    // TODO: take

    // public ArrayList<String> getTupleSpacesState(int qualifier) {
    //     GetTupleSpacesStateRequest request = GetTupleSpacesStateRequest.newBuilder().build();
    //     ResponseCollector collector = new ResponseCollector();
    //     this.stub[qualifier].getTupleSpacesState(request, new GetTupleSpacesStateObserver(collector));
    //     collector.waitUntilAllReceived(1);
    //     return collector.getStringList();
    // }

    public void shutdown() {
        FrontEndMain.debug(getClass().getSimpleName(), "Shutting down the channels");
        for (ManagedChannel ch : channels)
			ch.shutdown();
    }
}