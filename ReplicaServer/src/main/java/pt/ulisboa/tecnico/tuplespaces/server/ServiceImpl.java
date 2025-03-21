package pt.ulisboa.tecnico.tuplespaces.server;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase3Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.TakePhase3Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

public class ServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {
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
    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received takePhase1 request: " + request.getSearchPattern() + " from client: " + request.getClientId());
        ArrayList<String> reservedTuples = new ArrayList<String>();
        reservedTuples = state.takePhase1(request.getSearchPattern(), request.getClientId());
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Reserved tuples for client " + request.getClientId() + ": " + reservedTuples);
        
        TakePhase1Response response = TakePhase1Response.newBuilder().addAllReservedTuples(reservedTuples).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "takePhase1 request completed for client: " + request.getClientId());
    }

    @Override
    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received takePhase2 request from client: " + request.getClientId());
        state.takePhase2(request.getClientId());

        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Processed takePhase2 for client: " + request.getClientId());
        TakePhase2Response response = TakePhase2Response.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "takePhase2 request completed for client: " + request.getClientId());
    }

    @Override
    public void takePhase3(TakePhase3Request request, StreamObserver<TakePhase3Response> responseObserver) {
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Received takePhase3 request for tuple: " + request.getTuple() + " from client: " + request.getClientId());
        state.takePhase3(request.getTuple(), request.getClientId());

        ServerMain.debug(ServiceImpl.class.getSimpleName(), "Processed takePhase3 for client: " + request.getClientId());
        TakePhase3Response response = TakePhase3Response.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        ServerMain.debug(ServiceImpl.class.getSimpleName(), "takePhase3 request completed for client: " + request.getClientId());
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