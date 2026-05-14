package commands;

import cache.CacheStore;

import java.util.HashMap;
import java.util.Map;


public class CommandDispatcher {

    private final Map<String, Command> commands = new HashMap<>();
    private final CacheStore cache;

    public CommandDispatcher(CacheStore cache) {
        this.cache = cache;
        commands.put("SET", new SetCommand());
        commands.put("GET", new GetCommand());
        commands.put("DEL", new DeleteCommand());
        commands.put("TTL", new TTLCommand());
        commands.put("EXPIRE", new ExpireCommand());
    }

    public CommandResponse dispatch(String input) {
        String[] args = input.trim().split("\\s+");
        String commandName = args[0].toUpperCase();

        Command command = commands.get(commandName);
        if (command == null) return CommandResponse.error("ERROR unknown command: " + commandName);

        return command.execute(cache, args);
    }
}
