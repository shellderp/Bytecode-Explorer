package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.Node;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * Created by: Mike
 * Date: 1/25/12
 * Time: 11:13 PM
 */
public interface TreeContextMenuProvider<T> {
    public JPopupMenu createContextMenu(JTree tree, TreePath path, Node node, T arg);

    public JPopupMenu createContextMenu(JTree tree, TreePath[] paths, Node[] nodes, T arg);
}
