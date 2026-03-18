package wakfulib.logic.command;

import lombok.AllArgsConstructor;

import java.util.Scanner;
import java.util.function.Supplier;

@AllArgsConstructor
public class DefaultCommandManager<C extends CommandIssuer, H extends CommandHandler<C>> extends AbstractCommandManager<C, H> {

    private final Supplier<C> consoleIssuerProvider;
    public void start() {
        new Thread(() -> {
            var consoleIssuer = consoleIssuerProvider.get();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String next = scanner.nextLine();
                String[] command = next.split(" ");
                if (command.length > 0 && command[0].trim().length() > 0) {
                    var commandHandler = commands.get(command[0]);
                    if (commandHandler != null) {
                        commandHandler.onCommand(consoleIssuer, keepOnlyArguments(command));
                    } else {
                        consoleIssuer.error("Unrecognized command !");
                    }
                }
            }
        }).start();
    }

}
