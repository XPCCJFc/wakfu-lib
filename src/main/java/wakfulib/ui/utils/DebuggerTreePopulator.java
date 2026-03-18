package wakfulib.ui.utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wakfulib.doc.NonNull;
import wakfulib.doc.Nullable;
import wakfulib.internal.In;
import wakfulib.internal.Inject;
import wakfulib.internal.Internal;
import wakfulib.internal.Version;
import wakfulib.internal.View;
import wakfulib.utils.ReflectionUtils;
import wakfulib.utils.data.Tuple;

@Slf4j
public final class DebuggerTreePopulator {

    static {
        Class<?> protoClassTemp;
        try {
            protoClassTemp = Class.forName("com.google.protobuf.Message");
        } catch (Exception e) {
            protoClassTemp = null;
        }
        protoClass = protoClassTemp;
    }

    public static boolean HIDE_NULL = false;
    private final static Class<?> protoClass;

    public static void parse(@Nullable Object o, DefaultMutableTreeNode root) {
        var searchQueue = new ArrayDeque<Tuple<Object, DefaultMutableTreeNode>>(1_000);
        searchQueue.addFirst(new Tuple<>(o, root));
        parse(searchQueue);
    }

    private static void parse(@NonNull Queue<Tuple<Object, DefaultMutableTreeNode>> searchQueue) {
        var addedNodes = new HashMap<String, DefaultMutableTreeNode>();
        while (! searchQueue.isEmpty()) {
            var nodeInfo = searchQueue.poll();
            var o = nodeInfo._1;
            var root = nodeInfo._2;
            if (o == null) {
                root.setUserObject(root.getUserObject() + ": null (?)");
            } else {
                Class<?> clazz = o.getClass();
                if (o instanceof MutliLineString) {
                    root.setUserObject(o);
                } else if (clazz.isPrimitive() || clazz == String.class || clazz == Byte.class || clazz == Integer.class || clazz == Float.class || clazz == Long.class || clazz == Character.class || clazz == Double.class || clazz == Short.class || clazz == Boolean.class) {
                    root.setUserObject(root.getUserObject() + ": " + o + " (" + clazz.getSimpleName() + ")");
                } else if (clazz.isEnum()) {
                    root.setUserObject(root.getUserObject() + ": " + ((Enum<?>) o).name() + " (" + clazz.getSimpleName() + ")");
                } else if (protoClass != null && protoClass.isInstance(o)) {
                    root.setUserObject("Proto " + clazz.getSimpleName());
                    toTree(root, o.toString());
                } else if (clazz.isArray()) {
                    array(searchQueue,o, root, clazz);
//                } else if (o instanceof TLongObjectMap) {
//                    AtomicInteger i = new AtomicInteger();
//                    ((TLongObjectMap<?>) o).forEachEntry((k, v) -> {
//                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode("[" + k + "]");
//                        searchQueue.offer(new Tuple<>(v, node));
//                        root.add(node);
//                        i.getAndIncrement();
//                        return true;
//                    });
//                    String paramClass = "?";
//                    try {
//                        paramClass = getGenericClass(clazz).getSimpleName();
//                    } catch (Exception ignored) {
//                    }
//                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + "<" + paramClass + ">" + "(" + i + "))");
//                } else if (o instanceof TByteCollection) {
//                    final byte[] array = ((TByteCollection) o).toArray();
//                    array(searchQueue,array, root, byte[].class);
//                } else if (o instanceof TIntCollection) {
//                    final int[] array = ((TIntCollection) o).toArray();
//                    array(searchQueue, array, root, int[].class);
//                } else if (o instanceof TLongCollection) {
//                    final long[] array = ((TLongCollection) o).toArray();
//                    array(searchQueue,array, root, long[].class);
                } else if (o instanceof Iterable iterable) {
                    int i = 0;
                    for (Object o1 : iterable) {
                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode("[" + i + "]");
                        searchQueue.offer(new Tuple<>(o1, node));
                        root.add(node);
                        i++;
                    }
                    String paramClass = "?";
                    try {
                        paramClass = getGenericClass(clazz).getSimpleName();
                    } catch (Exception ignored) {
                    }
                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + "<" + paramClass + ">" + "(" + i + "))");
//                } else if (o instanceof TIntObjectMap<?> tIntObjectMap) {
//                    tIntObjectMap.forEachEntry((k, v) -> {
//                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(k);
//                        searchQueue.offer(new Tuple<>(v, node));
//                        root.add(node);
//                        return true;
//                    });
//                    String paramClass = "?";
//                    try {
//                        paramClass = getGenericClass(clazz).getSimpleName();
//                    } catch (Exception ignored) {
//                    }
//                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + "<" + paramClass + ">" + "(" + tIntObjectMap.size() + "))");
//                } else if (o instanceof TShortObjectMap<?> tShortObjectMap) {
//                    tShortObjectMap.forEachEntry((k, v) -> {
//                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(k);
//                        searchQueue.offer(new Tuple<>(v, node));
//                        root.add(node);
//                        return true;
//                    });
//                    String paramClass = "?";
//                    try {
//                        paramClass = getGenericClass(clazz).getSimpleName();
//                    } catch (Exception ignored) {
//                    }
//                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + "<" + paramClass + ">" + "(" + tShortObjectMap.size() + "))");
                } else if (o instanceof Map<?, ?> map) {
                    map.forEach((k, v) -> {
                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(k);
                        searchQueue.offer(new Tuple<>(v, node));
                        root.add(node);
                    });
                    String paramClass = "?";
                    try {
                        paramClass = getGenericClass(clazz).getSimpleName();
                    } catch (Exception ignored) {
                    }
                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + "<" + paramClass + ">" + "(" + map.size() + "))");
                } else if (o instanceof BitSet) {
                    array(searchQueue, ((BitSet) o).stream().toArray(), root, int[].class);
                } else {
                    root.setUserObject(root.getUserObject() + " (" + clazz.getSimpleName() + ")");
                    View annotation = clazz.getAnnotation(View.class);
                    if (annotation != null) {
                        try {
                            Method getView = annotation.viewer().getDeclaredMethod("getView", Object.class);
                            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
                            Object result = getView.invoke(null, o);
                            if (result == null && HIDE_NULL) {
                                continue;
                            }
                            if (annotation.inline()) {
                                searchQueue.offer(new Tuple<>(result, root));
                            } else {
                                searchQueue.offer(new Tuple<>(result, child));
                                root.add(child);
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            log.error("View for {} is not accessible !", annotation.viewer(), e);
                        }
                    } else {
                        var oHash = System.identityHashCode(o) + String.valueOf(o.hashCode()) + clazz.getSimpleName();
                        var alreadyComputedNode = addedNodes.get(oHash);
                        if (alreadyComputedNode != null) {
                            root.setUserObject(new Redirection(root.getUserObject(), alreadyComputedNode));
                            continue;
                        }
                        addedNodes.put(oHash, root);
                        for (Field declaredField : ReflectionUtils.getAllFields(clazz)) {
                            if (Modifier.isStatic(declaredField.getModifiers())) {
                                continue;
                            }
                            if (declaredField.getAnnotation(Inject.class) != null
                                || declaredField.getAnnotation(Internal.class) != null
                                || declaredField.getAnnotation(In.class) != null) {
                                continue;
                            }
                            var versionRange = Version.getRangeForCurrentVersion(declaredField);
                            if (versionRange != null) {
                                if (!Version.getCurrent().isInRange(versionRange)) {
                                    continue;
                                }
                            }
                            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(declaredField.getName());

                            View viewAnnotation = declaredField.getAnnotation(View.class);
                            if (viewAnnotation == null) {
                                if (! declaredField.trySetAccessible()) {
                                    root.setUserObject(root.getUserObject() + " <Inaccessible>");
                                } else {
                                    declaredField.setAccessible(true);
                                    try {
                                        Object fieldValue = declaredField.get(o);
                                        if (fieldValue == null && HIDE_NULL) {
                                            continue;
                                        }
                                        searchQueue.offer(new Tuple<>(fieldValue, node));
                                    } catch (IllegalAccessException e) {
                                        log.error("Should not happens", e);
                                    }
                                }
                            } else {
                                try {
                                    Method getView = viewAnnotation.viewer().getDeclaredMethod("getView", Object.class);
                                    DefaultMutableTreeNode child = new DefaultMutableTreeNode();
                                    Object result = getView.invoke(null, o);
                                    if (result == null && HIDE_NULL) {
                                        continue;
                                    }
                                    searchQueue.offer(new Tuple<>(result, child));
                                    node.add(child);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    log.error("View for {} is not accessible !", declaredField, e);
                                }

                            }
                            root.add(node);
                        }
                    }
                }
            }
        }
    }

    private static void array(@NonNull Queue<Tuple<Object, DefaultMutableTreeNode>> searchQueue, Object o, DefaultMutableTreeNode root, Class<?> clazz) {
        int length = Array.getLength(o);
        root.setUserObject(root.getUserObject() + " (" + clazz.getComponentType().getSimpleName()+"[" + length + "])");
        for (int i = 0; i < length; i ++) {
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode("[" + i + "]");
            searchQueue.offer(new Tuple<>(Array.get(o, i), node));
            root.add(node);
        }
    }

    private static void toTree(DefaultMutableTreeNode root, String json) {
        if (json.isEmpty()) return;
        String[] split = json.split("\n");

        for (String line : split) {
            char last = line.charAt(line.length() - 1);
            if (last == '{') {
                String val = line.substring(0, line.length() - 2);
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(val + ":");
                root.add(child);
                root = child;
            } else if (last == '}') {
                root = (DefaultMutableTreeNode) root.getParent();
            } else {
                String[] val = line.split(":");
                root.add(new DefaultMutableTreeNode(val[0].trim() + ":" + val[1]));
            }
        }
    }

    private static Class<?> getGenericClass(Class<?> clazz) {
        ParameterizedType type = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] types = type.getActualTypeArguments();
        return (Class<?>) types[0];
    }

    private record Redirection(@NonNull Object oldValue, @NonNull DefaultMutableTreeNode redirection) {

        @Override
        public String toString() {
            return oldValue + " {Already Computed}";
        }
    }

    @AllArgsConstructor
    public static class DebuggerTreeMouseListener extends MouseAdapter {

        private final JTree tree;
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && !e.isConsumed()) {
                e.consume();
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    Object lastPathComponent = path.getLastPathComponent();
                    if (lastPathComponent instanceof DefaultMutableTreeNode defaultMutableTreeNode) {
                        var userObject = defaultMutableTreeNode.getUserObject();
                        if (userObject instanceof Redirection redirection) {
                            var treePath = new TreePath(redirection.redirection.getPath());
                            tree.getSelectionModel().setSelectionPath(treePath);
                            tree.scrollPathToVisible(treePath);
                        }
                    }
                }
            }
        }
    }
}
