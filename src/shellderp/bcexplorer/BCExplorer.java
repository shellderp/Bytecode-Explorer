package shellderp.bcexplorer;

import javax.swing.*;

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
            bcExplorer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
