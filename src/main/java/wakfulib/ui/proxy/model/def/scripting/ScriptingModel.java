package wakfulib.ui.proxy.model.def.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.ui.proxy.model.def.scripting.instructions.CompositeScriptingInstruction;
import wakfulib.ui.proxy.model.def.scripting.instructions.LoopScriptingInstruction;
import wakfulib.ui.proxy.model.def.scripting.instructions.RuntimeLoopScriptingInstruction;
import wakfulib.ui.proxy.model.def.scripting.instructions.SimpleScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.FixedSizedType;
import wakfulib.ui.proxy.model.def.type.SizeVariableType;
import wakfulib.ui.proxy.model.def.type.Type;
import wakfulib.ui.proxy.model.def.type.storage.ComplexeTypes;
import wakfulib.ui.proxy.model.def.type.storage.CustomTypesRegistry;
import wakfulib.ui.proxy.model.def.type.storage.Endianness;
import wakfulib.ui.proxy.model.def.type.storage.JavaTypes;
import wakfulib.ui.proxy.model.def.type.storage.ParametrizedTypes;
import wakfulib.utils.MathUtils;
import wakfulib.utils.data.Tuple;

public class ScriptingModel {

    private static final Map<String, Type> allTypes = getAllTypes();

    @Getter
    private final List<ScriptingInstruction> instructions = new ArrayList<>();

    private final Stack<Tuple<Integer, CompositeScriptingInstruction>> nestedInstructionLines = new Stack<>();

    public ScriptingModel() {
        registerInstructions();
    }

    public void parse(@NonNull String allText) throws ScriptParsingException {
        instructions.clear();
        nestedInstructionLines.clear();
        String[] scriptingLines = allText.split("\n");
        for (int i = 0; i < scriptingLines.length; i++) {
            String scriptingLine = scriptingLines[i].trim();
            if (scriptingLine.isBlank()) continue;
            var scriptingLineArgs = scriptingLine.split(" ");
            Object[] scriptLineArgsObj = new Object[Math.max(0, scriptingLineArgs.length - 1)];
            int argIndex = 0;
            for (int argumentIndex = 1; argumentIndex < scriptingLineArgs.length; argumentIndex++) {
                String argument = scriptingLineArgs[argumentIndex];
                var trimmedArg = argument.trim();
                if (trimmedArg.length() > 0) {
                    scriptLineArgsObj[argIndex] = trimmedArg;
                    argIndex++;
                }
            }
            if (scriptLineArgsObj.length == 0) {
                scriptLineArgsObj = null;
            }

            Type type;
            String label;

            if (getCompositeInstruction(scriptingLine, i)) {
                continue;
            }

            if (scriptingLine.startsWith("#")) {
                continue; // comment line
            } else
            if (scriptingLine.startsWith("SKIP")) {
                int how = getPositiveComputedArg(scriptingLine.substring(Math.min(scriptingLine.length(), 5)).trim(), i);
                type = new FixedSizedType(how, "SKIPPED", b -> {
                    byte[] bytes = new byte[how];
                    b.get(bytes);
                    return "[" + how + "]";
                });
                label = "";
            } else if (scriptingLine.startsWith("END")) {
                if (nestedInstructionLines.isEmpty()) {
                    throw new ScriptParsingException("Ending a non existent nested instruction", i + 1);
                } else {
                    nestedInstructionLines.pop();
                    continue;
                }
            } else {
                type = getType(scriptingLine);
                if (type == null) {
                    throw new ScriptParsingException("Unknown type '" + scriptingLine, i + 1);
                }
                label = scriptingLine.substring(type.getName().length());
            }
            SimpleScriptingInstruction simpleScriptingInstruction = new SimpleScriptingInstruction(type, label.trim(), scriptLineArgsObj);
            if (nestedInstructionLines.isEmpty()) {
                instructions.add(simpleScriptingInstruction);
            } else {
                nestedInstructionLines.peek()._2.addInstruction(simpleScriptingInstruction);
            }
        }
        if (! nestedInstructionLines.isEmpty()) {
            throw new ScriptParsingException("Nested instruction not closed !", nestedInstructionLines.pop()._1);
        }
    }

    private void addLoopingInstruction(int i, CompositeScriptingInstruction insideLoop, ScriptingInstruction newLoop) {
        if (nestedInstructionLines.isEmpty()) {
            instructions.add(newLoop);
        } else {
            nestedInstructionLines.peek()._2.addInstruction(newLoop);
        }
        nestedInstructionLines.push(new Tuple<>(i + 1, insideLoop));
    }

    @Nullable
    public static Type getType(String line) {
        int wordEnd = line.indexOf(" ");
        if (wordEnd == - 1) {
            wordEnd = line.indexOf("\n");
            if (wordEnd == - 1) {
                wordEnd = line.length();
            }
        }
        String type = line.substring(0, wordEnd);
        return allTypes.get(type.toUpperCase());
    }

    @NonNull
    public static Map<String, Type> getAllTypes() {
        if (allTypes != null) {
            return allTypes;
        }
        HashMap<String, Type> res = new HashMap<>();

        for (Type value : JavaTypes.values) {
            res.put(value.getName().toUpperCase(), value);
        }
        for (SizeVariableType value : ComplexeTypes.values) {
            res.put(value.getName().toUpperCase(), value);
        }
        for (Type value : ParametrizedTypes.values) {
            res.put(value.getName().toUpperCase(), value);
        }
        for (Type value : Endianness.values) {
            res.put(value.getName().toUpperCase(), value);
        }
        for (Entry<String, Type> customEntry : CustomTypesRegistry.getAllCustomTypesWithLabel()) {
            res.put(customEntry.getKey(), customEntry.getValue());
        }

        return res;
    }

    public void clear() {
        instructions.clear();
    }

    public ScriptingModel copy() {
        var newModel = new ScriptingModel();
        for (ScriptingInstruction instruction : instructions) {
            newModel.instructions.add(instruction.copy());
        }
        return newModel;
    }


    final Map<String, CompositeInstructionBuilder> compositeInstructionBuilderStorage = new HashMap<>();

    public void registerInstructions() {
        compositeInstructionBuilderStorage.put("IF", (i, arg) -> {
            CompositeScriptingInstruction insideLoop = new CompositeScriptingInstruction("", true);
            addLoopingInstruction(i, insideLoop, new RuntimeLoopScriptingInstruction("if" + arg.substring(2), true, insideLoop, (state) -> {
                Object topOfStack = state.getStack().peek();
                final AtomicBoolean stopLooping = new AtomicBoolean(true);
                if (topOfStack instanceof Number) {
                    int stackValue = ((Number) topOfStack).intValue();
                    stopLooping.set(stackValue == 0);
                    return x -> stopLooping.getAndSet(false);
                }
                if (topOfStack instanceof Boolean) {
                    stopLooping.set((Boolean) topOfStack);
                    return x -> stopLooping.getAndSet(false);
                }

                throw new IllegalStateException("Illegal stack operand for if operator ! (type: " + topOfStack.getClass().getSimpleName() + "value: '" + topOfStack + "')");
            }));
        });
        compositeInstructionBuilderStorage.put("PEEKLOOP", (i, arg) -> {
            CompositeScriptingInstruction insideLoop = new CompositeScriptingInstruction("#");
            addLoopingInstruction(i, insideLoop, new RuntimeLoopScriptingInstruction("loop", false, insideLoop, (state) -> {
                int howMuchLoop = ((Number) state.getStack().peek()).intValue();
                if (howMuchLoop < 0) throw new IllegalStateException("Negative looping !");
                return x -> x < howMuchLoop;
            }));
        });
        compositeInstructionBuilderStorage.put("REMANINGLOOP", (i, arg) -> {
            CompositeScriptingInstruction insideLoop = new CompositeScriptingInstruction("#");
            addLoopingInstruction(i, insideLoop,
                new RuntimeLoopScriptingInstruction("loop", false, insideLoop, (stack) -> x -> stack.getBuffer().hasRemaining()));
        });
        compositeInstructionBuilderStorage.put("LOOP", (i, arg) -> {
            CompositeScriptingInstruction insideLoop = new CompositeScriptingInstruction("#");
            addLoopingInstruction(i, insideLoop, new LoopScriptingInstruction(insideLoop, getPositiveComputedArg(arg.substring(4), i)));
        });
        compositeInstructionBuilderStorage.put("STRUCT", (i, arg) -> {
            CompositeScriptingInstruction insideLoop = new CompositeScriptingInstruction("", true);
            addLoopingInstruction(i, insideLoop, new RuntimeLoopScriptingInstruction(arg, true, insideLoop, s -> {
                final AtomicBoolean stopLooping = new AtomicBoolean(true);
                return x -> stopLooping.getAndSet(false);
            }));
        });
    }

    public static int getMathComputedArg(String arg, int line) throws ScriptParsingException {
        String howStr = arg.trim();
        try {
            return (int) (MathUtils.eval(howStr));
        } catch (IllegalArgumentException e) {
            throw new ScriptParsingException("Not a valid mathematical expression '" + howStr + "'", line + 1);
        }
    }

    public static int getPositiveComputedArg(String arg, int line) throws ScriptParsingException {
        int how = getMathComputedArg(arg, line);
        if (how < 0) {
            throw new ScriptParsingException("Negative value is not allowed ( " + arg + ") !", line + 1);
        }
        return how;
    }

    @Nullable
    public boolean getCompositeInstruction(String argLine, int i) throws ScriptParsingException {
        var typeName = argLine.split(" ")[0].toUpperCase();
        var compositeScriptingInstructionFunction = compositeInstructionBuilderStorage.get(typeName);
        if (compositeScriptingInstructionFunction == null) return false;
        compositeScriptingInstructionFunction.createInstruction(i, argLine);
        return true;
    }

    @FunctionalInterface
    private interface CompositeInstructionBuilder {
        void createInstruction(Integer lineNumber, String arg) throws ScriptParsingException;
    }
}
