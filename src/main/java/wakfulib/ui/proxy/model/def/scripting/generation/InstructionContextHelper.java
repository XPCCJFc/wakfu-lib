package wakfulib.ui.proxy.model.def.scripting.generation;

import wakfulib.utils.StringUtils;

public class InstructionContextHelper {

  public static String getterFor(String name) {
    return "get" + StringUtils.capitalize(name) + "()";
  }

  public static String setterFor(String name, String toSet) {
    return "set" + StringUtils.capitalize(name) + "(" + toSet + ")";
  }

  public static String getterForBoolean(String name) {
    return "is" + StringUtils.capitalize(name) + "()";
  }

}
