package shellderp.bcexplorer.reference;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Instruction;

/**
 * An interface that provides a method to determine if an instruction is a reference.
 *
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:23 PM
 */
public interface InstructionFilter {

    public boolean filter(ClassGen visitClass, Method visitMethod, Instruction instruction);

}
