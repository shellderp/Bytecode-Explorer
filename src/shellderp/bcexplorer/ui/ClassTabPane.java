package shellderp.bcexplorer.ui;

import org.apache.bcel.generic.*;
import shellderp.bcexplorer.*;
import shellderp.bcexplorer.Reference;

import javax.swing.*;
import java.util.List;

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

    // TODO find suitable location
    public void addReferenceTab(ClassGen cg, Object value, List<Reference> refs) {
        if (refs.isEmpty())
            return;

        ReferenceTree rt = new ReferenceTree(this, refs);
        resultTabPane.addTab("Refs to " + cg.getClassName() + "." + value, new JScrollPane(rt));
        resultTabPane.setSelectedIndex(resultTabPane.getComponentCount() - 1);
    }

}
