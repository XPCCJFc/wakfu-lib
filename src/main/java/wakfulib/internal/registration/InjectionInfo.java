package wakfulib.internal.registration;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds information about an eligible component for the current version and its
 * dependency injection requirements.
 *
 * @param serializer the component instance being registered (e.g., a message implementation)
 * @param superName the canonical name of the superclass or interface that this component implements
 * @param toInject the set of fields that require dependency injection
 */
@Slf4j
public record InjectionInfo(Object serializer, String superName, Set<InjectedState> toInject) {

    /**
     * Constructs a new InjectionInfo.
     *
     * @param serializer the component instance being registered
     * @param superName the canonical name of the component identifier
     * @param toInject the set of fields requiring injection
     */
    public InjectionInfo(Object serializer, String superName, Set<InjectedState> toInject) {
        this.serializer = serializer;
        this.superName = superName;
        this.toInject = Collections.unmodifiableSet(toInject);
    }

    /**
     * Constructs a new InjectionInfo with no fields to inject.
     *
     * @param serializer the component instance being registered
     * @param superName the canonical name of the component identifier
     */
    public InjectionInfo(Object serializer, String superName) {
        this(serializer, superName, Collections.emptySet());
    }

    /**
     * Creates a new InjectionInfo from a set of reflective {@link Field} objects.
     *
     * @param serializer the component instance being registered
     * @param superName the canonical name of the component identifier
     * @param toInject the set of fields requiring injection
     * @return a new InjectionInfo instance
     */
    public static InjectionInfo fromFields(Object serializer, String superName, Set<Field> toInject) {
        return new InjectionInfo(serializer, superName, toInject.stream().map(InjectedState :: new).collect(Collectors.toSet()));
    }

    /**
     * Attempts to resolve any pending dependencies for this component using the
     * provided map of registered objects.
     *
     * @param registeredClass a map of already resolved and registered objects by their class name
     * @return {@code true} if any dependency was resolved, {@code false} otherwise
     */
    public boolean resolveAny(Map<String, Object> registeredClass) {
        boolean returnValue = false;
        for (InjectedState injectedState : toInject) {
            if (! injectedState.state) {
                if (registeredClass.containsKey(injectedState.fieldParam.getType().getCanonicalName())) {
                    Object toInject = registeredClass.get(injectedState.fieldParam.getType().getCanonicalName());
                    try {
                        injectedState.fieldParam.set(serializer, toInject);
                    } catch (Exception e) {
                        log.error("Cannot inject dependency {} in field {}", toInject, injectedState.fieldParam, e);
                        System.exit(-1);
                    }
                    injectedState.state = true;
                    returnValue = true;
                }
            }
        }
        return returnValue;
    }

    /**
     * Checks if there are still any unresolved dependencies for this component.
     *
     * @return {@code true} if at least one dependency is not resolved, {@code false} otherwise
     */
    public boolean notResolved() {
        return toInject.stream().anyMatch(f -> ! f.state);
    }

    /**
     * Appends descriptions of unresolved dependencies to a {@link StringBuilder}.
     *
     * @param sb the StringBuilder to append to
     */
    public void notResolved(StringBuilder sb) {
        toInject.stream()
            .filter(f -> ! f.state)
            .forEach(f -> sb.append("\t").append(f.fieldParam).append(System.lineSeparator()));
    }

    /**
     * Internal state for tracking dependency injection for a single field.
     */
    public final static class InjectedState {

        private final Field fieldParam;
        private boolean state;

        /**
         * Creates a new InjectedState for the given field.
         *
         * @param t the field that requires dependency injection
         */
        public InjectedState(Field t) {
            this.fieldParam = t;
            this.fieldParam.setAccessible(true);
        }

        public int hashCode() {
            return fieldParam.getType().getSimpleName().hashCode();
        }
    }
}
