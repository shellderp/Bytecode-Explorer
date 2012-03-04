package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.Node;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeContextMenuListener<T> extends MouseAdapter {
    private TreeContextMenuProvider<T> provider;
    private JTree classTree;
    private T arg;

    public TreeContextMenuListener(TreeContextMenuProvider<T> provider, JTree classTree, T arg) {
        this.provider = provider;
        this.classTree = classTree;
        this.arg = arg;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu contextMenu;

            // Get a list of paths selected by the user.
            // If n > 1, use the selected paths. If n <= 1, use the closest node to the mouse

            TreePath[] userSelectedPaths = classTree.getSelectionPaths();

            if (userSelectedPaths != null && userSelectedPaths.length > 1) {
                Node[] nodes = new Node[userSelectedPaths.length];

                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = (Node) userSelectedPaths[i].getLastPathComponent();
                }

                contextMenu = provider.createContextMenu(classTree, userSelectedPaths, nodes, arg);
            } else {
                TreePath path = classTree.getClosestPathForLocation(e.getX(), e.getY());
                if (path == null)
                    return;

                Node selected = (Node) path.getLastPathComponent();
                classTree.setSelectionPath(path);

                contextMenu = provider.createContextMenu(classTree, path, selected, arg);
            }

            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
