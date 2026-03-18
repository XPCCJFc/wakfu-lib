package wakfulib.internal.registration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Interface representing an external component that can provide version-dependent registrations.
 * This is used when the default package scanning in {@link VersionRegistry} is disabled.
 */
public interface ExternalRegistration {
    /**
     * Registers components with the provided {@link Registrator}.
     *
     * @param registrator the registrator to use for adding eligible version components
     */
    void register(Registrator registrator);

    /**
     * Helper record for managing eligible components during registration.
     *
     * @param elegibleForVersion a map to store eligible components by their canonical name
     */
    public static final record Registrator(Map<String, InjectionInfo> elegibleForVersion) {
        /**
         * Adds an eligible component to the registration.
         *
         * @param name the name of the component (e.g., class name)
         * @param info the injection information for the component
         */
        public void addEligible(String name, InjectionInfo info) {
            elegibleForVersion.put(name, info);
        }

        /**
         * Adds an eligible component instance to the registration.
         *
         * @param instance the component instance
         */
        public void addEligible(Object instance) {
            addEligible(instance.getClass(), instance);
        }

        /**
         * Adds an eligible component instance to the registration for a specific class.
         *
         * @param clazz the class identifier for the component
         * @param instance the component instance
         */
        public void addEligible(Class<?> clazz, Object instance) {
            elegibleForVersion.put(clazz.getCanonicalName(), new InjectionInfo(instance, clazz.getCanonicalName()));
        }

        /**
         * Adds an eligible component instance to the registration for a specific class, 
         * including a set of fields that require dependency injection.
         *
         * @param clazz the class identifier for the component
         * @param instance the component instance
         * @param fields the set of fields that require dependency injection
         */
        public void addEligibleWithFields(Class<?> clazz, Object instance, Set<Field> fields) {
            elegibleForVersion.put(clazz.getCanonicalName(), InjectionInfo.fromFields(instance, clazz.getCanonicalName(), fields));
        }
    }
}
