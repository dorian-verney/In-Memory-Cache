package commands;

import org.apache.commons.cli.ParseException;

public interface AdvancedCommand extends Command {
    String[] parseCmd(String[] args) throws ParseException;
}
