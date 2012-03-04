package shellderp.bcexplorer;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.ui.DefaultTreeContextMenuProvider;
import shellderp.bcexplorer.ui.MiddleClickCloseTabListener;
import shellderp.bcexplorer.ui.TreeContextMenuListener;
import shellderp.bcexplorer.ui.TreeContextMenuProvider;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.*;

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
        Node<String> root = new Node<String>("Class " + cg.getClassName() + " extends " + cg.getSuperclassName());

        Node<String> cp = root.addChild("Constant Pool (" + cpgen.getSize() + " entries)");
        for (int i = 0; i < cpgen.getSize(); i++) {
            Constant constant = cpgen.getConstant(i);
            cp.addChild(i + ": " + constant);
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

            for (InstructionHandle ih : list.getInstructionHandles()) {
                String label = ih.getPosition() + " " + ih.getInstruction().toString(cpgen.getConstantPool());
                m.addChild(new InstructionWrapper(ih, method)).setDisplayText(label);
            }
        }

        final JTree classTree = new JTree(root);
        classTree.addMouseListener(new TreeContextMenuListener<ClassGen>(contextMenuProvider, classTree, cg));
        addTab(cg.getClassName(), new JScrollPane(classTree));

        setSelectedIndex(getComponentCount() - 1);

        return classTree;
    }

    private TreeContextMenuProvider<ClassGen> contextMenuProvider = new DefaultTreeContextMenuProvider<ClassGen>() {
        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node, final ClassGen openClass) {
            JPopupMenu menu = super.createContextMenu(tree, path, node, openClass);

            if (node.get() instanceof org.apache.bcel.classfile.FieldOrMethod) {
                menu.addSeparator();

                menu.add(new AbstractAction("Find Local References") {
                    public void actionPerformed(ActionEvent e) {
                        Object value = node.get();
                        Node refs = ClassHierarchy.findReferences(openClass, (FieldOrMethod) value, openClass);
                        if (refs != null) {
                            ReferenceTree rt = new ReferenceTree(ClassTabPane.this, refs);
                            rt.setRootVisible(false);
                            resultTabPane.addTab("Refs to " + value, new JScrollPane(rt));
                            resultTabPane.setSelectedIndex(resultTabPane.getComponentCount() - 1);
                        }
                    }
                });

                menu.add(new AbstractAction("Find Global References") {
                    public void actionPerformed(ActionEvent e) {
                        Object value = node.get();
                        Node refs = classHierarchy.findReferences(openClass, (FieldOrMethod) value);
                        if (refs != null) {
                            ReferenceTree rt = new ReferenceTree(ClassTabPane.this, refs);
                            resultTabPane.addTab("Refs to " + value.toString(), new JScrollPane(rt));
                            resultTabPane.setSelectedIndex(resultTabPane.getComponentCount() - 1);
                        }
                    }
                });

                menu.addSeparator();

                menu.add(new AbstractAction("Refactor name") {
                    public void actionPerformed(ActionEvent e) {
                        // TODO find references, optionally check for conflicts, then update the references to the new name
                        // possibly use bcel to verify modified classes afterwards?
                    }
                });
            }
            if (node.get() instanceof Method) {
                menu.addSeparator();

                final Method method = (Method) node.get();
                Object superDecl = classHierarchy.findSuperDeclaration(openClass, method);
                if (superDecl != null) {
                    menu.add(new AbstractAction("Goto super declaration") {
                        public void actionPerformed(ActionEvent e) {

                        }
                    });
                }
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
