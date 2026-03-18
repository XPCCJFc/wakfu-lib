package wakfulib.ui.proxy.model.def.scripting.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.Getter;
import wakfulib.ui.proxy.model.def.scripting.ScriptingInstruction;
import wakfulib.ui.proxy.model.def.scripting.generation.InstructionWriter.InstructionContext;
import wakfulib.ui.proxy.model.def.scripting.instructions.SimpleScriptingInstruction;
import wakfulib.ui.proxy.model.def.type.Type;
import wakfulib.utils.Tabulator;

public abstract class GeneratedClass {
    private final Set<String> imports = new HashSet<>();
    @Getter
    private final String packageName;
    @Getter
    private final String name;
    private final int op;
    private final Iterable<ScriptingInstruction> structure;
    private final Byte archTarget;

    private final String version;

    public GeneratedClass(String packageName, String name, int op, Iterable<ScriptingInstruction> structure, Byte archTarget, String version) {
        this.packageName = packageName == null ? getDefaultPackage() : packageName;
        this.name = name;
        this.op = op;
        this.structure = structure;
        this.archTarget = archTarget;
        this.version = version.replace(".", "_");
        imports.addAll(getTemplateImports());
    }

    protected abstract Collection<String> getTemplateImports();

    public String generateCode() {
        if (archTarget != null) {
            imports.add("import wakfulib.internal.ArchTarget;");
        }

        Tabulator tabulator = new Tabulator("    ");

        List<String> fields = new ArrayList<>();
        return Arrays.stream(getTemplate()
                .replace("%encode%", StreamSupport.stream(structure.spliterator(), false)
                    .map(instruction -> {
                        if (! (instruction instanceof SimpleScriptingInstruction i)) return "//FIXME Implement me !";
                        Type type = i.getType();
                        InstructionWriter encoding = type.encoding();
                        if (encoding == null) {
                            return "//TODO implement me " + i.getType() + " " + i.getLabel() + " " + Arrays.toString(i.getArgs());
                        }
                        var context = new InstructionContext(null, "o", i.javaId());
                        String res = encoding.apply(context);
                        imports.addAll(context.additionalImports);
                        if (res != null) {
                            fields.add(i.getType().getJavaType() + " " + i.javaId());
                        }
                        return res;
                    }).filter(Objects :: nonNull)
                    .map(i -> i + "\n")
                    .collect(Collectors.joining()))

                .replace("%unserialize%", StreamSupport.stream(structure.spliterator(), false)
                    .map(instruction -> {
                        if (! (instruction instanceof SimpleScriptingInstruction i)) return "//FIXME Implement me !";
                        Type type = i.getType();
                        InstructionWriter unserialize = type.unserialize();
                        if (unserialize == null) {
                            return "//TODO implement me " + i.getType() + " " + i.getLabel() + " " + Arrays.toString(i.getArgs());
                        }
                        var context = new InstructionContext("buffer", "res", i.javaId());
                        var res = unserialize.apply(context);
                        imports.addAll(context.additionalImports);
                        return res;
                    }).filter(Objects :: nonNull)
                    .map(i -> i + "\n")
                    .collect(Collectors.joining()))

                .replace("%imports%", String.join("\n", imports))
                .replace("%package%", packageName)
                .replace("%className%", name)
                .replace("%fields%", fields.stream().map(f -> "private " + f + ";\n").collect(Collectors.joining()))
                .replace("%op%", Integer.toString(op))
                .replace("%version%", version)
                .replace("%arch%", archTarget == null ? "" : "    @ArchTarget(" + archTarget + ")\n")

                .split("\n")).map(String :: trim)
            .map(l -> {
                if (l.endsWith("}")) {
                    tabulator.decrement();
                }
                String t = tabulator.tab();
                if (l.endsWith("{")) {
                    tabulator.increment();
                }
                return t + l;
            })
            .collect(Collectors.joining(System.lineSeparator()));
    }

    abstract String getTemplate();

    abstract String getDefaultPackage();
}
