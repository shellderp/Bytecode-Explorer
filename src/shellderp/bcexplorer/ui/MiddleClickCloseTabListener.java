package shellderp.bcexplorer.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Provides a mouse listener for JTabbedPanes that closes a tab when one is middle clicked.
 *
 * Created by: Mike
 * Date: 1/17/12
 * Time: 11:37 PM
 */
public class MiddleClickCloseTabListener extends MouseAdapter {
    private JTabbedPane tabbedPane;

    public MiddleClickCloseTabListener(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            int clickedTab = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (clickedTab != -1) {
                tabbedPane.removeTabAt(clickedTab);
            }
        }
    }
}
