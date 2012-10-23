package shellderp.bcexplorer.ui;

import javax.swing.*;

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
