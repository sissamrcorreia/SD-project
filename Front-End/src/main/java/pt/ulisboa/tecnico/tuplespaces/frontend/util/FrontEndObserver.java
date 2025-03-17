// package pt.ulisboa.tecnico.tuplespaces.frontend.util;

// // PRECISAMOS DESTE FICHEIRO (?) FIXME
// import io.grpc.stub.StreamObserver;
// // import pt.ulisboa.tecnico.tuplespaces.frontend.FrontEndResponseCollector;
// import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;

// public class FrontEndObserver<R> implements StreamObserver<TupleSpacesReplicaGrpc> {
//     FrontEndResponseCollector collector;

//     public FrontEndObserver (FrontEndResponseCollector c) {
//         collector = c;
//     }

//     @Override
//     public void onNext(TupleSpacesReplicaGrpc r) {
//         // collector.addString(r.getGreeting());
//         collector.addString(r.getGetTupleSpacesStateMethod().toString()); // TODO: do this, example
//         System.out.println("Received response: " + r);
//     }

//     @Override
//     public void onError(Throwable throwable) {
//         System.out.println("Received error: " + throwable);
//     }

//     @Override
//     public void onCompleted() {
//         System.out.println("Request completed");
//     }
// }