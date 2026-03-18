package wakfulib.logic.command;

public interface CommandIssuer {

    void message(String text);
    void error(String text);
}
