package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import java.util.ArrayList;
import java.util.List;

public class FrontEndResponseCollector {
    ArrayList<String> collectedResponses;

    public FrontEndResponseCollector() {
        collectedResponses = new ArrayList<>();
    }

    synchronized public void addString(String s) {
        collectedResponses.add(s);
        notifyAll();
    }

    synchronized public void addStringList(List<String> list) {
        if (list.isEmpty())
            collectedResponses.add("[]");
        else
            collectedResponses.addAll(list);
        notifyAll();
    }

    synchronized public void clear() {
        collectedResponses.clear();
    }

    synchronized public String getString(int n) {
        return collectedResponses.get(n);
    }

    synchronized public ArrayList<String> getStringList() {
        return collectedResponses;
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