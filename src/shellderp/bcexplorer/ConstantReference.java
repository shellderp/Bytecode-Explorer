package shellderp.bcexplorer;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.generic.ClassGen;
import shellderp.bcexplorer.ui.ClassTree;

/**
 * Created by: Mike
 * Date: 5/23/12
 * Time: 1:23 AM
 */
public class ConstantReference extends Reference {
    Constant constant;

    public ConstantReference(ClassGen classGen, Constant constant) {
        super(classGen);
        this.constant = constant;
    }

    public Constant getConstant() {
        return constant;
    }

    @Override public Node getReferencedClassNode(ClassTree classTree) {
        return classTree.constants.findChild(constant.getClass().getSimpleName()).findChild(constant);
    }

    @Override public void addResultTreeNode(Node classNode) {
        classNode.addChild(this);
    }

    @Override public String toString() {
        return getClassGen().getConstantPool().getConstantPool().constantToString(constant);
    }
}
