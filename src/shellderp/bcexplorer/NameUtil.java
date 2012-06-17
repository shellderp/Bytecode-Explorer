package shellderp.bcexplorer;

import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;

/**
 * Created by: Mike
 * Date: 4/9/12
 * Time: 8:08 PM
 */
public class NameUtil {

    public static String getSimpleName(ClassGen classGen) {
        return getSimpleName(classGen.getClassName());
    }

    public static String getSimpleName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot != -1) {
            qualifiedName = qualifiedName.substring(lastDot + 1);
        }
        return qualifiedName;
    }

    public static String getSimpleArgumentString(String signature) {
        StringBuilder sb = new StringBuilder("(");

        String[] args = Utility.methodSignatureArgumentTypes(signature);
        boolean first = true;
        for (String arg : args) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(NameUtil.getSimpleName(arg));
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }
    
    public static Type reduceArrayType(Type type) {
        while (type instanceof ArrayType) {
            type = ((ArrayType) type).getElementType();
        }
        return type;
    }

}
