package wakfulib.internal.registration;

/**
 * An external registration implementation that parses a class to find version-specific implementations.
 * It uses the {@link MessageRegistrationStrategy#PARSE_CLASS_STRATEGY} to identify and register
 * eligible internal classes (e.g., serializers or message implementations).
 */
public class ClassParsingRegistration implements ExternalRegistration {
    private final Class<?> clazz;

    /**
     * Creates a new registration for the specified class.
     *
     * @param clazz the class to parse for version-dependent implementations
     */
    public ClassParsingRegistration(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void register(Registrator registrator) {
        MessageRegistrationStrategy.PARSE_CLASS_STRATEGY.parseInternalClass(registrator, clazz.getSuperclass(), clazz);
    }
}
