package wakfulib.logic.proxy;

import io.netty.channel.Channel;

public interface ChannelProvider {
    Channel getChannel();
}
