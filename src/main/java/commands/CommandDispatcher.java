package commands;

import cache.CacheStore;
import java.util.HashMap;
import java.util.Map;


public class CommandDispatcher {

    private final Map<String, Command> commands = new HashMap<>();
    private final CacheStore cache;

    public CommandDispatcher(CacheStore cache) {
        this.cache = cache;
        commands.put("ADD", new AddCommand());
        commands.put("GET", new GetCommand());
        commands.put("SET", new SetCommand());
        commands.put("DEL", new DeleteCommand());
    }

    public CommandResponse dispatch(String input) {
        String[] args = input.trim().split("\\s+");
        String commandName = args[0].toUpperCase();

        Command command = commands.get(commandName);
        if (command == null) return CommandResponse.error("ERROR unknown command: " + commandName);

        return command.execute(cache, args);

//        // TODO
//        if (response.isSuccess())
//        {
//            // TODO
//            // response.getValue().ifPresent(val -> sendToClient(val.toString()));
//        }
//
//        // logger, renvoyer l'erreur / sucess au client TCP...
//        return response.getMessage();
    }
}
