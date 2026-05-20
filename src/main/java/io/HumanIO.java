package io;

import java.util.Scanner;

public class HumanIO implements ClientIO {

    private final Scanner scanner;

    public HumanIO(Scanner scanner){
        this.scanner = scanner;
    }

    @Override
    public String readInput() { return scanner.nextLine(); }

    @Override
    public void writeOutput(String s) { IO.println(s); }

}
