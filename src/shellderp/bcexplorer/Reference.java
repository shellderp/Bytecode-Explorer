package shellderp.bcexplorer;

import org.apache.bcel.generic.ClassGen;
import shellderp.bcexplorer.ui.ClassTree;

import java.lang.ref.WeakReference;

/**
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:25 PM
 */
public class Reference {
    private WeakReference<ClassGen> classGen;

    public Reference(ClassGen classGen) {
        this.classGen = new WeakReference<ClassGen>(classGen);
    }

    public ClassGen getClassGen() {
        return classGen.get();
    }

    /**
     * Returns the node which this reference points to
     *
     * @param classTree the class tab tree for the ClassGen referenced by this object
     */
    public Node getReferencedClassNode(ClassTree classTree) {
        return (Node) classTree.getModel().getRoot();
    }

    /**
     * Adds this reference to the result tree
     *
     * @param classNode the tree node for the referenced classgen, to which additional nodes should be added
     */
    public void addResultTreeNode(Node classNode) {
        classNode.set(this);
    }

}
