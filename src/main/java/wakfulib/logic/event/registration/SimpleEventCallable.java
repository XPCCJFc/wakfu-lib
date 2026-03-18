package wakfulib.logic.event.registration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.logic.Session;
import wakfulib.logic.event.Event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
class SimpleEventCallable implements EventCallable {

    @Getter
    private final int priority;
    private final Object target;
    @Getter
    private final MethodHandle handle;

    public SimpleEventCallable(int priority, @NonNull Object target, @NonNull Method declaredMethod) throws IllegalAccessException {
        this.target = target;
        this.priority = priority;
        var lookup = MethodHandles.lookup();
        handle = lookup.unreflect(declaredMethod);
    }

    public void call(Session session, @NonNull Event message) {
        try {
            handle.invokeWithArguments(target, message);
        } catch (Throwable e) {
            log.error("Error while invoking @EventHandler '{}'", handle, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleEventCallable that = (SimpleEventCallable) o;

        if (target != that.target) return false;
        return Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        int result = target != null ? target.hashCode() : 0;
        result = 31 * result + (handle != null ? handle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleEventCallable{" +
            "priority=" + priority +
            ", target=" + target.getClass().getSimpleName() +
            ", handle=" + handle +
            '}';
    }
}
