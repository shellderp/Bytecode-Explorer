package shellderp.bcexplorer;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Instruction;

/**
 * An interface that provides a method to determine if an instruction should be included in the filter results.
 *
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:23 PM
 */
public interface InstructionFilter {

    public boolean process(ClassGen visitClass, Method visitMethod, Instruction instruction);

}
