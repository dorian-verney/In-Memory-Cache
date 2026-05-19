package commands.storage;

import cache.CacheStore;
import commands.CommandResponse;
import org.apache.commons.cli.*;

public class SetCommand implements AdvancedCommand {

    private static final int MIN_ARGS = 3;

    @Override
    public String[] parseCmd(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("EX", "EXPIRATION", true, "set expiration in seconds");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);
        String[] cmd = new String[1];
        String res = cmdLine.getOptionValue("EX");
        cmd[0] = res;
        return cmd;
    }

    private long parseTTL(String[] args) throws ParseException {
        String[] options = parseCmd(args);
        if (options[0] == null) return -1;
        long ttl = Long.parseLong(options[0]);
        if (ttl <= 0) throw new IllegalArgumentException("EX must be a positive integer");
        return ttl;
    }


    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. SET key value)");

        long ttl;
        try {
            ttl = parseTTL(args);
        } catch (ParseException | IllegalArgumentException e) {
            return CommandResponse.error("ERROR invalid args: " + e.getMessage());
        }

        try {
            cache.set(args[1], args[2], ttl);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success(args[0]);
    }
}
