package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import pt.ulisboa.tecnico.tuplespaces.server.ServerMain;
import pt.ulisboa.tecnico.tuplespaces.server.util.TupleEntry;

public class ServerState {

  private List<TupleEntry> tuples;

  public ServerState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Creating new ServerState");
    this.tuples = new ArrayList<TupleEntry>();
  }

  public synchronized void put(String tuple) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Adding tuple: " + tuple);
    tuples.add(new TupleEntry(tuple));

    // notify all waiting threads
    notifyAll();
  }

  private synchronized String getMatchingTuple(String pattern) {
    for (TupleEntry tupleEntry : this.tuples) {
      String tuple = tupleEntry.getTuple();
      if (tuple.matches(pattern)) {
        ServerMain.debug(ServerState.class.getSimpleName(), "Found tuple matching pattern: " + pattern + " -> " + tuple);
        return tuple;
      }
    }

    return null;
  }

  public synchronized String read(String pattern) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Reading tuple matching pattern: " + pattern);
    String tuple = getMatchingTuple(pattern);
    while (tuple == null) {
      try {
        // release the lock and put the thread on hold
        wait();
        // when notified, reacquire lock and try again
        tuple = getMatchingTuple(pattern);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      }
    }
    return tuple;
  }

  public synchronized ArrayList<String> takePhase1(String searchPattern, int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Taking tuple matching pattern: " + searchPattern);
    ArrayList<String> takenTuples = new ArrayList<>();
    boolean tuplesNotFound = true;

    while(tuplesNotFound) {
      for (TupleEntry tupleEntry : this.tuples) {
        if (tupleEntry.getTuple().matches(searchPattern)) {
          if (tupleEntry.isLocked()) { // if there is one lock, return an empty list
            ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Tuple is locked: " + tupleEntry.getTuple());
            takePhase2(clientId);
            return new ArrayList<>();
          } else {
            System.out.println("Tuple is not locked");
            tupleEntry.setLocked(true);
            tupleEntry.setLockedByClientID(clientId);
            takenTuples.add(tupleEntry.getTuple());
          }
        }
      }
      // if any tuples were found, stop waiting
      if (takenTuples.isEmpty()) {
        try {
          System.out.println("Waiting for tuple");
          wait(); // wait until a tuple is added
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } else{
        System.out.println("Tuple found");
        tuplesNotFound = false;
      }
    }

    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Returning taken tuples: " + takenTuples);
    return takenTuples;
  }

  public synchronized void takePhase2(int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase2} Releasing locks for client: " + clientId);
    for (TupleEntry tupleEntry : this.tuples) {
      if (tupleEntry.getLockedByClientID() == clientId) {
        tupleEntry.setLocked(false);
        tupleEntry.setLockedByClientID(-1);
      }
    }
  }

  public synchronized void takePhase3(String tuple, int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase3} Removing tuple: " + tuple + " for client: " + clientId);
    Iterator<TupleEntry> iterator = tuples.iterator();
    while (iterator.hasNext()) {
        TupleEntry tupleEntry = iterator.next();
        if (tupleEntry.getTuple().equals(tuple)) { // && tupleEntry.getLockedByClientID() == clientId
          iterator.remove();
        }
    }
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase3} Tuple removed: " + tuple + " for client: " + clientId);
}

  public synchronized ArrayList<String> getTupleSpacesState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Returning all tuples");
    ArrayList<String> tupleSpacesState = new ArrayList<>();
    for (TupleEntry tupleEntry : this.tuples){
      tupleSpacesState.add(tupleEntry.getTuple());
    }
    return tupleSpacesState;
  }
}