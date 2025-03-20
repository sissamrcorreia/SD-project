package pt.ulisboa.tecnico.tuplespaces.server.util;

public class TupleEntry {
    private String tuple;
    private boolean isLocked;
    private int lockedByClientID;

    public TupleEntry(String tuple) {
        this.tuple = tuple;
        this.isLocked = false;
        this.lockedByClientID = -1; // not-set clientId
    }

    public String getTuple() {
        return tuple;
    }

    public void setTuple(String tuple) {
        this.tuple = tuple;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getLockedByClientID() {
        return lockedByClientID;
    }

    public void setLockedByClientID(int lockedByClientID) {
        this.lockedByClientID = lockedByClientID;
    }
}
