package wakfulib.exception;

import wakfulib.internal.Version;

/**
 * Thrown when there is a mismatch between the client and server versions.
 */
public class ServerVersionMismatch extends Exception {
    public ServerVersionMismatch(Version clientVersion, Version serverVersion) {
        super("Server version is not compatible with version " + clientVersion + " ! Excepted version : " + serverVersion);
    }
}
