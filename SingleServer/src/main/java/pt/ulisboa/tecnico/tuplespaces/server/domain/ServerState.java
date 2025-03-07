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

  public void put(String tuple) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Adding tuple: " + tuple);
    tuples.add(tuple);
  }

  private String getMatchingTuple(String pattern) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Searching for tuple matching pattern: " + pattern);

    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
        ServerMain.debug(ServerState.class.getSimpleName(), "Found tuple matching pattern: " + pattern + " -> " + tuple);
        return tuple;
      }
    }

    ServerMain.debug(ServerState.class.getSimpleName(), "No tuple found matching pattern: " + pattern);
    return null;
  }

  public String read(String pattern) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Reading tuple matching pattern: " + pattern);

    String tuple = getMatchingTuple(pattern);
    while (tuple == null) {
      tuple = getMatchingTuple(pattern);
    }
    return tuple;
  }

  public String take(String pattern) {
    ServerMain.debug(ServerState.class.getSimpleName(), "Taking tuple matching pattern: " + pattern);
    String tuple = getMatchingTuple(pattern);
    while (tuple == null) {
      tuple = getMatchingTuple(pattern);
    }
    tuples.remove(tuple);
    ServerMain.debug(ServerState.class.getSimpleName(), "Removed tuple: " + tuple);
    return tuple;
  }

  public List<String> getTupleSpacesState() {
    ServerMain.debug(ServerState.class.getSimpleName(), "Returning all tuples");
    return this.tuples;
  }
}
