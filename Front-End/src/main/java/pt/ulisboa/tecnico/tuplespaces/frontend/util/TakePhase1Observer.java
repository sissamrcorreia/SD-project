package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;

public class TakePhase1Observer implements StreamObserver<TakePhase1Response> {
    FrontEndResponseListsCollector collector;

    public TakePhase1Observer(FrontEndResponseListsCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakePhase1Response value) {
        List<String> reservedTuples = value.getReservedTuplesList();
        collector.addStringList(reservedTuples);
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(TakePhase1Observer.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(TakePhase1Observer.class.getSimpleName(), "Completed");
    }
}