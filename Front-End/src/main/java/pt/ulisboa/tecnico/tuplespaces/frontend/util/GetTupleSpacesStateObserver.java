package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndMain;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateResponse;

import java.util.ArrayList;
public class GetTupleSpacesStateObserver implements StreamObserver<getTupleSpacesStateResponse> {
    FrontEndResponseCollector collector;

    public GetTupleSpacesStateObserver(FrontEndResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(getTupleSpacesStateResponse value) {
        ArrayList<String> tupleSpacesState = new ArrayList<>(value.getTupleList());
        collector.addStringList(tupleSpacesState);
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