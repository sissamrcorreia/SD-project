package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutResponse;
// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.PutResponse;

public class PutObserver implements StreamObserver<PutResponse> {
    FrontEndResponseCollector collector;

    public PutObserver(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(PutResponse value) {
        collector.addString("OK");
        System.out.println("Received: " + value);
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("Put operation completed.");
    }
}