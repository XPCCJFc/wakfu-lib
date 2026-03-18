package wakfulib.ui.proxy.listeners;

import wakfulib.internal.Version;

@FunctionalInterface
public interface VersionChangeListener {
    void onVersionChanged(Version version);
}
