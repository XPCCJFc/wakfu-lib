package wakfulib.logic.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EventPriority {
    FIRST(Integer.MIN_VALUE, true),
    HIGH(-1, false),
    NORMAL(0, false),
    LOW(1, false),
    LATEST(Integer.MAX_VALUE, true);

    private final int priority;
    @Getter
    private final boolean unique;

    public int asInt() {
        return priority;
    }
}
