package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;

/**
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:25 PM
 */
public class Reference {
    public ClassGen classGen;
    public Method method;
    public InstructionHandle ih;

    public Reference(ClassGen classGen, Method method, InstructionHandle ih) {
        this.classGen = classGen;
        this.method = method;
        this.ih = ih;
    }

    public String toString() {
        return ih.getInstruction().toString(classGen.getConstantPool().getConstantPool());
    }
}
