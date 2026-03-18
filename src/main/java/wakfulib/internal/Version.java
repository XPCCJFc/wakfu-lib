package wakfulib.internal;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.ui.proxy.listeners.VersionChangeListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Enumeration representing different versions of the Wakfu game.
 * Provides methods for version comparison, range checking, and serialization.
 */
public enum Version {
    /**
     * ONLY USE FOR TESTS
     */
    TEST(-2, -2, -2),
    /**
     * Represents an unknown version.
     */
    UNKNOWN(-1, -1, -1),

    v0_315(0, 315, 0),
    v1_50_0(1, 50, 2),
    v1_63_0(1, 63, 0),
    v1_63_2(1, 63, 2),
    v1_64_4(1, 64, 4),
    v1_65_2(1, 65, 2),
    v1_65_3(1, 65, 3),
    v1_66_1(1, 66, 1),
    v1_67_1(1, 67, 1),
    v1_67_2(1, 67, 2),
    v1_67_BETA(1, 67, -1),
    v1_68_0(1, 68, 0),
    v1_69_0(1, 69, 0),
    v1_70_4(1, 70, 4),
    v1_72_1(1, 72, 1),
    v1_74_1(1, 74, 1),
    v1_74_4(1, 74, 4),
    v1_75_4(1, 75, 4),
    v1_91_2(1, 91, 2),

    v2_4_ARENA(2, 4, -1, "ARENA"),
    v2_13_ARENA(2, 13, -1, "ARENA"),
    v2_70_ARENA(2, 70, -1, "ARENA")
    ;

    /**
     * Comparator for comparing version instances.
     */
    public static final Comparator<Version> versionComparator = (o1, o2) -> {
        int res = Integer.compare(o1.major, o2.major);
        if (res == 0) {
            res = Integer.compare(o1.minor, o2.minor);
            if (res == 0) {
                return Integer.compare(o1.revision, o2.revision);
            } else {
                return res;
            }
        } else {
            return res;
        }
    };


    @Getter
    private static Version current = UNKNOWN;
    /** The major version component. */
    @Getter
    private final byte major;
    /** The minor version component. */
    @Getter
    private final short minor;
    /** The revision version component. */
    @Getter
    private final byte revision;

    /**
     * Denotes the type of the game version.
     * Comparison cannot happen between two versions with different keys.
     */
    @Internal
    private final String key;

    Version(int major, int minor, int revision) {
        this(major, minor, revision, "wakfu");
    }

    Version(int major, int minor, int revision, String key) {
        Objects.requireNonNull(key);
        this.major = (byte) major;
        this.minor = (short) minor;
        this.revision = (byte) revision;
        this.key = key;
    }


    public static final byte[] s_encodedRequiredBuild;
    public static final byte[] s_internal_version;

    static {
        s_encodedRequiredBuild = Integer.toString(- 1).getBytes(StandardCharsets.UTF_8);
        s_internal_version = new byte[6 + s_encodedRequiredBuild.length];
    }

    /**
     * Sets the current version used by the library for protocol-dependent logic.
     *
     * @param newVersion The version to set as current.
     */
    public static void setCurrent(Version newVersion) {
        Version.current = newVersion;
        listeners.forEach(l -> l.onVersionChanged(newVersion));
    }

    private final static List<VersionChangeListener> listeners = new ArrayList<>();

    /**
     * Registers a listener to be notified when the current version changes.
     *
     * @param changeListener The listener to register.
     */
    public static void registerForVersionChanged(@NonNull VersionChangeListener changeListener) {
        listeners.add(changeListener);
    }

    /**
     * Packs the version components into a byte array for network transmission.
     *
     * @return The serialized version bytes.
     */
    public byte[] getAsByte() {
        ByteBuffer allocate = ByteBuffer.allocate(6 + s_encodedRequiredBuild.length);
        allocate.put(major);
        allocate.putShort(minor);
        allocate.put(revision);
        allocate.put(s_encodedRequiredBuild);
        return allocate.array();
    }

    /**
     * Finds a matching Version instance based on its numeric components.
     *
     * @param major The major version.
     * @param minor The minor version.
     * @param revision The revision.
     * @return The matching Version, or {@code UNKNOWN} if no match is found.
     */
    @NonNull
    public static Version getVersion(int major, int minor, int revision) {
        for (Version value : values()) {
            if (value.major == major && value.minor == minor && value.revision == revision) {
                return value;
            }
        }
        return Version.UNKNOWN;
    }

    /**
     * Checks if this version is between the specified minimum and maximum versions (inclusive).
     *
     * @param min The minimum allowed version (can be {@code UNKNOWN} for no lower bound).
     * @param max The maximum allowed version (can be {@code UNKNOWN} for no upper bound).
     * @return {@code true} if this version is within the range, {@code false} otherwise.
     */
    public boolean isInRange(@NonNull Version min, @NonNull Version max) {
        if (! this.key.equals(min.key)) return false;
        if (min == UNKNOWN && max == UNKNOWN) return false;
        if (this == TEST) {
            return min == TEST;
        }
        if (min == UNKNOWN || versionComparator.compare(this, min) >= 0) {
            if (max == UNKNOWN) {
                return true;
            } else return versionComparator.compare(this, max) <= 0;
        }
        return false;
    }

    /**
     * Retrieves architectural target information for the current game version
     * from annotations on the specified element.
     *
     * @param annotatedElement The element (class, method, etc.) to check for annotations.
     * @return The matching architecture target, or {@code null} if none match.
     */
    @Nullable
    public static ArchTarget getArchTargetForCurrentVersion(@NonNull AnnotatedElement annotatedElement) {
        var versionRange = annotatedElement.getAnnotation(ArchTarget.class);
        if (versionRange != null) return versionRange;
        var versionRanges = annotatedElement.getAnnotation(ArchTarget.ArchTargets.class);
        if (versionRanges != null) return getArchTargetForCurrentVersion(versionRanges);
        return null;
    }

    /**
     * Retrieves architectural target information for the current game version
     * from a collection of architecture target definitions.
     *
     * @param archTargets The collection of target definitions.
     * @return The matching architecture target, or {@code null} if none match.
     */
    @Nullable
    public static ArchTarget getArchTargetForCurrentVersion(@NonNull ArchTarget.ArchTargets archTargets) {
        return getArchTargetForGame(Version.getCurrent().key, archTargets);
    }

    /**
     * Filters architecture targets based on the specific game key.
     *
     * @param key The game key to match.
     * @param archTargets The collection of target definitions.
     * @return The matching architecture target, or {@code null} if none match.
     */
    @Nullable
    public static ArchTarget getArchTargetForGame(@NonNull String key, @NonNull ArchTarget.ArchTargets archTargets) {
        for (ArchTarget archTarget : archTargets.value()) {
            if (archTarget.gameKey().equals("universal") || archTarget.gameKey().equals(key))
                return archTarget;
        }
        return null;
    }

    /**
     * Retrieves version range information for the current game version
     * from annotations on the specified element.
     *
     * @param annotatedElement The element to check for annotations.
     * @return The matching version range, or {@code null} if none match.
     */
    @Nullable
    public static VersionRange getRangeForCurrentVersion(@NonNull AnnotatedElement annotatedElement) {
        var versionRange = annotatedElement.getAnnotation(VersionRange.class);
        if (versionRange != null) return versionRange;
        var versionRanges = annotatedElement.getAnnotation(VersionRange.VersionRanges.class);
        if (versionRanges != null) return getRangeForCurrentVersion(versionRanges);
        return null;
    }

    /**
     * Retrieves version range information for the current game version
     * from a collection of version range definitions.
     *
     * @param versionRanges The collection of version range definitions.
     * @return The matching version range, or {@code null} if none match.
     */
    @Nullable
    public static VersionRange getRangeForCurrentVersion(@NonNull VersionRange.VersionRanges versionRanges) {
        return getRangeForGame(Version.getCurrent().key, versionRanges);
    }

    /**
     * Filters version ranges based on the specific game key.
     *
     * @param key The game key to match.
     * @param versionRanges The collection of version range definitions.
     * @return The matching version range, or {@code null} if none match.
     */
    @Nullable
    public static VersionRange getRangeForGame(@NonNull String key, @NonNull VersionRange.VersionRanges versionRanges) {
        for (VersionRange versionRange : versionRanges.value()) {
            if (versionRange.min().key.equals(key))
                return versionRange;
        }
        return null;
    }

    /**
     * Checks if this version falls within the specified range.
     *
     * @param range The version range to check.
     * @return {@code true} if this version is within the range, {@code false} otherwise.
     */
    public boolean isInRange(@NonNull VersionRange range) {
        return isInRange(range.min(), range.max());
    }

    /**
     * Returns a human-readable string representation of the version (e.g., "1.67.2").
     *
     * @return The formatted version string.
     */
    public String toString() {
        return name().replaceFirst("v", "").replaceAll("_", ".");
    }

    /**
     * Writes the version components into a data stream.
     *
     * @param out The stream to write to.
     * @throws IOException If a writing error occurs.
     */
    public void serialize(@NonNull DataOutputStream out) throws IOException {
        out.writeByte(major);
        out.writeShort(minor);
        out.writeByte(revision);
    }

    /**
     * Reads a version from a data stream and returns the corresponding instance.
     *
     * @param in The stream to read from.
     * @return The deserialized Version instance.
     * @throws IOException If a reading error occurs.
     */
    @NotNull
    public static Version unserialize(@NonNull DataInputStream in) throws IOException {
        return getVersion(in.read(), in.readShort(), in.read());
    }

    /**
     * Checks if this version is strictly older than the specified version.
     *
     * @param v The version to compare with.
     * @return {@code true} if this is older, {@code false} otherwise.
     */
    public boolean isOlderThan(Version v) {
        return versionComparator.compare(this, v) < 0;
    }

    /**
     * Checks if this version is strictly newer than the specified version.
     *
     * @param v The version to compare with.
     * @return {@code true} if this is newer, {@code false} otherwise.
     */
    public boolean isNewerThan(Version v) {
      return versionComparator.compare(this, v) > 0;
    }

    /**
     * Checks if this version belongs to the same game type (key) as another version.
     *
     * @param value The version to check against.
     * @return {@code true} if they have the same key, {@code false} otherwise.
     */
    public boolean hasSameKey(Version value) {
        return this.key.equals(value.key);
    }
    
    /**
     * Groups all known versions by their game key.
     *
     * @return A map where the key is the game type and the value is a list of its versions.
     */
    public static Map<String, List<Version>> allVersionByKey() {
        var res = new HashMap<String, List<Version>>();
        for (Version value : values()) {
            var versions = res.computeIfAbsent(value.key, k -> new ArrayList<>());
            versions.add(value);
        }
        return res;
    }
}
