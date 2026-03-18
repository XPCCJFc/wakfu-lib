package wakfulib.internal.registration;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import wakfulib.internal.Inject;
import wakfulib.internal.Version;

/**
 * Strategy interface for identifying and registering version-dependent message implementations.
 */
public interface MessageRegistrationStrategy {
    /**
     * Strategy that parses a Java class to find its static inner classes 
     * that match the current version.
     */
    @Slf4j
    class ParseClassStrategy implements MessageRegistrationStrategy {

        private ParseClassStrategy() {
        }

        /**
         * Parses the given message class to find eligible inner implementations.
         *
         * @param registrator the registrator to use for adding eligible components
         * @param message the message base class to parse
         */
        public void parse(ExternalRegistration.Registrator registrator, Class<?> message) {
            Class<?>[] serials = message.getDeclaredClasses();
            for (Class<?> serial : serials) {
                parseInternalClass(registrator, message, serial);
            }
        }

        /**
         * Parses a specific internal class to see if it's an implementation for the current version.
         *
         * @param registrator the registrator to use for adding eligible components
         * @param message the superclass/interface this implementation corresponds to
         * @param messageImpl the actual implementation class to evaluate
         */
        public void parseInternalClass(ExternalRegistration.Registrator registrator, Class<?> message, Class<?> messageImpl) {
            if (! Modifier.isStatic(messageImpl.getModifiers())) {
                log.warn(messageImpl.getSimpleName() + " is not static, skipped !");
                return;
            }
            var annotation = Version.getRangeForCurrentVersion(messageImpl);
            if (annotation != null && Version.getCurrent().isInRange(annotation)) {
                Object messageImplInstance = null;
                try {
                    Constructor<?> messageImplDeclaredConstructor = messageImpl.getDeclaredConstructor();
                    messageImplDeclaredConstructor.setAccessible(true);
                    messageImplInstance = messageImplDeclaredConstructor.newInstance();
                } catch (Exception e) {
                    log.error("Cannot instantiate class {}", messageImpl.getSimpleName(), e);
                    System.exit(-1);
                }
                registrator.addEligibleWithFields(message, messageImplInstance, Arrays.stream(messageImpl.getDeclaredFields())
                    .filter(f -> f.getAnnotation(Inject.class) != null)
                    .collect(Collectors.toSet()));
            }
        }
    }

    /**
     * The default instance for the class parsing strategy.
     */
    ParseClassStrategy PARSE_CLASS_STRATEGY = new ParseClassStrategy();

    /**
     * Strategy that would parse scripts (e.g., Lua, JS) to define message implementations.
     * (Currently not fully implemented).
     */
    class ParseScriptStrategy implements MessageRegistrationStrategy {

        private ParseScriptStrategy() {
        }

        /**
         * Parses a script file to find message implementations.
         *
         * @param res a map to store the identified injection info
         * @param scriptFile the script file to parse
         */
        public void parse(Map<String, InjectionInfo> res, File scriptFile) {
            //TODO implements script reader
        }

        /**
         * Evaluates a class to see if it matches the current version and registers it.
         *
         * @param res a map to store the identified injection info
         * @param message the superclass/interface this implementation corresponds to
         * @param messageImpl the actual implementation class to evaluate
         */
        public void parseInternalClass(Map<String, InjectionInfo> res, Class<?> message, Class<?> messageImpl) {
            if (! Modifier.isStatic(messageImpl.getModifiers())) {
                System.err.println(messageImpl.getSimpleName() + " is not static, skipped !");
            }
            var annotation = Version.getRangeForCurrentVersion(messageImpl);
            if (annotation != null && Version.getCurrent().isInRange(annotation)) {
                Object instance = null;
                try {
                    Constructor<?> declaredConstructor = messageImpl.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    instance = declaredConstructor.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                res.put(message.getSimpleName(), InjectionInfo.fromFields(instance, message.getCanonicalName(),
                    Arrays.stream(messageImpl.getDeclaredFields())
                        .filter(f -> f.getAnnotation(Inject.class) != null)
                        .collect(Collectors.toSet())));
            }
        }
    }

    /**
     * The default instance for the script parsing strategy.
     */
    ParseScriptStrategy PARSE_SCRIPT_STRATEGY = new ParseScriptStrategy();
}
