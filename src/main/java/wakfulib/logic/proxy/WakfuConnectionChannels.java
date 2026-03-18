package wakfulib.logic.proxy;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WakfuConnectionChannels {
    private Channel backProvider;
    private Channel frontProvider;
}
