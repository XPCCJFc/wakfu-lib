package wakfulib.logic.pipeline;

public record MessagePacket(
    int size,
    byte archTarget,
    DataNettyBuffer buffer) {

}
