package wakfulib.logic.event.registration;

import java.lang.reflect.Method;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.logic.Session;
import wakfulib.logic.event.Event;
import wakfulib.logic.event.EventPriority;
import wakfulib.exception.EventRegistrationFailedException;

@Slf4j
@AllArgsConstructor
public class EventRegistration {

    private final EventCallable[] registrations;

    public void dispatch(@Nullable Session session, @NonNull Event message) {
        for (EventCallable registration : registrations) {
            registration.call(session, message);
        }
    }

    @NoArgsConstructor
    public static class EventRegistrationBuilder {

        private final List<EventCallable> registration = new LinkedList<>();
        public void addListener(@NonNull Object o, @NonNull Method declaredMethod, boolean withSession, EventPriority priority) throws EventRegistrationFailedException {
            try {
                if (priority.isUnique()) {
                    for (EventCallable callable : registration) {
                        if (callable.getPriority() == priority.asInt()) {
                            String newRegistrer = declaredMethod.getDeclaringClass().getName() + '.' + declaredMethod.getName() + "(...) ";
                            var oldCallable = callable.getHandle();
                            String oldRegistrer = oldCallable.toString();
                            throw new EventRegistrationFailedException("Asked to register new event handler:\n\t" + newRegistrer +
                                " with priority " + priority.name() + " but this priority is unique and already use with the event handler: \n\t"
                                + oldRegistrer);
                        }
                    }
                }
                EventCallable toRegister = withSession ?
                    new EventCallableWithSession(priority.asInt(), o, declaredMethod) :
                    new SimpleEventCallable(priority.asInt(), o, declaredMethod);
                if (registration.contains(toRegister)) {
                    log.error("Couldn't register @EventHandler{} as it is already registered!! Not normal, check your shit !", toRegister.getHandle().toString());
                    log.error("Was trying to insert:");
                    log.error("- {} with hash {}", toRegister, toRegister.hashCode());
                    log.error("Clashes with registered @EventHandler :");
                    for (EventCallable callable : registration) {
                        if (Objects.equals(callable, toRegister)) {
                            log.error("- {} with hash {}", callable, callable.hashCode());
                        }
                    }

                    log.error("Registered @EventHandler :");
                    for (EventCallable callable : registration) {
                        log.error("- {} with hash {}", callable, callable.hashCode());
                    }
                } else {
                    registration.add(toRegister);
                }
            } catch (IllegalAccessException e) {
                log.error("Error while registering a @EventHandler", e);
            }
        }

        public EventRegistration build() {
            return new EventRegistration(
                registration.stream()
                    .sorted(Comparator.comparingInt(EventCallable::getPriority))
                    .toArray(EventCallable[]::new));
        }
    }
}
