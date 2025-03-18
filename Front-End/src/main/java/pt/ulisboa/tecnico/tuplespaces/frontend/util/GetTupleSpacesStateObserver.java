package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;

import java.util.ArrayList;
//import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;

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
        System.err.println("Error: " + t.getMessage());
    }

    @Override
    public void onCompleted() {}
}