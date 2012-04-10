package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.Node;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeContextMenuListener extends MouseAdapter {
    private TreeContextMenuProvider provider;
    private JTree tree;

    public TreeContextMenuListener(TreeContextMenuProvider provider, JTree tree) {
        this.provider = provider;
        this.tree = tree;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu contextMenu;

            // Get a list of paths selected by the user.
            // If n > 1, use the selected paths. If n <= 1, use the closest node to the mouse

            TreePath[] userSelectedPaths = tree.getSelectionPaths();

            if (userSelectedPaths != null && userSelectedPaths.length > 1) {
                Node[] nodes = new Node[userSelectedPaths.length];

                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = (Node) userSelectedPaths[i].getLastPathComponent();
                }

                contextMenu = provider.createContextMenu(tree, userSelectedPaths, nodes);
            } else {
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                if (path == null)
                    return;

                Node selected = (Node) path.getLastPathComponent();
                tree.setSelectionPath(path);

                contextMenu = provider.createContextMenu(tree, path, selected);
            }

            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
