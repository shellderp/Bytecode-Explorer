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

            if (args.length == 1) {
                // first argument specifies path to load on startup
                bcExplorer.classHierarchy.loadDirectory(new File[]{new File(args[0])});
            }

            bcExplorer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
