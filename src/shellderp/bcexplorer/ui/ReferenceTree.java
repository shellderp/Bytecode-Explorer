package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.InstructionWrapper;
import shellderp.bcexplorer.Node;
import shellderp.bcexplorer.Reference;

import javax.swing.tree.DefaultTreeModel;
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

    public ReferenceTree(final ClassTabPane classTabPane, final List<Reference> refs) {
        Node root = new Node("References");

        for (Reference ref : refs) {
            Node classNode = root.findChild(ref.getClassGen());
            if (classNode == null) {
                classNode = root.addChild(ref.getClassGen());
                classNode.setDisplayText(ref.getClassGen().getClassName());
            }
            
            ref.addResultTreeNode(classNode);
        }

        setModel(new DefaultTreeModel(root));
        setRootVisible(false);
        SwingUtils.expandAllChildren(this, new TreePath(root), true);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
                    Node node = (Node) getLastSelectedPathComponent();

                    if (node == null || !(node.get() instanceof Reference))
                        return;

                    Reference reference = (Reference) node.get();

                    ClassTree classTree = classTabPane.openClassTab(reference.getClassGen());
                    Node refNode = reference.getReferencedClassNode(classTree);
                    SwingUtils.goToNode(classTree, refNode.getPath());
                }
            }
        });
    }
}

