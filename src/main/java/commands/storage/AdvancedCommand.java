package commands.storage;

import org.apache.commons.cli.ParseException;

public interface AdvancedCommand extends BasicCommand {
    String[] parseCmd(String[] args) throws ParseException;
}
