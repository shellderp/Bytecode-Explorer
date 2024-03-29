package shellderp.bcexplorer.ui;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Enumeration;

/**
 * Provides swing utility methods
 *
 * Created by: Mike
 * Date: 1/16/12
 * Time: 9:16 PM
 */
public class SwingUtils {

    private SwingUtils() {
    }

    public static void showErrorDialog(Component parent, String title, Object... messageLines) {
        JOptionPane.showMessageDialog(parent, messageLines, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void copyStringToClipboard(String string) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
    }

    public static void expandAllChildren(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAllChildren(tree, path, expand);
            }
        }

        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public static void goToNode(JTree t, TreePath path) {
        t.setSelectionPath(path);
        t.scrollPathToVisible(path);
    }

}
