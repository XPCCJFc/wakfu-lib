package wakfulib.logic.internal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.logic.event.EventManager;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Responsible for the asynchronous dispatching of network messages and internal events.
 * It maintains a queue of {@link Dispatchable} tasks and processes them sequentially in a dedicated thread.
 */
@Slf4j
public class MessageDispatcher implements Runnable {
    /**
     * The queue containing pending messages and events to be dispatched.
     */
    @Getter
    public final TransferQueue<Dispatchable> messageQueue;
    private final EventManager eventManager;

    private static MessageDispatcher INSTANCE;

    /**
     * Creates a new MessageDispatcher associated with the specified event manager.
     *
     * @param eventManager The event manager to use for dispatching.
     */
    public MessageDispatcher(EventManager eventManager) {
        this.eventManager = eventManager;
        this.messageQueue = new LinkedTransferQueue<>();
        INSTANCE = this;
    }

    /**
     * Returns the singleton instance of the MessageDispatcher.
     *
     * @return The current dispatcher instance.
     */
    public static MessageDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * The main processing loop of the dispatcher.
     * Continuously takes tasks from the queue and dispatches them until a poison pill is received.
     */
    @Override
    public void run() {
        while (true) {
            Dispatchable dispatchable = null;
            try {
                dispatchable = messageQueue.take();
                if (dispatchable.isPoisonPill()) {
                    log.warn("< run(): Poison pill eaten, goodbye !");
                    return;
                } else {
                    handle(dispatchable);
                }
            } catch (InterruptedException e) {
                log.error("- run(): Interrupted !", e);
            } finally {
                if (dispatchable != null) {
                    dispatchable.release();
                }
            }
        }
    }

    /**
     * Dispatches a single task using the associated event manager.
     *
     * @param dispatchable The task to handle.
     */
    protected void handle(Dispatchable dispatchable) {
        dispatchable.selfDispatch(eventManager);
    }

    /**
     * Starts the dispatcher in a new daemon thread.
     *
     * @return The newly created and started thread.
     */
    public Thread runAsDeamon() {
        Thread messageDispatcher = new Thread(this, "MessDispa");
        messageDispatcher.start();
        return messageDispatcher;
    }
}
