package pubsub;

public record Message(String channel, String payload) {

    @Override
    public String toString(){
        return "Channel: %s, Message: %s".formatted(channel, payload);
    }
}
