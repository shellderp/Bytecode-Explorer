package shellderp.bcexplorer;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * Created by: Mike
 * Date: 1/25/12
 * Time: 11:13 PM
 */
public interface TreeContextMenuProvider<T> {
    public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node, final T arg);

    public JPopupMenu createContextMenu(final JTree tree, final TreePath[] paths, final Node[] nodes, final T arg);
}
