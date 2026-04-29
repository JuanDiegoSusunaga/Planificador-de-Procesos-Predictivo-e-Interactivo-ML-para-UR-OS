package ur_os.telemetry;

/**
 * Defines the global User Intent profiles that dictate the OS scheduling behavior.
 * Using an Enum prevents Magic String errors across the project.
 */
public enum UserProfile {
    DEVELOPMENT,
    OFFICE,
    MULTIMEDIA,
    DEFAULT
}
