package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

public class ServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase {
    private ServerState state = new ServerState();

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received put request: " + request.getNewTuple());
        state.put(request.getNewTuple());

        PutResponse response = PutResponse.newBuilder().build();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending put response");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Put request completed");
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received read request: " + request.getSearchPattern());
        String tuple = state.read(request.getSearchPattern());

        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending read response: " + tuple);
        ReadResponse response = ReadResponse.newBuilder().setResult(tuple).build();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending read response: " + response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Read request completed");
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received take request: " + request.getSearchPattern());
        String tuple = state.take(request.getSearchPattern());
        
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending take response: " + tuple);
        TakeResponse response = TakeResponse.newBuilder().setResult(tuple).build();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending take response: " + response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Take request completed");
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received getTupleSpacesState request");
        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(state.getTupleSpacesState()).build();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Sending getTupleSpacesState response: " + response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "getTupleSpacesState request completed");
    }
}