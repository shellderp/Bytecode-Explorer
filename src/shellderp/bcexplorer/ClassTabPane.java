package shellderp.bcexplorer;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.ByteSequence;
import shellderp.bcexplorer.reference.FieldReferenceFilter;
import shellderp.bcexplorer.reference.MethodReferenceFilter;
import shellderp.bcexplorer.reference.Reference;
import shellderp.bcexplorer.ui.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a tabbed pane that displays open classes and their members.
 * <p/>
 * Created by: Mike
 * Date: 1/15/12
 * Time: 11:22 PM
 */
public class ClassTabPane extends JTabbedPane {
    private ClassHierarchy classHierarchy;
    private JTabbedPane resultTabPane;

    public ClassTabPane(ClassHierarchy classHierarchy, JTabbedPane resultTabPane) {
        super();

        this.classHierarchy = classHierarchy;
        this.resultTabPane = resultTabPane;

        addMouseListener(new MiddleClickCloseTabListener(this));
    }

    public JTree openClassTab(final ClassGen cg) {
        int openIndex = indexOfTab(cg.getClassName());
        if (openIndex != -1) {
            setSelectedIndex(openIndex);

            JScrollPane scrollPane = (JScrollPane) getComponentAt(openIndex);
            return (JTree) scrollPane.getViewport().getView();
        }

        ConstantPoolGen cpgen = cg.getConstantPool();
        Node<String> root = new Node<>("Class " + cg.getClassName() + " extends " + cg.getSuperclassName());

        Node<String> cp = root.addChild("Constant Pool (" + cpgen.getSize() + " entries)");
        for (int i = 0; i < cpgen.getSize(); i++) {
            Constant constant = cpgen.getConstant(i);
            if (constant == null)
                continue;

            String type = constant.getClass().getSimpleName();
            Node typeNode = cp.findChild(type);
            if (typeNode == null)
                typeNode = cp.addChild(type);
            typeNode.addChild(i + ": " + cpgen.getConstantPool().constantToString(constant));
        }

        Node fields = root.addChild("Fields");
        for (Field field : cg.getFields()) {
            fields.addChild(field);
        }

        Node methods = root.addChild("Methods");
        for (Method method : cg.getMethods()) {
            Node m = methods.addChild(method);
            
            InstructionList list = new MethodGen(method, cg.getClassName(), cpgen).getInstructionList();
            if (list == null)
                continue;

            int posMaxDigits = String.valueOf(list.getEnd().getPosition()).length();
            for (InstructionHandle ih : list.getInstructionHandles()) {
                String label = String.format("%" + posMaxDigits + "d: %s", ih.getPosition(), ih.getInstruction().toString(cpgen.getConstantPool()));
                m.addChild(new InstructionWrapper(ih, method)).setDisplayText(label);
            }
        }

        final JTree classTree = new JTree(root);
        classTree.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        classTree.addMouseListener(new TreeContextMenuListener<>(contextMenuProvider, classTree, cg));

        classTree.expandPath(fields.getPath());
        classTree.expandPath(methods.getPath());

        addTab(cg.getClassName(), new JScrollPane(classTree));

        setSelectedIndex(getComponentCount() - 1);

        return classTree;
    }

    // TODO find suitable location
    private void addReferenceTab(Object value, List<Reference> refs) {
        if (refs.isEmpty())
            return;

        ReferenceTree rt = new ReferenceTree(this, refs);
        resultTabPane.addTab("Refs to " + value, new JScrollPane(rt));
        resultTabPane.setSelectedIndex(resultTabPane.getComponentCount() - 1);
    }

    private TreeContextMenuProvider<ClassGen> contextMenuProvider = new DefaultTreeContextMenuProvider<ClassGen>() {
        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node, final ClassGen openClass) {
            JPopupMenu menu = super.createContextMenu(tree, path, node, openClass);

            if (node.get() instanceof Field) {
                menu.addSeparator();

                final Field field = (Field) node.get();

                menu.add(new AbstractAction("Find Local References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addReferenceTab(field, ClassHierarchy.findReferences(openClass, new FieldReferenceFilter(openClass, field)));
                    }
                });

                menu.add(new AbstractAction("Find Global References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addReferenceTab(field, classHierarchy.findReferences(new FieldReferenceFilter(openClass, field)));
                    }
                });

                menu.addSeparator();

                menu.add(new AbstractAction("Refactor name") {
                    @Override public void actionPerformed(ActionEvent e) {
                        // TODO find references, optionally check for conflicts, then update the references to the new name
                        // possibly use bcel to verify modified classes afterwards?
                    }
                });
            } else if (node.get() instanceof Method) {
                menu.addSeparator();

                final Method method = (Method) node.get();

                menu.add(new AbstractAction("Find Local References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addReferenceTab(method, ClassHierarchy.findReferences(openClass, new MethodReferenceFilter(openClass, method)));
                    }
                });

                menu.add(new AbstractAction("Find Global References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addReferenceTab(method, classHierarchy.findReferences(new MethodReferenceFilter(openClass, method)));
                    }
                });

                menu.addSeparator();

                final Node<ClassGen> superDecl = classHierarchy.findSuperDeclaration(openClass, method);
                if (superDecl != null) {
                    menu.add(new AbstractAction("Go to super declaration") {
                        @Override public void actionPerformed(ActionEvent e) {
                            JTree classTree = openClassTab(superDecl.get());
                            
                            Node root = (Node) classTree.getModel().getRoot();
                            SwingUtils.goToNode(classTree, root.findChild("Methods").findChild(method).getPath());
                        }
                    });
                }

                menu.add(new AbstractAction("Find overrides") {
                    @Override public void actionPerformed(ActionEvent e) {
                        // TODO
                    }
                });
            }

            return menu;
        }

        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath[] paths, final Node[] nodes, final ClassGen openClass) {
            JPopupMenu menu = super.createContextMenu(tree, paths, nodes, openClass);


            return menu;
        }
    };

}
