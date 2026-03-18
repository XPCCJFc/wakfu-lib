package wakfulib.logic.event;

/**
 * Marker interface for events generated internally by the library,
 * such as connection status changes, as opposed to events decoded from network messages.
 */
public interface InternalEvent extends Event {
}
