package wakfulib.logic.command;

public interface CommandManager<C extends CommandIssuer, H extends CommandHandler<C>> {

    void register(String commandName, H handler);
}
