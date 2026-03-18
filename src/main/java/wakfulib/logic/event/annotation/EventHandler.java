package wakfulib.logic.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import wakfulib.logic.event.EventPriority;

/**
 * Annotation used to mark methods that handle network messages or internal events.
 * Annotated methods must be part of a class registered with an {@link wakfulib.logic.event.EventManager}.
 * <p>
 * The method signature should typically accept the event/message as a single parameter,
 * or the session and the event/message as two parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * Defines the priority of this handler.
     * Handlers with higher priority are executed first.
     *
     * @return The priority level.
     */
    EventPriority priority() default EventPriority.NORMAL;
}
