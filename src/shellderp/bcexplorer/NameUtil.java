package shellderp.bcexplorer;

import org.apache.bcel.generic.ClassGen;

/**
 * Created by: Mike
 * Date: 4/9/12
 * Time: 8:08 PM
 */
public class NameUtil {
    
    public static String getSimpleName(ClassGen classGen) {
        String name = classGen.getClassName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            name = name.substring(lastDot + 1);
        }
        return name;
    }
}
