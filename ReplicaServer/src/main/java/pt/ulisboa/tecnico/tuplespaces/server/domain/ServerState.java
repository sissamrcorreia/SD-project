package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    // Notify all waiting threads
    notifyAll();
  }

  private synchronized String getMatchingTuple(String pattern) {
    for (TupleEntry tupleEntry : this.tuples) {
      String tuple = tupleEntry.getTuple();
      if (tuple.matches(pattern)) {
        ServerMain.debug(ServerState.class.getSimpleName(),
            "Found tuple matching pattern: " + pattern + " -> " + tuple);
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
        // Release the lock and put the thread on hold
        wait();
        // When notified, reacquire lock and try again
        tuple = getMatchingTuple(pattern);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      }
    }
    return tuple;
  }

  // Retrieves tuples matching a pattern, locking them for the client if not already locked.
  public synchronized ArrayList<String> takePhase1(String searchPattern, int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Taking tuple matching pattern: " + searchPattern + " for client: " + clientId);

    ArrayList<String> takenTuples = new ArrayList<>();
    boolean tuplesNotFound = true;

    while (tuplesNotFound) {
      for (TupleEntry tupleEntry : this.tuples) {
        if (tupleEntry.getTuple().matches(searchPattern)) {

          // If the tuple is locked, release all locks for the client and return an empty list
          if (tupleEntry.isLocked()) {

            ServerMain.debug(ServerState.class.getSimpleName(),
                "{takePhase1} Tuple is locked: " + tupleEntry.getTuple());
            takePhase2(clientId);
            return new ArrayList<>();

          } else {
            // If the tuple is not locked, lock it for the client and add it to the result list
            ServerMain.debug(ServerState.class.getSimpleName(),
                "{takePhase1} Tuple is not locked: " + tupleEntry.getTuple());
            tupleEntry.setLocked(true);
            tupleEntry.setLockedByClientID(clientId);
            takenTuples.add(tupleEntry.getTuple());
          }
        }
      }
      // If no tuples were found, wait for a new tuple to be added
      if (takenTuples.isEmpty()) {
        try {
          ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Waiting for new tuple");
          wait(); // Release the lock and wait until notified

        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } else {
        // If tuples were found, stop waiting
        ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Tuple found");
        tuplesNotFound = false;
      }
    }

    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase1} Returning taken tuples: " + takenTuples);
    return takenTuples;
  }

  // Method to release locks held by a specific client
  public synchronized void takePhase2(int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase2} Releasing locks for client: " + clientId);

    for (TupleEntry tupleEntry : this.tuples) {
      // Check if the tuple is locked by the given client
      if (tupleEntry.getLockedByClientID() == clientId) {
        tupleEntry.setLocked(false);
        tupleEntry.setLockedByClientID(-1);
      }
    }
  }

  // Method to remove a tuple from the tuple space for a specific client
  public synchronized void takePhase3(String tuple, int clientId) {
    ServerMain.debug(ServerState.class.getSimpleName(),
        "{takePhase3} Removing tuple: " + tuple + " for client: " + clientId);
    Iterator<TupleEntry> iterator = tuples.iterator();
    while (iterator.hasNext()) {
      TupleEntry tupleEntry = iterator.next();
      if (tupleEntry.getTuple().equals(tuple)) {
        iterator.remove();
      }
    }

    ServerMain.debug(ServerState.class.getSimpleName(), "{takePhase3} Tuple removed: " + tuple + " for client: " + clientId);
  }

  public synchronized ArrayList<String> getTupleSpacesState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Returning all tuples");
    ArrayList<String> tupleSpacesState = new ArrayList<>();
    for (TupleEntry tupleEntry : this.tuples) {
      tupleSpacesState.add(tupleEntry.getTuple());
    }
    return tupleSpacesState;
  }
}