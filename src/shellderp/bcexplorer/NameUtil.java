package shellderp.bcexplorer;

import org.apache.bcel.generic.ClassGen;

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
    
}
