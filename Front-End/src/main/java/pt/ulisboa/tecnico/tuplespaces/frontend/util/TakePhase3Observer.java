package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase3Response;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;

public class TakePhase3Observer implements StreamObserver<TakePhase3Response> {
    FrontEndResponseCollector collector;

    public TakePhase3Observer(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakePhase3Response value) {
        collector.addString("OK");
        FrontEndMain.debug(TakePhase3Observer.class.getSimpleName(), "Received getTupleSpaces response: " + value);
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(TakePhase3Observer.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(TakePhase3Observer.class.getSimpleName(), "Completed");
    }
}