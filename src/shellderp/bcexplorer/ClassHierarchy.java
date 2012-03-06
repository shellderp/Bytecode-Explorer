package shellderp.bcexplorer;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.ui.DefaultTreeContextMenuProvider;
import shellderp.bcexplorer.ui.TreeContextMenuListener;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassHierarchy maintains the hierarchy of loaded classes, and provides a JTree
 * component to display the classes in the explorer.
 * 
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:31 PM
 */
public class ClassHierarchy {
    private HashMap<String, Node<ClassGen>> classes = new HashMap<String, Node<ClassGen>>();
    Node<ClassGen> rootClass;

    public ClassHierarchy(List<ClassGen> loadList) {

        // initialize the root class (Object)
        try {
            rootClass = new Node<ClassGen>(new ClassGen(Repository.lookupClass("java.lang.Object")));
            rootClass.setDisplayText("java.lang.Object");
            classes.put("java.lang.Object", rootClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Queue<String> loadQueue = new LinkedList<String>();
        for (ClassGen cg : loadList) {
            classes.put(cg.getClassName(), new Node<ClassGen>(cg));
            loadQueue.add(cg.getClassName());
        }

        while (!loadQueue.isEmpty()) {
            String className = loadQueue.poll();
            Node<ClassGen> node = classes.get(className);
            node.setDisplayText(className);
            String superName = node.get().getSuperclassName();

            if (classes.containsKey(superName)) {
                classes.get(superName).addChild(node);
            } else {
                // super wasn't in list, attempt to load from repository
                try {
                    ClassGen cg = new ClassGen(Repository.lookupClass(superName));
                    Node<ClassGen> superNode = new Node<ClassGen>(cg);
                    classes.put(superName, superNode);
                    loadQueue.add(superName);
                    superNode.addChild(node);
                } catch (ClassNotFoundException e) {
                    System.err.println("Superclass missing: " + className);
                    System.err.println(e);
                }
            }
        }

        rootClass.sortAll(); // TODO maybe do this while inserting?
    }

    public static ClassHierarchy fromJarFile(JarFile jar) throws IOException {
        Enumeration<JarEntry> jarEntries = jar.entries();
        List<ClassGen> classes = new LinkedList<ClassGen>();
        while (jarEntries.hasMoreElements()) {
            JarEntry je = jarEntries.nextElement();
            if (je.getName().endsWith(".class")) {
                ClassParser cp = new ClassParser(jar.getInputStream(je), je.getName());
                classes.add(new ClassGen(cp.parse()));
            }
        }
        return new ClassHierarchy(classes);
    }

    public static ClassHierarchy fromDirectories(File[] dirs) throws IOException {
        List<ClassGen> classes = new LinkedList<ClassGen>();

        Stack<File> subDirs = new Stack<File>();
        for (File dir : dirs) {
            subDirs.push(dir);
        }

        do {
            File[] list = subDirs.pop().listFiles();
            for (File file : list) {
                if (file.isDirectory()) {
                    subDirs.push(file);
                } else if (file.getName().endsWith(".class")) {
                    ClassParser cp = new ClassParser(new FileInputStream(file), file.getName());
                    classes.add(new ClassGen(cp.parse()));
                }
            }
        } while (!subDirs.empty());

        return new ClassHierarchy(classes);
    }

    public JTree buildJTree(final ClassTabPane classTabPane) {
        final JTree tree = new JTree(rootClass);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // double click opens the class, but we can still allow the user to expand the tree with a triple click
        tree.setToggleClickCount(3);

        // add a listener to open new tab when a class is double clicked
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Node<ClassGen> node = (Node<ClassGen>) tree.getLastSelectedPathComponent();
                    if (node != null)
                        classTabPane.openClassTab(node.get());
                }
            }
        });

        tree.addMouseListener(new TreeContextMenuListener<>(new DefaultTreeContextMenuProvider<>(), tree, null));

        return tree;
    }

    /*
     * Search for references to a value of targetClass, in all classes in the hierarchy.
     */
    public Node findReferences(ClassGen targetClass, org.apache.bcel.classfile.FieldOrMethod value) {
        Node refs = new Node("References to " + targetClass.getClassName() + " : " + value);
        HashMap<ClassGen, FieldOrMethod> targets = new HashMap<>();
        targets.put(targetClass, value);
        if (value instanceof Method) {
            Node<ClassGen> cgNode = classes.get(targetClass.getClassName());
            Method method = (Method) value;
            if (!cgNode.isLeaf() && !method.isPrivate()) {
                HashMap<ClassGen, Method> overrides = getOverridingClasses(cgNode, method);

                if (overrides.size() > 0) {
                    // TODO probably get rid of this dialog entirely
                    int input = JOptionPane.showConfirmDialog(null, "The selected method is overriden.\nSearch for references to overriden methods as well?", "Notice", JOptionPane.YES_NO_OPTION);
                    if (input == JOptionPane.YES_OPTION) {
                        targets.putAll(overrides);
                    }
                }
            }
        }
        for (Map.Entry<ClassGen, FieldOrMethod> target : targets.entrySet()) {
            for (Node<ClassGen> cg : classes.values()) {

                ClassGen scanClass = target.getKey();
                FieldOrMethod targetValue = target.getValue();
                Node n = findReferences(scanClass, targetValue, cg.get());
                if (n != null)
                    refs.addChild(n);
            }
        }
        return refs.getChildCount() > 0 ? refs : null;
    }

    public static Node findReferences(ClassGen targetClass, org.apache.bcel.classfile.FieldOrMethod value, ClassGen scanClass) {
        // TODO cleanup
        ConstantPoolGen cpgen = scanClass.getConstantPool();
        Node refs = new Node("in " + scanClass.getClassName());
        if (value instanceof Field) {
            Field field = (Field) value;
            for (Method method : scanClass.getMethods()) {
                InstructionList list = new MethodGen(method, scanClass.getClassName(), cpgen).getInstructionList();
                if (list == null)
                    continue;
                Node methodRefs = new Node("in " + method);
                for (InstructionHandle ih : list.getInstructionHandles()) {
                    Instruction instruction = ih.getInstruction();
                    if (instruction instanceof FieldInstruction) {
                        FieldInstruction fi = (FieldInstruction) ih.getInstruction();
                        try {
                            ObjectType loadType = fi.getLoadClassType(cpgen);
                            if (!loadType.getClassName().equals(targetClass.getClassName()))
                                continue;
                        } catch (Exception ex) {
                            System.err.println(scanClass + " " + method + " " + fi + " " + ex);
                            continue;
                        }
                        if (fi.getFieldName(cpgen).equals(field.getName()) && fi.getFieldType(cpgen).equals(field.getType())) {
                            methodRefs.addChild(new Reference(scanClass, method, ih));
                        }
                    }
                }
                if (methodRefs.getChildCount() > 0)
                    refs.addChild(methodRefs);
            }
        } else if (value instanceof Method) {
            Method target = (Method) value;
            String targetName = target.getName();
            String targetSig = target.getSignature();
            for (Method method : scanClass.getMethods()) {
                InstructionList list = new MethodGen(method, scanClass.getClassName(), cpgen).getInstructionList();
                if (list == null)
                    continue;
                for (InstructionHandle ih : list.getInstructionHandles()) {
                    Instruction instruction = ih.getInstruction();
                    if (instruction instanceof InvokeInstruction) {
                        InvokeInstruction invoke = (InvokeInstruction) ih.getInstruction();
                        if (!invoke.getLoadClassType(cpgen).getClassName().equals(targetClass.getClassName()))
                            continue;
                        if (targetName.equals(invoke.getMethodName(cpgen)) && targetSig.equals(invoke.getSignature(cpgen))) {
                            refs.addChild(new Reference(scanClass, method, ih));
                        }
                    }
                }
            }
        }
        return refs.getChildCount() > 0 ? refs : null;
    }

    public HashMap<ClassGen, Method> getOverridingClasses(Node<ClassGen> cgNode, Method method) {
        // todo: test this method
        HashMap<ClassGen, Method> overrides = new HashMap<ClassGen, Method>();
        String targetName = method.getName();
        String targetSig = method.getSignature();
        for (Node<ClassGen> child : cgNode) {
            if (!child.isLeaf())
                overrides.putAll(getOverridingClasses(child, method));
            for (Method m : cgNode.get().getMethods()) {
                if (targetName.equals(m.getName()) && targetSig.equals(m.getSignature())) {
                    overrides.put(child.get(), m);
                    break;
                }
            }
        }
        return overrides;
    }

    public Node<ClassGen> findSuperDeclaration(ClassGen cg, Method method) {
        Node<ClassGen> node = classes.get(cg.getClassName());
        Node<ClassGen> superNode = node.getParent();
        if (superNode == null)
            return null;

        for (Method m : superNode.get().getMethods()) {
            if (m.equals(method)) {
                return superNode;
            }
        }

        return findSuperDeclaration(superNode.get(), method);
    }
}
