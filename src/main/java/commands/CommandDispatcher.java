package commands;

import cache.CacheStore;
import commands.pubsub.*;
import commands.storage.*;
import pubsub.PubSubBroker;

import java.util.HashMap;
import java.util.Map;


public class CommandDispatcher {

    private final Map<String, BasicCommand> commands = new HashMap<>();
    private final Map<String, PubSubCommand> pubSubCommands = new HashMap<>();
    private final CacheStore cache;
    private final PubSubBroker broker;

    public CommandDispatcher(CacheStore cache, PubSubBroker broker) {
        this.cache  = cache;
        this.broker = broker;

        commands.put("SET", new SetCommand());
        commands.put("GET", new GetCommand());
        commands.put("DEL", new DeleteCommand());
        commands.put("TTL", new TTLCommand());
        commands.put("EXPIRE", new ExpireCommand());

        pubSubCommands.put("PUB", new PublishCommand());
        pubSubCommands.put("SUB", new SubscribeCommand());
        pubSubCommands.put("UNSUB", new UnSubscribeCommand());
        pubSubCommands.put("PUBSUB NUMSUB", new NumSubCommand());
        pubSubCommands.put("PUBSUB CHANNELS", new ChannelCommand());
    }

    public CommandResponse dispatch(String input){
        String[] args = input.trim().split("\\s+");
        String commandName = args[0].toUpperCase();

        BasicCommand cmd = commands.get(commandName);
        if (cmd != null) return cmd.execute(cache, args);

        if (commandName.equals("PUBSUB")){
            commandName = args[0].toUpperCase() + " " + args[1].toUpperCase();
        }

        PubSubCommand psCmd = pubSubCommands.get(commandName);
        if (psCmd != null) return psCmd.execute(broker, args);

        return CommandResponse.error("ERROR unknown command: " + commandName);
    }

    public PubSubBroker getBroker() {
        return broker;
    }
}
