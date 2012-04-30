package shellderp.bcexplorer.ui;

import shellderp.bcexplorer.Node;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by: Mike
 * Date: 4/29/12
 * Time: 9:36 PM
 */
public class TabContextMenuListener extends MouseAdapter {
    private JTabbedPane tabbedPane;

    public TabContextMenuListener(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            final int index = tabbedPane.indexAtLocation(e.getX(), e.getY());

            if (index == -1)
                return;

            JPopupMenu contextMenu = new JPopupMenu();
            contextMenu.add(new AbstractAction("Close") {
                @Override public void actionPerformed(ActionEvent e) {
                    tabbedPane.removeTabAt(index);
                }
            });
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
