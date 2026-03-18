package wakfulib.logic.command;

public interface CommandHandler<C extends CommandIssuer> {

    /**
     * Handle a command logic
     * @param commandIssuer the issuer of the command
     * @param command the arguments of the command, should NOT contain the name of the command itself
     * @return true weather the command execution was successful
     */
    boolean onCommand(C commandIssuer, String[] command);
}
