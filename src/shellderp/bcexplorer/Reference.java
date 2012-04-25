package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import shellderp.bcexplorer.ui.ClassTree;

import java.lang.ref.WeakReference;

/**
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:25 PM
 */
public abstract class Reference {
    private WeakReference<ClassGen> classGen;

    public Reference(ClassGen classGen) {
        this.classGen = new WeakReference<ClassGen>(classGen);
    }

    public ClassGen getClassGen() {
        return classGen.get();
    }

    /** Returns the node which this reference points to
     * @param classTree the class tab tree for the ClassGen referenced by this object
     */
    public abstract Node getReferencedClassNode(ClassTree classTree);

    /**
     * Adds this reference to the result tree
     * @param classNode the tree node for the referenced classgen, to which additional nodes should be added
     */
    public abstract void addResultTreeNode(Node classNode);
}
