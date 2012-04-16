package shellderp.bcexplorer;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import shellderp.bcexplorer.ui.ClassTree;

/**
 * Created by: Mike
 * Date: 4/15/12
 * Time: 8:47 PM
 */
public class FieldOrMethodReference extends Reference {
    private FieldOrMethod fieldOrMethod;

    public FieldOrMethodReference(ClassGen classGen, FieldOrMethod fieldOrMethod) {
        super(classGen);
        this.fieldOrMethod = fieldOrMethod;
    }

    @Override public Node getReferencedClassNode(ClassTree classTree) {
        if (fieldOrMethod instanceof Field)
            return classTree.fields.findChild(fieldOrMethod);
        return classTree.methods.findChild(fieldOrMethod);
    }

    @Override public void addResultTreeNode(Node classNode) {
        // TODO
    }

    public String toString() {
        return getClassGen().getClassName() + "." + fieldOrMethod.getName();
    }

    public FieldOrMethod getFieldOrMethod() {
        return fieldOrMethod;
    }
}
