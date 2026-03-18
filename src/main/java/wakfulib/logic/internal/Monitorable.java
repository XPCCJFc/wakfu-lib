package wakfulib.logic.internal;

public interface Monitorable {

    String[] EMPTY_STR = new String[0];

    default String[] getMonitoringLabel() {
        return EMPTY_STR;
    }
}
