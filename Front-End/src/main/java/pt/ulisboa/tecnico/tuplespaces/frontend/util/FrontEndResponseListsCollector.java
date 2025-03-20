package pt.ulisboa.tecnico.tuplespaces.frontend.util;

import java.util.ArrayList;
import java.util.List;

public class FrontEndResponseListsCollector {
    ArrayList<List<String>> collectedResponses;

    public FrontEndResponseListsCollector() {
        collectedResponses = new ArrayList<>();
    }

    synchronized public void addStringList(List<String> list) {
        collectedResponses.add(list);
        notifyAll();
    }

    synchronized public void clear() {
        collectedResponses.clear();
    }

    synchronized public List<String> getList(int n) {
        return collectedResponses.get(n);
    }

    synchronized public List<List<String>> getAll() {
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

    synchronized public int getSize() {
        return collectedResponses.size();
    }
}