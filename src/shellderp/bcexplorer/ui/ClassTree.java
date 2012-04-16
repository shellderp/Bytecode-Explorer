package shellderp.bcexplorer.ui;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.*;
import shellderp.bcexplorer.Node;
import shellderp.bcexplorer.reference.FieldReferenceFilter;
import shellderp.bcexplorer.reference.MethodReferenceFilter;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

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
        root.setDisplayText(Utility.accessToString(classGen.getAccessFlags(), true) + " " + Utility.classOrInterface(classGen.getAccessFlags()) + " " + classGen.getClassName() + " extends " + classGen.getSuperclassName());

        Node interfaces = root.addChild("Interfaces");
        for (int constantIndex : classGen.getInterfaces()) {
            Constant constant = cpgen.getConstant(constantIndex);
            interfaces.addChild(constant).setDisplayText(cpgen.getConstantPool().constantToString(constant));
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

        expandPath(constants.getPath());
        expandPath(interfaces.getPath());
        expandPath(fields.getPath());
        expandPath(methods.getPath());
    }

    private DefaultTreeContextMenuProvider contextMenuProvider = new DefaultTreeContextMenuProvider() {
        @Override
        public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node) {
            JPopupMenu menu = super.createContextMenu(tree, path, node);

            if (node.get() instanceof Field) {
                addFieldMenuItems(menu, classGen, (Field) node.get());
            } else if (node.get() instanceof Method) {
                addMethodMenuItems(menu, classGen, (Method) node.get());
            } else if (node.get() instanceof Constant) {
                addConstantMenuItems(menu, (Constant) node.get());
            } else if (node.get() instanceof InstructionWrapper) {
                InstructionHandle iHandle = ((InstructionWrapper) node.get()).instruction;

                if (iHandle.getInstruction() instanceof CPInstruction) {
                    CPInstruction cpInstruction = (CPInstruction) iHandle.getInstruction();
                    addConstantMenuItems(menu, classGen.getConstantPool().getConstant(cpInstruction.getIndex()));
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

    public void addConstantMenuItems(JPopupMenu menu, Constant constant) {
        ConstantPoolGen cpgen = classGen.getConstantPool();
        ConstantPool cp = cpgen.getConstantPool();

        if (constant instanceof ConstantCP) {
            ConstantCP constantCP = (ConstantCP) constant;

            ConstantClass constantClass = (ConstantClass) cpgen.getConstant(constantCP.getClassIndex());
            ConstantNameAndType nameAndType = (ConstantNameAndType) cpgen.getConstant(constantCP.getNameAndTypeIndex());
            final String refClassName = Utility.compactClassName((String) constantClass.getConstantValue(cp), false);
            if (addClassMenuItems(menu, refClassName)) {
                final FieldOrMethodReference reference = classHierarchy.findFieldOrMethod(refClassName, nameAndType.getName(cp), nameAndType.getSignature(cp));
                if (reference == null)
                    return;

                FieldOrMethod fieldOrMethod = reference.getFieldOrMethod();
                JMenu submenu = new JMenu(fieldOrMethod.getName());
                submenu.add(new AbstractAction("Go to declaration") {
                    @Override public void actionPerformed(ActionEvent e) {
                        ClassTree classTree = classTabPane.openClassTab(reference.getClassGen());
                        SwingUtils.goToNode(classTree, reference.getReferencedClassNode(classTree).getPath());
                    }
                });
                if (fieldOrMethod instanceof Field) {
                    addFieldMenuItems(submenu.getPopupMenu(), reference.getClassGen(), (Field) fieldOrMethod);
                } else {
                    addMethodMenuItems(submenu.getPopupMenu(), reference.getClassGen(), (Method) fieldOrMethod);
                }
                menu.add(submenu);
            }
        } else if (constant instanceof ConstantClass) {
            ConstantClass constantClass = (ConstantClass) constant;
            String className = Utility.compactClassName((String) constantClass.getConstantValue(cp), false);
            addClassMenuItems(menu, className);
        }
    }

    public boolean addClassMenuItems(JPopupMenu menu, final String className) {
        menu.addSeparator();

        if (!classHierarchy.classes.containsKey(className)) {
            JMenu submenu = new JMenu("Class '" + NameUtil.getSimpleName(className) + "' not loaded");
            submenu.add(new AbstractAction("Attempt load from classpath") {
                @Override public void actionPerformed(ActionEvent e) {
                    try {
                        ClassGen cg = new ClassGen(Repository.lookupClass(className));
                        classHierarchy.loadClasses(Collections.singletonList(cg));
                    } catch (ClassNotFoundException ex) {
                        System.err.println("class not found in repository: " + className);
                    }
                }
            });
            menu.add(submenu);

            return false;
        }

        JMenu submenu = new JMenu("Class '" + NameUtil.getSimpleName(className) + "'");
        submenu.add("Open");
        submenu.add("Show in tree");
        submenu.add("Find references");
        submenu.add("Unload");
        menu.add(submenu);

        return true;
    }

    public void addMethodMenuItems(JPopupMenu menu, final ClassGen methodClassGen, final Method method) {
        menu.addSeparator();

        menu.add(new AbstractAction("Find Local References") {
            @Override public void actionPerformed(ActionEvent e) {
                classTabPane.addReferenceTab(methodClassGen, method, ClassHierarchy.findReferences(classGen, new MethodReferenceFilter(methodClassGen, method)));
            }
        });

        menu.add(new AbstractAction("Find Global References") {
            @Override public void actionPerformed(ActionEvent e) {
                classTabPane.addReferenceTab(methodClassGen, method, classHierarchy.findReferences(new MethodReferenceFilter(methodClassGen, method)));
            }
        });

        menu.addSeparator();

        final Node<ClassGen> superDecl = classHierarchy.findSuperDeclaration(methodClassGen, method);
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
    }

    public void addFieldMenuItems(JPopupMenu menu, final ClassGen fieldClassGen, final Field field) {
        menu.addSeparator();

        menu.add(new AbstractAction("Find Local References") {
            @Override public void actionPerformed(ActionEvent e) {
                classTabPane.addReferenceTab(fieldClassGen, field, ClassHierarchy.findReferences(classGen, new FieldReferenceFilter(fieldClassGen, field)));
            }
        });

        menu.add(new AbstractAction("Find Global References") {
            @Override public void actionPerformed(ActionEvent e) {
                classTabPane.addReferenceTab(fieldClassGen, field, classHierarchy.findReferences(new FieldReferenceFilter(fieldClassGen, field)));
            }
        });
    }
}
