package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;

public class TakePhase2Observer implements StreamObserver<TakePhase2Response> {
    FrontEndResponseCollector collector;

    public TakePhase2Observer(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakePhase2Response value) {
        collector.addString("OK");
        FrontEndMain.debug(TakePhase2Observer.class.getSimpleName(), "Received getTupleSpaces response: " + value);
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(TakePhase2Observer.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(TakePhase2Observer.class.getSimpleName(), "Completed");
    }
}