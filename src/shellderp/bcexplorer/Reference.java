package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import shellderp.bcexplorer.ui.ClassTree;

/**
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:25 PM
 */
public abstract class Reference {
    private ClassGen classGen;

    public Reference(ClassGen classGen) {
        this.classGen = classGen;
    }

    public ClassGen getClassGen() {
        return classGen;
    }
    
    public abstract Node getReferencedClassNode(ClassTree classTree);

    public abstract void addResultTreeNode(Node classNode);
}
