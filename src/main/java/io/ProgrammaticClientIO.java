package io;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ProgrammaticClientIO implements ClientIO {

    private final Queue<String> commands = new LinkedList<>();

    private final List<String> responses = new ArrayList<>();

    @Override
    public String readInput() { return commands.poll(); }

    @Override
    public void writeOutput(String s) { responses.add(s); }

    public List<String> getResponses() { return responses; }

}
