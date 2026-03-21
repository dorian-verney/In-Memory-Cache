package commands;

import java.util.Optional;

public class CommandResponse {
    private final boolean success;
    private final String message;
    private final String value;

    private CommandResponse(boolean success, String message, String value) {
        this.success = success;
        this.message = message;
        this.value = value;
    }

    public static CommandResponse success(String result) {
        return new CommandResponse(true, result, null);
    }

    public static CommandResponse success(String result, String value) {
        return new CommandResponse(true, result, value);
    }

    public static CommandResponse error(String message) {
        return new CommandResponse(false, message, null);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    public boolean isSuccess()
    {
        return this.success;
    }

    public String getMessage(){return this.message;}
}