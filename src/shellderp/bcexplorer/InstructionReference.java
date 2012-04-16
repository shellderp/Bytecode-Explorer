package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import shellderp.bcexplorer.ui.ClassTree;

/**
 * Created by: Mike
 * Date: 4/15/12
 * Time: 8:46 PM
 */
public class InstructionReference extends Reference {
    private Method method;
    private InstructionHandle ih;

    public InstructionReference(ClassGen classGen, Method method, InstructionHandle ih) {
        super(classGen);
        this.ih = ih;
        this.method = method;
    }

    @Override public Node getReferencedClassNode(ClassTree classTree) {
        return classTree.methods.findChild(method).findChild(new InstructionWrapper(ih, method));
    }

    @Override public void addResultTreeNode(Node classNode) {
        Node methodNode = classNode.findChild(method);
        if (methodNode == null) {
            methodNode = classNode.addChild(method);
        }

        methodNode.addChild(this);
    }

    public String toString() {
        return ih.getInstruction().toString(getClassGen().getConstantPool().getConstantPool());
    }
}
