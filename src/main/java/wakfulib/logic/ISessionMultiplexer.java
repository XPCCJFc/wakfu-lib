package wakfulib.logic;

import io.netty.channel.Channel;
import wakfulib.doc.NonNull;
import wakfulib.internal.versionable.protocol.Message;

import java.util.Iterator;

public interface ISessionMultiplexer {
    Iterator<Channel> iterate();

    void add(Session session);

    void remove(Session session);

    void broadcast(@NonNull Message out);

    void broadcastExcept(@NonNull Message out, @NonNull Session session);
}
