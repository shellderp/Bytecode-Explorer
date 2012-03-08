package shellderp.bcexplorer;

import shellderp.bcexplorer.reference.Reference;
import shellderp.bcexplorer.ui.SwingUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Models reference search results.
 * <p/>
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:38 PM
 */
public class ReferenceTree extends ResultTree {

    public ReferenceTree(final ClassTabPane classTabPane, Object value, final List<Reference> refs) {
        Node root = new Node("Ref's to " + value);

        for (Reference ref : refs) {
            Node classNode = root.findChild(ref.classGen);
            if (classNode == null) {
                classNode = root.addChild(ref.classGen);
                classNode.setDisplayText(ref.classGen.getClassName());
            }

            Node methodNode = classNode.findChild(ref.method);
            if (methodNode == null) {
                methodNode = classNode.addChild(ref.method);
            }

            methodNode.addChild(ref);
        }

        ((DefaultTreeModel) getModel()).setRoot(root);

        SwingUtils.expandAllChildren(this, new TreePath(root), true);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
                    Node node = (Node) getLastSelectedPathComponent();

                    if (node == null || !(node.get() instanceof Reference))
                        return;

                    Reference value = (Reference) node.get();

                    JTree classTree = classTabPane.openClassTab(value.classGen);
                    Node root = (Node) classTree.getModel().getRoot();
                    Node inode = root.findChild("Methods").findChild(value.method).findChild(new InstructionWrapper(value.ih, value.method));
                    SwingUtils.goToNode(classTree, inode.getPath());
                }
            }
        });
    }
}

