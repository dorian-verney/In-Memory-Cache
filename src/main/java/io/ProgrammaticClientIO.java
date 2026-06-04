package io;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class ProgrammaticClientIO implements ClientIO {

    private final Queue<String> commands;

    private final Consumer<String> outputConsumer;

    public ProgrammaticClientIO(List<String> commands, Consumer<String> outputConsumer) {
        this.commands = new LinkedList<>(commands);
        this.outputConsumer = outputConsumer;
    }

    public ProgrammaticClientIO(List<String> commands) {
        this(commands, s -> {});
    }

    @Override
    public String readInput() { return commands.poll(); }

    @Override
    public void writeOutput(String s) { outputConsumer.accept(s); }

}
