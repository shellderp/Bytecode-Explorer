package shellderp.bcexplorer.ui;

import org.apache.bcel.generic.*;
import shellderp.bcexplorer.*;

import javax.swing.*;

/**
 * Provides a tabbed pane that displays open classes and their members.
 * <p/>
 * Created by: Mike
 * Date: 1/15/12
 * Time: 11:22 PM
 */
public class ClassTabPane extends JTabbedPane {
    private ClassHierarchy classHierarchy;
    private JTabbedPane resultTabPane;

    public ClassTabPane(ClassHierarchy classHierarchy, JTabbedPane resultTabPane) {
        super();

        this.classHierarchy = classHierarchy;
        this.resultTabPane = resultTabPane;

        addMouseListener(new MiddleClickCloseTabListener(this));
        addMouseListener(new TabContextMenuListener(this));
    }

    public ClassTree openClassTab(final ClassGen cg) {
        for (int i = getTabCount() - 1; i >= 0; i--) {
            JScrollPane scrollPane = (JScrollPane) getComponentAt(i);
            ClassTree classTree = (ClassTree) scrollPane.getViewport().getView();
            if (classTree.classGen.equals(cg)) {
                setSelectedIndex(i);
                return classTree;
            }
        }
        
        ClassTree classTree = new ClassTree(cg, this, classHierarchy);

        addTab(NameUtil.getSimpleName(cg), null, new JScrollPane(classTree), cg.getClassName());

        setSelectedIndex(getComponentCount() - 1);

        return classTree;
    }

    public void closeClassTab(String className) {
        for (int i = getTabCount() - 1; i >= 0; i--) {
            JScrollPane scrollPane = (JScrollPane) getComponentAt(i);
            ClassTree classTree = (ClassTree) scrollPane.getViewport().getView();
            if (className.equals(classTree.classGen.getClassName())) {
                removeTabAt(i);
                return;
            }
        }
    }

    public void addResultTab(String tabTitle, ResultTree resultTree) {
        resultTabPane.addTab(tabTitle, new JScrollPane(resultTree));
        resultTabPane.setSelectedIndex(resultTabPane.getComponentCount() - 1);
    }

}
