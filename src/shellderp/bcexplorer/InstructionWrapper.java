package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

public class InstructionWrapper {
    InstructionHandle instruction;
    Method method;

    InstructionWrapper(InstructionHandle instruction, Method method) {
        this.instruction = instruction;
        this.method = method;
    }

    public boolean equals(Object o) {
        if (o instanceof InstructionWrapper) {
            InstructionWrapper other = (InstructionWrapper) o;
            return method.equals(other.method) && instruction.getInstruction().equals(other.instruction.getInstruction());
        }
        return false;
    }
}
