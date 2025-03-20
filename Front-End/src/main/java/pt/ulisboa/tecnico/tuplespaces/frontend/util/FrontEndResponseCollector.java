package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import java.util.ArrayList;
import java.util.List;

public class FrontEndResponseCollector {
    ArrayList<String> collectedResponses;

    public FrontEndResponseCollector() {
        collectedResponses = new ArrayList<>();
    }

    synchronized public void addString(String s) {
        if (s != null && !s.isEmpty()) {
            collectedResponses.add(s);
            notifyAll();
        }
    }

    synchronized public void addStringList(List<String> list) {
        if (list.isEmpty()) {
            collectedResponses.add("[]");
        }
        else {
            collectedResponses.addAll(list);
        }
        
        notifyAll();
    }

    synchronized public void clear() {
        collectedResponses.clear();
    }

    synchronized public String getString(int n) {
        if (n >= 0 && n < collectedResponses.size()) {
            return collectedResponses.get(n);
        }
        return null;
    }

    synchronized public ArrayList<String> getStringList() {
        return new ArrayList<>(collectedResponses);
    }

    synchronized public void waitUntilAllReceived(int n) {
        try {
            while (collectedResponses.size() < n)
                wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}