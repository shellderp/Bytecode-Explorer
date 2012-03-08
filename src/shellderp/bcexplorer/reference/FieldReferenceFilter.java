package shellderp.bcexplorer.reference;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

/**
 * Created by: Mike
 * Date: 3/5/12
 * Time: 9:34 PM
 */
public class FieldReferenceFilter implements InstructionFilter {
    private ClassGen containingClass;
    private Field field;

    public FieldReferenceFilter(ClassGen containingClass, Field field) {
        this.containingClass = containingClass;
        this.field = field;
    }

    @Override
    public boolean filter(ClassGen visitClass, Method visitMethod, Instruction instruction) {
        if (!(instruction instanceof FieldInstruction))
            return false;
        
        FieldInstruction fi = (FieldInstruction) instruction;
        ConstantPoolGen cpgen = visitClass.getConstantPool();
        ObjectType loadType = fi.getLoadClassType(cpgen);

        return loadType.getClassName().equals(containingClass.getClassName())
                && fi.getFieldName(cpgen).equals(field.getName())
                && fi.getFieldType(cpgen).equals(field.getType());
    }
}
