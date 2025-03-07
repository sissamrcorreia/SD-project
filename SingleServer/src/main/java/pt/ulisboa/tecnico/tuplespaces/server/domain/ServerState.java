package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.tuplespaces.server.ServerMain;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Creating new ServerState");
    this.tuples = new ArrayList<String>();
  }

  public synchronized void put(String tuple) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Adding tuple: " + tuple);
    tuples.add(tuple);

    // notify all waiting threads
    notifyAll();
  }

  private synchronized String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
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
      tuple = getMatchingTuple(pattern);
    }
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

  public synchronized String take(String pattern) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Taking tuple matching pattern: " + pattern);
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
    tuples.remove(tuple);
    ServerMain.debug(ServerState.class.getSimpleName(), "Removed tuple: " + tuple);
    return tuple;
  }

  public synchronized List<String> getTupleSpacesState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Returning all tuples");
    return this.tuples;
  }
}