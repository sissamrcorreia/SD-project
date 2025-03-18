package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import io.grpc.stub.StreamObserver;
// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ReadObserver implements StreamObserver<ReadResponse> {
    FrontEndResponseCollector collector;

    public ReadObserver(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(ReadResponse value) {
        collector.addString(value.getResult());
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(ReadObserver.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(ReadObserver.class.getSimpleName(), "Completed");
    }
}