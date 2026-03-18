package wakfulib.ui.proxy.model.def.scripting;

import lombok.Getter;

public class ScriptParsingException extends Exception {

  @Getter
  private final int line;

  public ScriptParsingException(String message, int line) {
    super(message);
    this.line = line;
  }
}
