package shellderp.bcexplorer;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.reference.Reference;
import shellderp.bcexplorer.reference.InstructionFilter;
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
 * <p/>
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:31 PM
 */
public class ClassHierarchy {
    private HashMap<String, Node<ClassGen>> classes = new HashMap<String, Node<ClassGen>>();
    Node<ClassGen> rootClass;

    public ClassHierarchy(String rootClassName) throws ClassNotFoundException {
        // initialize the root class
        rootClass = new Node<ClassGen>(new ClassGen(Repository.lookupClass(rootClassName)));
        rootClass.setDisplayText(rootClass.get().getClassName());
        classes.put(rootClass.get().getClassName(), rootClass);
    }

    public void loadClasses(List<ClassGen> loadList) {
        Queue<String> loadQueue = new LinkedList<String>();
        for (ClassGen cg : loadList) {
            if (classes.containsKey(cg.getClassName())) {
                System.err.println("[WARNING] skipping class already loaded: " + cg.getClassName());
                continue;
            }
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
                    System.err.println("WARNING: superclass missing: " + className);
                }
            }
        }

        rootClass.sortAll(); // TODO maybe do this while inserting?
    }

    public void loadJarFile(JarFile jar) throws IOException {
        Enumeration<JarEntry> jarEntries = jar.entries();
        List<ClassGen> classes = new LinkedList<ClassGen>();
        while (jarEntries.hasMoreElements()) {
            JarEntry je = jarEntries.nextElement();
            if (je.getName().endsWith(".class")) {
                ClassParser cp = new ClassParser(jar.getInputStream(je), je.getName());
                classes.add(new ClassGen(cp.parse()));
            }
        }
        loadClasses(classes);
    }

    public void loadDirectory(File[] dirs) throws IOException {
        List<ClassGen> classes = new LinkedList<>();

        Stack<File> subDirs = new Stack<>();
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
                } else if (file.getName().endsWith(".jar")) {
                    loadJarFile(new JarFile(file));
                }
            }
        } while (!subDirs.empty());

        loadClasses(classes);
    }

    public void unloadClasses() {
        rootClass.removeAllChildren();
        classes.clear();
        classes.put(rootClass.get().getClassName(), rootClass);
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
     * Searches for references in all classes in the hierarchy.
     */
    public List<Reference> findReferences(InstructionFilter filter) {
        List<Reference> refs = new ArrayList<>();

        for (Node<ClassGen> classNode : classes.values()) {
            refs.addAll(findReferences(classNode.get(), filter));
        }

        return refs;
    }

    public static List<Reference> findReferences(ClassGen visitClass, InstructionFilter filter) {
        ConstantPoolGen cpgen = visitClass.getConstantPool();

        List<Reference> refs = new ArrayList<>();

        // Visit every instruction of every method
        for (Method method : visitClass.getMethods()) {
            InstructionList list = new MethodGen(method, visitClass.getClassName(), cpgen).getInstructionList();
            if (list == null)
                continue;

            for (InstructionHandle ih : list.getInstructionHandles()) {
                Instruction instruction = ih.getInstruction();

                if (filter.filter(visitClass, method, instruction)) {
                    refs.add(new Reference(visitClass, method, ih));
                }
            }
        }

        return refs;
    }

    public HashMap<ClassGen, Method> findOverrides(Node<ClassGen> cgNode, Method method) {
        // todo: test this method
        HashMap<ClassGen, Method> overrides = new HashMap<>();
        String targetName = method.getName();
        String targetSig = method.getSignature();
        for (Node<ClassGen> child : cgNode) {
            if (!child.isLeaf())
                overrides.putAll(findOverrides(child, method));
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
