package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.PutResponse;

// import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutResponse;

public class PutObserver implements StreamObserver<PutResponse> {
    FrontEndResponseCollector collector;

    public PutObserver(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(PutResponse value) {
        collector.addString("OK");
        FrontEndMain.debug(PutObserver.class.getSimpleName(), "Received put response: " + value);
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(PutObserver.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(PutObserver.class.getSimpleName(), "Completed");
    }
}