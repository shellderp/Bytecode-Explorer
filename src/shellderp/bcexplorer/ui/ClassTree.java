package shellderp.bcexplorer.ui;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.*;
import shellderp.bcexplorer.SwingUtils;
import shellderp.bcexplorer.reference.FieldReferenceFilter;
import shellderp.bcexplorer.reference.MethodReferenceFilter;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by: Mike
 * Date: 4/9/12
 * Time: 4:11 PM
 */
public class ClassTree extends JTree {
    ClassGen classGen;
    public Node constants, fields, methods;

    ClassTabPane classTabPane;
    ClassHierarchy classHierarchy;

    public ClassTree(ClassGen classGen, ClassTabPane classTabPane, ClassHierarchy classHierarchy) {
        super();

        this.classGen = classGen;
        this.classTabPane = classTabPane;
        this.classHierarchy = classHierarchy;

        ConstantPoolGen cpgen = classGen.getConstantPool();
        Node root = new Node<>(classGen);
        root.setDisplayText("Class " + classGen.getClassName() + " extends " + classGen.getSuperclassName());

        Node interfaces = root.addChild("Interfaces");
        for (String iface : classGen.getInterfaceNames()) {
            interfaces.addChild(iface); // TODO lookup interface, visually distinguish if unloaded
        }

        constants = root.addChild("Constant Pool (" + cpgen.getSize() + " entries)");
        for (int i = 0; i < cpgen.getSize(); i++) {
            Constant constant = cpgen.getConstant(i);
            if (constant == null)
                continue;

            String type = constant.getClass().getSimpleName();
            Node typeNode = constants.findChild(type);
            if (typeNode == null)
                typeNode = constants.addChild(type);

            typeNode.addChild(constant).setDisplayText(i + ": " + cpgen.getConstantPool().constantToString(constant));
        }

        fields = root.addChild("Fields");
        for (Field field : classGen.getFields()) {
            fields.addChild(field);
        }

        methods = root.addChild("Methods");
        for (Method method : classGen.getMethods()) {
            Node m = methods.addChild(method);

            InstructionList list = new MethodGen(method, classGen.getClassName(), cpgen).getInstructionList();
            if (list == null)
                continue;

            int posMaxDigits = String.valueOf(list.getEnd().getPosition()).length();
            for (InstructionHandle ih : list.getInstructionHandles()) {
                String label = String.format("%" + posMaxDigits + "d: %s", ih.getPosition(), ih.getInstruction().toString(cpgen.getConstantPool()));
                m.addChild(new InstructionWrapper(ih, method)).setDisplayText(label);
            }
        }

        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        addMouseListener(new TreeContextMenuListener(contextMenuProvider, this));

        super.setModel(new DefaultTreeModel(root));

        expandPath(fields.getPath());
        expandPath(methods.getPath());
    }

    private DefaultTreeContextMenuProvider contextMenuProvider = new DefaultTreeContextMenuProvider() {
        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node) {
            JPopupMenu menu = super.createContextMenu(tree, path, node);

            if (node.get() instanceof Field) {
                menu.addSeparator();

                final Field field = (Field) node.get();

                menu.add(new AbstractAction("Find Local References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        classTabPane.addReferenceTab(classGen, field, ClassHierarchy.findReferences(classGen, new FieldReferenceFilter(classGen, field)));
                    }
                });

                menu.add(new AbstractAction("Find Global References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        classTabPane.addReferenceTab(classGen, field, classHierarchy.findReferences(new FieldReferenceFilter(classGen, field)));
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
                        classTabPane.addReferenceTab(classGen, method, ClassHierarchy.findReferences(classGen, new MethodReferenceFilter(classGen, method)));
                    }
                });

                menu.add(new AbstractAction("Find Global References") {
                    @Override public void actionPerformed(ActionEvent e) {
                        classTabPane.addReferenceTab(classGen, method, classHierarchy.findReferences(new MethodReferenceFilter(classGen, method)));
                    }
                });

                menu.addSeparator();

                final Node<ClassGen> superDecl = classHierarchy.findSuperDeclaration(classGen, method);
                if (superDecl != null) {
                    menu.add(new AbstractAction("Go to super declaration") {
                        @Override public void actionPerformed(ActionEvent e) {
                            ClassTree classTree = classTabPane.openClassTab(superDecl.get());

                            SwingUtils.goToNode(classTree, classTree.methods.findChild(method).getPath());
                        }
                    });
                }

                menu.add(new AbstractAction("Find overrides") {
                    @Override public void actionPerformed(ActionEvent e) {
                        // TODO
                    }
                });
            } else if (node.get() instanceof InstructionWrapper) {
                InstructionHandle iHandle = ((InstructionWrapper) node.get()).instruction;

                if (iHandle.getInstruction() instanceof CPInstruction) {
                    CPInstruction cp = (CPInstruction) iHandle.getInstruction();
                    ConstantPoolGen cpgen = classGen.getConstantPool();
                    System.out.println(cp.getType(cpgen));
                    Constant c = cpgen.getConstant(cp.getIndex());
                    System.out.println(cpgen.getConstantPool().constantToString(c));
                    System.out.println(c.getClass().getSimpleName());

                }
            }

            return menu;
        }

        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath[] paths, final Node[] nodes) {
            JPopupMenu menu = super.createContextMenu(tree, paths, nodes);

            return menu;
        }
    };
}
