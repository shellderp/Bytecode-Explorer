package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.ui.DefaultTreeContextMenuProvider;
import shellderp.bcexplorer.ui.TreeContextMenuListener;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Tree component used to display search results.
 *
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:38 PM
 */
public class ResultTree extends JTree {

    public ResultTree() {
        addMouseListener(new TreeContextMenuListener(new DefaultTreeContextMenuProvider(), this));
    }

}