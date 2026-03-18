package wakfulib.internal.registration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.ArchTarget;
import wakfulib.internal.DuplicateOpCode;
import wakfulib.internal.Version;
import wakfulib.internal.versionable.PacketFactory;
import wakfulib.internal.versionable.protocol.Message;
import wakfulib.internal.versionable.protocol.ToClientMessage;
import wakfulib.internal.versionable.protocol.ToServerMessage;
import wakfulib.exception.NotImplementedException;
import wakfulib.ui.proxy.model.mapping.Mapping;
import wakfulib.utils.data.Tuple;

import static wakfulib.internal.versionable.protocol.Message.UNKNOWN_OPCODE;
import static wakfulib.internal.versionable.protocol.ToServerMessage.UNKNOWN_ARCH_TARGET;

/**
 * The central registry for version-dependent components and protocol messages.
 * It manages the identification, instantiation, and dependency injection of classes
 * based on the current {@link Version}.
 * <p>
 * The registry can be initialized either by scanning packages for {@link VersionDependant}
 * annotations or by manually registering external components.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionRegistry {

    /**
     * Flag to prevent the registry from recursively resolving deep dependencies.
     * When set to {@code true}, only one level of dependency injection is performed.
     */
    public static boolean NO_DEEP_RESOLVE_FLAG = false;

    /**
     * Flag indicating whether the default class parsing strategy should be used 
     * during package scanning.
     */
    public static boolean USE_DEFAULT_PARSE_CLASS_STRATEGY = true;

    /**
     * Initializes the registry without package scanning, using only provided external implementations.
     *
     * @param externalRegisteredImpls the external registration components to use
     * @throws IllegalStateException if no external implementations are provided
     */
    public static void noScan(ExternalRegistration... externalRegisteredImpls) {
        noScan(true, externalRegisteredImpls);
    }

    /**
     * Initializes the registry without package scanning, with optional deep dependency resolution control.
     *
     * @param noDeepResolve whether to disable deep dependency resolution
     * @param externalRegisteredImpls the external registration components to use
     * @throws IllegalStateException if no external implementations are provided
     */
    public static void noScan(boolean noDeepResolve, ExternalRegistration... externalRegisteredImpls) {
        VersionRegistry.externalRegisteredImpl.addAll(Arrays.asList(externalRegisteredImpls));
        USE_DEFAULT_PARSE_CLASS_STRATEGY = false;
        NO_DEEP_RESOLVE_FLAG = noDeepResolve;
        log.info("- noScan(): init with no package scanning");
        if (externalRegisteredImpl.isEmpty()) {
            throw new IllegalStateException("You must register at least one class with the noScan() method");
        }

        var beforeScan = registeredClass.size();
        configure();
        var nbrNewRegistrations = registeredClass.size() - beforeScan;
        if (nbrNewRegistrations == 0) {
            log.warn("< noScan(): Registered 0 version dependant classes with version ({}) !", Version.getCurrent());
        } else {
            log.info("< noScan(): Registered {} version dependant classes with version ({})", nbrNewRegistrations, Version.getCurrent());
        }
    }

    /**
     * Initializes the registry by scanning specified packages for {@link VersionDependant} classes.
     *
     * @param packagesToScan the package names to scan recursively
     * @throws IllegalArgumentException if no packages are specified
     */
    public static void scan(String... packagesToScan) {
        if (packagesToScan == null || packagesToScan.length == 0) {
            throw new IllegalArgumentException("You must specify at least one package");
        } else {
            if (log.isInfoEnabled()) {
                log.info("- scan(): init with package scanning '{}'", Arrays.asList(packagesToScan));
            }
        }
        USE_DEFAULT_PARSE_CLASS_STRATEGY = true;
        var beforeScan = registeredClass.size();
        configure(packagesToScan);
        var nbrNewRegistrations = registeredClass.size() - beforeScan;
        if (nbrNewRegistrations == 0) {
            log.warn("< scan(): Registered 0 version dependant classes with version ({}) !", Version.getCurrent());
        } else {
            log.info("< scan(): Registered {} version dependant classes with version ({}) for the packages ({})", nbrNewRegistrations, Version.getCurrent(), Arrays.asList(packagesToScan));
        }
    }

    private static final List<ExternalRegistration> externalRegisteredImpl = new ArrayList<>();

    /**
     * Adds an external class for registration that will be parsed using the default strategy.
     *
     * @param clazz the class to add
     */
    public static void addExternalImpl(Class<?> clazz) {
        externalRegisteredImpl.add(new ClassParsingRegistration(clazz));
    }

    private static final Map<String, Object> registeredClass = new ConcurrentHashMap<>();

    /**
     * Returns all singleton model instances registered for the current version.
     *
     * @return a collection of registered component instances
     */
    @NonNull
    public static Collection<Object> registeredClasses() {
      return registeredClass.values();
    }

    /**
     * Get the model for the provided versioned class by its name.
     * If you want a modifiable instance, use {@link #newInstance(Class)} instead.
     *
     * @param fullName the same of the versioned class <b>(should be the canonicalName of the class)</b>
     * @return the model for the versioned class
     * @throws NotImplementedException if the class is not implemented in this version
     */
    @NonNull
    public static <T> T get(@NonNull String fullName) throws NotImplementedException {
        var res = registeredClass.get(fullName);
        if (res == null) throw NotImplementedException.noSuffix("No class '" + fullName + "' registered for this version (" + Version.getCurrent() + ")!");
        return (T) res;
    }

    /**
     * Get the model for the provided versioned class by its type.
     * If you want a modifiable instance, use {@link #newInstance(Class)} instead.
     *
     * @param clazz the type of the requested class
     * @return the model for the versioned class
     * @throws NotImplementedException if the class is not implemented in this version
     */
    @NonNull
    public static <T> T get(@NonNull Class<T> clazz) throws NotImplementedException {
        return get(clazz.getCanonicalName());
    }

    /**
     * Get a new instance of a versionable class <b>(Without DI !)</b>
     * @param clazz the type of the class that you want
     * @return a new instance of the class
     * @throws IllegalArgumentException if the class has no default constructor
     * @throws NotImplementedException if the class is not implemented in this version
     */
    @NonNull
    public static <T> T newInstance(@NonNull Class<T> clazz) throws NotImplementedException, IllegalArgumentException {
        return (T) newInstanceOfClazz(get(clazz).getClass());
    }

    /**
     * Get a new instance of a class
     * @param clazz the type of the class that you want
     * @return a new instance of the class
     * @throws IllegalArgumentException if the class has no default constructor
     */
    @NonNull
    private static <T> T newInstanceOfClazz(@NonNull Class<T> clazz) throws IllegalArgumentException {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("No default constructor on class '" + clazz.getName() + '\'');
        }
    }

    /**
     * Get a new instance of a message <b>(Without DI !)</b>
     * @param toProduce the type of message that you want
     * @return a new message instance
     * @throws NotImplementedException if the packet is not found for the current version
     */
    @NonNull
    public static <T extends Message<T>> T messageInstance(Class<T> toProduce) throws NotImplementedException {
        T savedInstance = get(toProduce);
        T res = newInstanceOfClazz((Class<T>) savedInstance.getClass());
        //noinspection deprecation
        res.setOpCode(savedInstance.getOpCode());
        if (res instanceof ToServerMessage) {
            ((ToServerMessage<?>)res).archTarget = ((ToServerMessage<?>)savedInstance).archTarget;
        }
        return res;
    }

    /**
     * Updates the opcode mappings for registered messages using the provided {@link Mapping}.
     * This is useful for dynamically updating opcodes based on server-side configurations.
     *
     * @param mapping the mapping containing the new opcodes for message classes
     */
    @Nullable
    public static void updateMapping(Mapping mapping) {
        if (mapping == null) return;
        synchronized (registeredClass) {
            mapping.forEach((key, opcode) -> {
                final Object value = VersionRegistry.registeredClass.get(key);
                if (value != null) {
                    if (value instanceof Message<?>) {
                        //noinspection deprecation
                        ((Message<?>) value).setOpCode(opcode);
                    }
                } else {
                    log.error("< No class found for ({})", key);
                }
            });
        }
    }

    private static Map<String, InjectionInfo> configureParseStrategies(String[] packageToScan) {
        Set<Class<?>> versionDependants = new HashSet<>();
        var registrator = new ExternalRegistration.Registrator(new HashMap<>());
        if (USE_DEFAULT_PARSE_CLASS_STRATEGY) {
            for (String package_ : packageToScan) {
                versionDependants.addAll(new Reflections(package_).getTypesAnnotatedWith(VersionDependant.class));
            }
            for (Class<?> versionDependant : versionDependants) {
                MessageRegistrationStrategy.PARSE_CLASS_STRATEGY.parse(registrator, versionDependant);
            }
        }
        for (ExternalRegistration externalRegistration : externalRegisteredImpl) {
            externalRegistration.register(registrator);
        }
        return registrator.elegibleForVersion();
    }

    private static void configure(String... packageToScan) {
        registeredClass.clear();
        Map<String, InjectionInfo> elegibleForVersion = configureParseStrategies(packageToScan);
        Map<String, InjectionInfo> toResolve = new HashMap<>();
        for (Map.Entry<String, InjectionInfo> entry : elegibleForVersion.entrySet()) {
            InjectionInfo value = entry.getValue();
            String key = entry.getKey();
            if (value.notResolved()) {
                toResolve.put(key, value);
            } else {
                registeredClass.put(value.superName(), value.serializer());
            }
        }
        if (NO_DEEP_RESOLVE_FLAG) {
            toResolve.values().forEach(v -> registeredClass.put(v.superName(), v.serializer()));
        } else {
            List<String> toRemove = new ArrayList<>();
            boolean resolvedThisTurn;
            while(! toResolve.isEmpty()) {
                resolvedThisTurn = false;
                for (Map.Entry<String, InjectionInfo> entry : toResolve.entrySet()) {
                    InjectionInfo value = entry.getValue();
                    if (value.resolveAny(registeredClass)) {
                        resolvedThisTurn = true;
                    }
                    if (! value.notResolved()) {
                        registeredClass.put(value.superName(), value.serializer());
                        toRemove.add(entry.getKey());
                    }
                }
                if (toRemove.isEmpty() || ! resolvedThisTurn) {
                    StringBuilder sb = new StringBuilder("Dependencies cannot be resolved for :").append(System.lineSeparator());
                    toResolve.values().forEach(s -> s.notResolved(sb));
                    throw new IllegalStateException(sb.toString());
                }
                for (String s : toRemove) {
                    toResolve.remove(s);
                }
                toRemove.clear();
            }
        }
        HashMap<Integer, List<Tuple<String, Boolean>>> opCodeCache = new HashMap<>();
        List<String> noOpCodeClassesForVersion = new ArrayList<>();
        for (Map.Entry<String, Object> entry : registeredClass.entrySet()) {
            String messageName = entry.getKey();
            Class<?> aClass = entry.getValue().getClass();
            if (Message.class.isAssignableFrom(aClass)) {
                int opCode = ((Message<?>) entry.getValue()).getOpCode();
                if (opCode == UNKNOWN_OPCODE) {
                    opCode = PacketFactory.findOpCode((Class<? extends Message>) aClass);
                    if (opCode != UNKNOWN_OPCODE && aClass.getSuperclass().getAnnotation(DuplicateOpCode.class) == null) {
                        opCodeCache.computeIfAbsent(opCode, k -> new ArrayList<>())
                            .add(new Tuple<>(messageName + '_' + aClass.getCanonicalName(), ToClientMessage.class.isAssignableFrom(aClass)));
                    } else {
                        noOpCodeClassesForVersion.add(aClass.getSimpleName());
                    }
                } else {
                    opCodeCache.computeIfAbsent(opCode, k -> new ArrayList<>())
                        .add(new Tuple<>(messageName + '_' + aClass.getCanonicalName(), ToClientMessage.class.isAssignableFrom(aClass)));
                }
                //noinspection deprecation
                ((Message<?>) entry.getValue()).setOpCode(opCode);
                if (ToServerMessage.class.isAssignableFrom(aClass)) {
                    byte archTarget = ((ToServerMessage<?>) entry.getValue()).archTarget;
                    if (archTarget == UNKNOWN_ARCH_TARGET) {
                        ArchTarget archTargetAnnotation = Version.getArchTargetForCurrentVersion(aClass);
                        if (archTargetAnnotation != null) {
                            ((ToServerMessage<?>) entry.getValue()).archTarget = archTargetAnnotation.value();
                        } else {
                            log.warn("- configure(): The message '{}' has no archTarget provided, using default value 0.", entry.getValue().getClass().getSimpleName());
                            ((ToServerMessage<?>) entry.getValue()).archTarget = 0;
                        }
                    }
                }
            }
        }

        if (! noOpCodeClassesForVersion.isEmpty()) {
            log.error("- configure(): The following classes have no valid opcode for version {} :\n{}", Version.getCurrent(), String.join(", ", noOpCodeClassesForVersion));
        }

        boolean fatal = false;
        for (var entry : opCodeCache.entrySet()) {
            Integer op = entry.getKey();
            var opCodeWithDirectionality = entry.getValue();
            var duplicated = opCodeWithDirectionality.stream()
                .collect(Collectors.partitioningBy(x -> x._2, Collectors.mapping(x -> x._1, Collectors.toList())))
                .values().stream().filter(e -> e.size() > 1)
                .toList();
            for (var duplicate : duplicated) {
                log.error("- configure(): OpCode {} declared multiples times ! ({})", op, String.join(",", duplicate));
                fatal = true;
            }
        }
        if (fatal) {
            System.exit(-1);
        }
    }

}
