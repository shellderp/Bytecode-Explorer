package shellderp.bcexplorer;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by: Mike
 * Date: 10/17/12
 * Time: 10:23 PM
 */
public class NameUtilTest {

    private ClassGen cgString;

    @Before
    public void setUp() throws Exception {
        cgString = new ClassGen(Repository.lookupClass("java.lang.String"));
    }

    @Test
    public void testGetSimpleName() throws Exception {
        assertThat(NameUtil.getSimpleName(cgString), is("String"));
        assertThat(NameUtil.getSimpleName("generic.package.name.ClassName"), is("ClassName"));
    }

    @Test
    public void testGetSimpleArgumentString() throws Exception {
        Method methodCopyValueOf = cgString.containsMethod("copyValueOf", "([CII)Ljava/lang/String;");
        Method methodFormat = cgString.containsMethod("format",
                "(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");

        assertThat(NameUtil.getSimpleArgumentString(methodCopyValueOf.getSignature()), is("(char[], int, int)"));
        assertThat(NameUtil.getSimpleArgumentString(methodFormat.getSignature()), is("(Locale, String, Object[])"));
    }

    @Test
    public void testReduceArrayType() throws Exception {
        Type typeObject = new ObjectType("java.lang.Object");
        assertThat(NameUtil.reduceArrayType(typeObject), is(typeObject));
        Type typeObject1 = new ArrayType("java.lang.Object", 1);
        Type typeObject2 = new ArrayType("java.lang.Object", 2);
        Type typeObject3 = new ArrayType("java.lang.Object", 3);
        assertThat(NameUtil.reduceArrayType(typeObject1), is(typeObject));
        assertThat(NameUtil.reduceArrayType(typeObject2), is(typeObject));
        assertThat(NameUtil.reduceArrayType(typeObject3), is(typeObject));
    }
}
