package wakfulib.logic.event;

import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.Inject;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.logic.Session;
import wakfulib.logic.event.annotation.EventHandler;
import wakfulib.logic.event.registration.EventRegistration;
import wakfulib.utils.data.Tuple;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static wakfulib.utils.ReflectionUtils.getAllFields;

/**
 * Manages event registration and dispatching for network messages and internal events.
 * Use {@link EventManagerBuilder} to register listeners and build an instance.
 */
@Slf4j
public class EventManager {

    /**
     * Builder for {@link EventManager}.
     * Provides methods to register listener objects containing {@link EventHandler} annotated methods.
     */
    public static class EventManagerBuilder {

        public final Map<Integer, EventRegistration.EventRegistrationBuilder> messageRegistration;
        public final Map<String, EventRegistration.EventRegistrationBuilder> eventRegistration;

        private final Map<Object, Field> sessionFields;

        /**
         * Creates a new EventManagerBuilder.
         */
        public EventManagerBuilder() {
            this.messageRegistration = new HashMap<>();
            this.eventRegistration = new HashMap<>();
            this.sessionFields = new HashMap<>();
        }

        /**
         * Registers a listener object.
         * Methods in the listener object annotated with {@link EventHandler} will be registered to handle
         * network messages or internal events based on their parameter types.
         * <p>
         * If the listener has fields of type {@link Session} annotated with {@link Inject}, 
         * they will be automatically populated with the active session when it becomes available.
         *
         * @param o The listener object containing event handler methods.
         * @return This builder instance for chaining.
         */
        public EventManagerBuilder register(@NonNull Object o) {
            for (Field declaredField : getAllFields(o.getClass())) {
                if (Session.class.isAssignableFrom(declaredField.getType()))
                    if (declaredField.getAnnotation(Inject.class) != null) {
                        sessionFields.put(o, declaredField);
                        log.warn("- register({}): Session field injected, DO NOT use this feature in server mode", o.getClass().getSimpleName());
                    } else {
                        log.warn("- register({}): Session field '{}' is not annotated with @Inject and will not be injected, is this normal ?", o.getClass().getSimpleName(), declaredField);
                    }
            }
            Method[] methods = o.getClass().getMethods();
            for (Method declaredMethod : methods) {
                EventHandler annotation = declaredMethod.getAnnotation(EventHandler.class);
                if (annotation != null) {
                    Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                    int eventPosition;
                    if (parameterTypes.length == 1) {
                        eventPosition = 0;
                    } else if (parameterTypes.length == 2 && Session.class.isAssignableFrom(parameterTypes[0])) {
                        eventPosition = 1;
                    } else {
                        log.error("- register({}): Invalid signature for event handler " + declaredMethod.getName(), o.getClass().getSimpleName());
                        continue;
                    }
                    Class<?> eventType = parameterTypes[eventPosition];
                    if (InternalEvent.class.isAssignableFrom(eventType)) {
                        EventRegistration.EventRegistrationBuilder reg = this.eventRegistration.get(eventType.getName());
                        if (reg == null) {
                            reg = new EventRegistration.EventRegistrationBuilder();
                            eventRegistration.put(eventType.getName(), reg);
                        }
                        declaredMethod.setAccessible(true);
                        reg.addListener(o, declaredMethod, eventPosition == 1, annotation.priority());
                    } else if (Message.class.isAssignableFrom(eventType)) {
                        Message message = (Message) VersionRegistry.get(eventType);
                        int opCode = message.getOpCode();
                        Map<Integer, EventRegistration.EventRegistrationBuilder> registrationMap = this.messageRegistration;
                        EventRegistration.EventRegistrationBuilder eventRegistration = registrationMap.get(opCode);
                        if (eventRegistration == null) {
                            eventRegistration = new EventRegistration.EventRegistrationBuilder();
                            registrationMap.put(opCode, eventRegistration);
                        }
                        declaredMethod.setAccessible(true);
                        eventRegistration.addListener(o, declaredMethod, eventPosition == 1, annotation.priority());
                    }
                }
            }
            return this;
        }

        /**
         * Builds a new {@link EventManager} instance containing all registered listeners.
         *
         * @return A configured EventManager instance.
         */
        public EventManager build() {
            return buildInto(null);
        }

        /**
         * Builds the registered listeners into an existing {@link EventManager} or creates a new one.
         * If a target is provided, its existing registrations will be replaced.
         *
         * @param target An existing EventManager to update, or {@code null} to create a new one.
         * @return The updated or newly created EventManager instance.
         */
        public EventManager buildInto(@Nullable EventManager target) {
            EventManager eventManager;
            if (target == null) {
                eventManager = new EventManager();
            } else {
                eventManager = target;
                eventManager.sessionFields.clear();
                eventManager.messageRegistration.clear();
                eventManager.eventRegistration.clear();
            }

            messageRegistration.entrySet().stream().map(e -> new Tuple<>(e.getKey(), e.getValue().build()))
                .forEach(mr -> eventManager.messageRegistration.put(mr._1, mr._2));
            eventRegistration.entrySet().stream().map(e -> new Tuple<>(e.getKey(), e.getValue().build()))
                .forEach(mr -> eventManager.eventRegistration.put(mr._1, mr._2));
            eventManager.sessionFields.putAll(sessionFields);

            eventRegistration.clear();
            messageRegistration.clear();
            sessionFields.clear();
            return eventManager;
        }
    }

    public final Map<Integer, EventRegistration> messageRegistration;
    public final Map<String, EventRegistration> eventRegistration;

    private final Map<Object, Field> sessionFields;

    public EventManager() {
        this.messageRegistration = new HashMap<>();
        this.eventRegistration = new HashMap<>();
        this.sessionFields = new HashMap<>();
    }

    /**
     * Injects the active session into registered listener objects' fields.
     *
     * @param session The active session.
     */
    public void setSessionFields(Session session) {
        sessionFields.forEach((o, field) -> {
            try {
                field.setAccessible(true);
                field.set(o, session);
            } catch (IllegalAccessException e) {
                log.error("Error while setting a session field in class {}", o.getClass().getSimpleName(), e);
            }
        });
    }

    /**
     * Dispatches a network message to registered listeners.
     *
     * @param message The message to dispatch.
     * @param session The session associated with the message.
     */
    public void dispatch(@Nullable Message message, @Nullable Session session) {
        if (message == null) {
            log.warn("<> dispatch(): Trying to dispatch a null message");
            return;
        }
        EventRegistration eventRegistration = messageRegistration.get(message.getOpCode());
        if (eventRegistration != null) {
            eventRegistration.dispatch(session, message);
        } else {
            log.warn("<> dispatch(): No handler for message {}", message.getOpCode());
        }
    }

    /**
     * Dispatches an internal event to registered listeners.
     *
     * @param event The internal event to dispatch.
     * @param session The session associated with the event.
     */
    public void dispatch(@NonNull InternalEvent event, @Nullable Session session) {
        var eventName = event.getClass().getName();
        EventRegistration registration = eventRegistration.get(eventName);
        if (registration != null) {
            registration.dispatch(session, event);
        } else {
            log.warn("<> dispatch(): No handler for internal event {}: {}", eventName, event);
        }
    }
}
