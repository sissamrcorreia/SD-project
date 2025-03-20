package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateResponse;

import java.util.ArrayList;
public class GetTupleSpacesStateObserver implements StreamObserver<getTupleSpacesStateResponse> {
    FrontEndResponseListsCollector collector;

    public GetTupleSpacesStateObserver(FrontEndResponseListsCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(getTupleSpacesStateResponse value) {
        ArrayList<String> tupleSpacesState = new ArrayList<>(value.getTupleList());
        collector.addStringList(tupleSpacesState);
        FrontEndMain.debug(GetTupleSpacesStateObserver.class.getSimpleName(), "Received response: " + tupleSpacesState);
    }

    @Override
    public void onError(Throwable t) {
        FrontEndMain.debug(GetTupleSpacesStateObserver.class.getSimpleName(), "Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        FrontEndMain.debug(GetTupleSpacesStateObserver.class.getSimpleName(), "Completed");
    }
}