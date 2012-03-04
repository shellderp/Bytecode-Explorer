package shellderp.bcexplorer;

import shellderp.bcexplorer.ui.DefaultTreeContextMenuProvider;
import shellderp.bcexplorer.ui.TreeContextMenuListener;

import javax.swing.*;
import javax.swing.tree.TreeNode;

/**
 * Tree component used to display search results.
 *
 * Created by: Mike
 * Date: 1/17/12
 * Time: 8:38 PM
 */
public class ResultTree extends JTree {

    public ResultTree(TreeNode root) {
        super(root);

        addMouseListener(new TreeContextMenuListener<Object>(new DefaultTreeContextMenuProvider<Object>(), this, null));
    }

    
}
