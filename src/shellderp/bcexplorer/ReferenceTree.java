package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import shellderp.bcexplorer.ui.SwingUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Models reference search results.
 *
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:38 PM
 */
public class ReferenceTree extends ResultTree {

    public ReferenceTree(final ClassTabPane classTabPane, final Node<Reference> refs) {
        super(refs);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
                    Node node = (Node) getLastSelectedPathComponent();
                    if (node != null && node.get() instanceof Reference) {
                        Reference value = (Reference) node.get();
                        if (value != null) {
                            JTree classTree = classTabPane.openClassTab(value.cg);
                            TreePath path = new TreePath(classTree.getModel().getRoot());
                            path = path.pathByAddingChild(((Node) path.getLastPathComponent()).findChild("Methods"));
                            Node methodNode = (Node) path.getLastPathComponent();
                            path = path.pathByAddingChild(methodNode.findChild(value.method));
                            methodNode = (Node) path.getLastPathComponent();
                            path = path.pathByAddingChild(methodNode.findChild(new InstructionWrapper(value.ih, value.method)));
                            SwingUtils.goToNode(classTree, path);
                        }
                    }
                }
            }
        });
    }
}

class Reference {
    ClassGen cg;
    Method method;
    InstructionHandle ih;

    public Reference(ClassGen cg, Method method, InstructionHandle ih) {
        this.cg = cg;
        this.method = method;
        this.ih = ih;
    }

    public String toString() {
        return ih.getInstruction().toString(cg.getConstantPool().getConstantPool());
    }
}