package wakfulib.logic.command;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static wakfulib.utils.Empty.EMPTY_STRING_ARRAY;

@Slf4j
public class AbstractCommandManager<C extends CommandIssuer, H extends CommandHandler<C>> implements CommandManager<C, H> {
    protected final Map<String, H> commands;

    public AbstractCommandManager() {
        commands = new HashMap<>();
    }

    public void register(@NotNull String commandName, @NotNull H handler) {
        var oldCommand = commands.put(commandName, handler);
        if (oldCommand != null) {
            log.warn("< register(): Duplicated command alias ! ({} - {})", handler.getClass().getSimpleName(), oldCommand.getClass().getSimpleName());
        }
    }

    @Nullable
    public H getCommand(@NonNull String commandName) {
        return commands.get(commandName);
    }

    @NotNull
    protected String[] keepOnlyArguments(@NotNull String[] commandLine) {
        if (commandLine.length == 1) {
            return EMPTY_STRING_ARRAY;
        }
        return Arrays.copyOfRange(commandLine, 1, commandLine.length);
    }
}
