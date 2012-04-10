package shellderp.bcexplorer.reference;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.InstructionFilter;

/**
 * Created by: Mike
 * Date: 3/7/12
 * Time: 9:09 PM
 */
public class MethodReferenceFilter implements InstructionFilter {
    private ClassGen containingClass;
    private Method method;

    public MethodReferenceFilter(ClassGen containingClass, Method method) {
        this.containingClass = containingClass;
        this.method = method;
    }

    @Override
    public boolean process(ClassGen visitClass, Method visitMethod, Instruction instruction) {
        if (!(instruction instanceof InvokeInstruction))
            return false;

        InvokeInstruction invoke = (InvokeInstruction) instruction;
        ConstantPoolGen cpgen = visitClass.getConstantPool();

        return invoke.getLoadClassType(cpgen).getClassName().equals(containingClass.getClassName())
                && method.getName().equals(invoke.getMethodName(cpgen))
                && method.getSignature().equals(invoke.getSignature(cpgen));
    }
}
