package shellderp.bcexplorer;

import javax.swing.*;
import java.io.File;

/**
 * User: Mike
 * Date: 1/16/12
 * Time: 9:11 PM
 */
public class BCExplorer {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            BCExplorerFrame bcExplorer = new BCExplorerFrame();

            // arguments specify path(s) to load on startup
            for (String arg : args) {
                bcExplorer.classHierarchy.loadDirectory(new File(arg));
            }

            bcExplorer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
