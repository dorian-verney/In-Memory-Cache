package commands;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CommandResponse {

    private final ResponseType responseType;

    private final boolean success;
    private final String message;
    private final String value;

    private CommandResponse(ResponseType responseType, boolean success, String message, String value) {
        this.responseType = responseType;
        this.success = success;
        this.message = message;
        this.value = value;
    }


    public static CommandResponse success(String result) {
        return new CommandResponse(ResponseType.NORMAL, true, result, null);
    }

    public static CommandResponse success(String result, String value) {
        return new CommandResponse(ResponseType.NORMAL,true, result, value);
    }

    public static CommandResponse success(HashMap<String, Integer> map) {
        return new CommandResponse(ResponseType.NORMAL,true, map.toString(), null);
    }

    public static CommandResponse error(String message) {
        return new CommandResponse(ResponseType.NORMAL, false, message, null);
    }

    public static CommandResponse subscribe(List<String> channels, int numChannelSub){
        return new CommandResponse(ResponseType.SUBSCRIBE_MODE, false,
                String.join(" ", channels), String.valueOf(numChannelSub));
    }

    public static CommandResponse unsubscribe(List<String> channels, int numChannelSub){
        return new CommandResponse(ResponseType.UNSUBSCRIBE_MODE, false,
                String.join(" ", channels), String.valueOf(numChannelSub));
    }

    public ResponseType getResponseType(){
        return responseType;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage(){return this.message;}
}