// Copyright (c) 2010, Donald Strong.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.

package au.com.illyrian.classmaker.members;

import java.util.Vector;

import org.mozilla.classfile.ByteCode;

import au.com.illyrian.classmaker.CallStack;
import au.com.illyrian.classmaker.ClassMaker;
import au.com.illyrian.classmaker.ClassMakerException;
import au.com.illyrian.classmaker.ClassMakerFactory;
import au.com.illyrian.classmaker.ClassMakerTestCase;
import au.com.illyrian.classmaker.types.ClassType;
import au.com.illyrian.classmaker.types.Type;

public class MakerResolveMethodTest extends ClassMakerTestCase
{
    ClassMakerFactory factory;
    ClassMaker maker;
    MethodResolver resolver;
    ClassType DESSERT;
    ClassType CAKE;
    ClassType SCONE;
    ClassType CHOCOLATE_CAKE;
    ClassType BUTTERED_SCONE;


    public void setUp() throws Exception
    {
        factory = new MyClassMakerFactory();
        maker = factory.createClassMaker();
        maker.setFullyQualifiedClassName("MyClass");
        maker.Extends(Object.class);
        maker.setSourceFilename("MyClass.java");
        defaultConstructor(maker);
        resolver = factory.getMethodResolver();
    }

    class MyClassMakerFactory extends ClassMakerFactory
    {
        MyClassMakerFactory()
        {
            super();
            addLocalClasses();
        }
        protected void addLocalClasses()
        {
            DESSERT = addClassType("test.Dessert", ClassMaker.OBJECT_TYPE);
            CAKE    = addClassType("test.Cake", DESSERT);
            SCONE   = addClassType("test.Scone", DESSERT);
            CHOCOLATE_CAKE = addClassType("test.ChocolateCake", CAKE);
            BUTTERED_SCONE = addClassType("test.ButteredScone", SCONE);
        }
    }

    // Generate default constructor
    public void defaultConstructor(ClassMaker maker) throws Exception
    {
        maker.Method("<init>", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Begin();
          maker.Init(maker.Super(), null);
        maker.Return();
        maker.End();
    }

    public void forwardPrimitiveMethods(Type type) throws Exception
    {
        maker.Method("eval", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Forward();

        maker.Method("eval", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", type, 0);
        maker.Forward();

        maker.Method("eval", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", type, 0);
        maker.Declare("y", type, 0);
        maker.Forward();
    }

	public void testResolveIntMethods() throws Exception
    {
    	forwardPrimitiveMethods(ClassMaker.INT_TYPE);
        Vector<MakerMethod> methods = resolver.findMethods(maker.getDeclaredMethods(), "eval");
        assertEquals("Wrong number of methods found.", 3, methods.size());
        assertEquals("void eval()", methods.get(0).toString());
        assertEquals("void eval(int)", methods.get(1).toString());
        assertEquals("void eval(int, int)", methods.get(2).toString());

        MakerMethod method;
        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push());
        assertEquals("Did not find eval()", 0, method.getFormalParams().length);

        // Check assignment of primitives
        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push(ClassMaker.INT_TYPE));
        assertEquals("Did not find eval(int)", 1, method.getFormalParams().length);
        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push(ClassMaker.BYTE_TYPE));
        assertEquals("Did not find eval(int)", 1, method.getFormalParams().length);
        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push(ClassMaker.SHORT_TYPE));
        assertEquals("Did not find eval(int)", 1, method.getFormalParams().length);
        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push(ClassMaker.CHAR_TYPE));
        assertEquals("Did not find eval(int)", 1, method.getFormalParams().length);

        failToResolveMethod("eval", maker.Push(ClassMaker.FLOAT_TYPE));
        failToResolveMethod("eval", maker.Push(ClassMaker.DOUBLE_TYPE));
        failToResolveMethod("eval", maker.Push(ClassMaker.LONG_TYPE));

        method = maker.resolveMethod(maker.getClassType(), "eval", maker.Push(ClassMaker.INT_TYPE).Push(ClassMaker.INT_TYPE));
        assertEquals("Did not find eval(int,int)", 2, method.getFormalParams().length);
    }

    public void testResolveReferenceMethods() throws Exception
    {
        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", DESSERT, 0);
        maker.Declare("y", SCONE, 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", CAKE, 0);
        maker.Declare("y", DESSERT, 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", CHOCOLATE_CAKE, 0);
        maker.Declare("y", SCONE, 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", CHOCOLATE_CAKE, 0);
        maker.Declare("y", ClassMaker.INT_TYPE, 0);
        maker.Forward();

        MakerMethod method;
        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(DESSERT).Push(SCONE));
        assertEquals("Did not resolve method: ", "void moorge(test.Dessert, test.Scone)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(CHOCOLATE_CAKE).Push(DESSERT));
        assertEquals("Did not resolve method: ", "void moorge(test.Cake, test.Dessert)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(CHOCOLATE_CAKE).Push(BUTTERED_SCONE));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake, test.Scone)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(CHOCOLATE_CAKE).Push(ClassMaker.BYTE_TYPE));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake, int)", method.toString());

    	failToResolveMethod("moorge", maker.Push(CAKE).Push(SCONE));

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(DESSERT).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", "void moorge(test.Dessert, test.Scone)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(SCONE));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake, test.Scone)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(CHOCOLATE_CAKE).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake, test.Scone)", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake, test.Scone)", method.toString());

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", ClassMaker.OBJECT_TYPE, 0);
        maker.Declare("y", ClassMaker.OBJECT_TYPE, 0);
        maker.Forward();

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(DESSERT).Push(DESSERT));
        assertEquals("Did not resolve method: ", "void moorge(java.lang.Object, java.lang.Object)", method.toString());
    }

    public void testResolveArrayReferenceMethods() throws Exception
    {
        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", maker.ArrayOf(DESSERT), 0);
        maker.Declare("y", maker.ArrayOf(SCONE), 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", maker.ArrayOf(CAKE), 0);
        maker.Declare("y", maker.ArrayOf(DESSERT), 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", maker.ArrayOf(CHOCOLATE_CAKE), 0);
        maker.Declare("y", maker.ArrayOf(SCONE), 0);
        maker.Forward();

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", maker.ArrayOf(CHOCOLATE_CAKE), 0);
        maker.Declare("y", maker.ArrayOf(ClassMaker.INT_TYPE), 0);
        maker.Forward();

        MakerMethod method;
        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(DESSERT)).Push(maker.ArrayOf(SCONE)));
        assertEquals("Did not resolve method: ", "void moorge(test.Dessert[], test.Scone[])", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(CHOCOLATE_CAKE)).Push(maker.ArrayOf(DESSERT)));
        assertEquals("Did not resolve method: ", "void moorge(test.Cake[], test.Dessert[])", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(CHOCOLATE_CAKE)).Push(maker.ArrayOf(BUTTERED_SCONE)));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake[], test.Scone[])", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(CHOCOLATE_CAKE)).Push(maker.ArrayOf(ClassMaker.INT_TYPE)));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake[], int[])", method.toString());

    	failToResolveMethod("moorge", maker.Push(maker.ArrayOf(CHOCOLATE_CAKE)).Push(maker.ArrayOf(ClassMaker.BYTE_TYPE)));

    	failToResolveMethod("moorge", maker.Push(maker.ArrayOf(CAKE)).Push(maker.ArrayOf(SCONE)));

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(DESSERT)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", "void moorge(test.Dessert[], test.Scone[])", method.toString());

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(maker.ArrayOf(SCONE)));
        assertEquals("Did not resolve method: ", "void moorge(test.ChocolateCake[], test.Scone[])", method.toString());

    	failToResolveMethod("moorge", maker.Push(maker.ArrayOf(CAKE)).Push(ClassMaker.NULL_TYPE));

    	failToResolveMethod("moorge", maker.Push(ClassMaker.NULL_TYPE).Push(ClassMaker.NULL_TYPE));

        maker.Method("moorge", ClassMaker.VOID_TYPE, ByteCode.ACC_PUBLIC);
        maker.Declare("x", ClassMaker.OBJECT_TYPE, 0);
        maker.Declare("y", ClassMaker.OBJECT_TYPE, 0);
        maker.Forward();

        method = maker.resolveMethod(maker.getClassType(), "moorge", maker.Push(maker.ArrayOf(DESSERT)).Push(maker.ArrayOf(DESSERT)));
        assertEquals("Did not resolve method: ", "void moorge(java.lang.Object, java.lang.Object)", method.toString());
    }

    class Dessert {}
    class Cake extends Dessert {}
    class Scone extends Dessert {}
    class ChocolateCake extends Cake {}
    class ButteredScone extends Scone {}
    public interface MoorgeInterface
    {
        public void moorge(Dessert d, Scone s);
        public void moorge(Cake c, Dessert d);
        public void moorge(ChocolateCake cc, Scone s);
        public void moorge(ChocolateCake cc, int x);
    }
    public static final String MOORGE_CHOC_INT      = "void moorge(au.com.illyrian.classmaker.members.MakerResolveMethodTest$ChocolateCake, int)";
    public static final String MOORGE_DESSERT_SCONE = "void moorge(au.com.illyrian.classmaker.members.MakerResolveMethodTest$Dessert, au.com.illyrian.classmaker.members.MakerResolveMethodTest$Scone)";
    public static final String MOORGE_CHOC_SCONE    = "void moorge(au.com.illyrian.classmaker.members.MakerResolveMethodTest$ChocolateCake, au.com.illyrian.classmaker.members.MakerResolveMethodTest$Scone)";
    public static final String MOORGE_CAKE_DESSERT  = "void moorge(au.com.illyrian.classmaker.members.MakerResolveMethodTest$Cake, au.com.illyrian.classmaker.members.MakerResolveMethodTest$Dessert)";

    public Type type(Class javaClass)
    {
        return  factory.classToType(javaClass);
    }

    public void testResolveMethod()
    {
        int i = 0;
        MethodResolver resolver = factory.getMethodResolver();
        ClassType classType = new ClassType("Test", "LTest;", null);
        classType.setJavaClass(MoorgeInterface.class);
        MakerMethod [] methods = factory.getMethods(classType);
        String name = "moorge";
        CallStack actualParameters =
            maker.Push(type(ChocolateCake.class)).Push(type(ButteredScone.class));
        Vector<MakerMethod> candidates = resolver.findMethods(methods, name);
//        public void moorge(Dessert d, Scone s);
//        public void moorge(Cake c, Dessert d);
//        public void moorge(ChocolateCake cc, Scone s);
//        public void moorge(ChocolateCake cc, int x);
        assertEquals("findMethods", 4, candidates.size());
        methods = candidates.toArray(ClassMakerFactory.METHOD_ARRAY);
        assertContains("Expected method", MOORGE_CHOC_INT, methods);
        assertContains("Expected method", MOORGE_CHOC_SCONE, methods);
        assertContains("Expected method", MOORGE_CAKE_DESSERT, methods);
        assertContains("Expected method", MOORGE_DESSERT_SCONE, methods);

        //      Remove :   public void moorge(ChocolateCake cc, int x);
        resolver.removeIncompatableCandidates(candidates, actualParameters);
        assertEquals("removeIncompatableCandidates", 3, candidates.size());
        methods = candidates.toArray(ClassMakerFactory.METHOD_ARRAY);
        i=0;
        assertContains("Expected method", MOORGE_CHOC_SCONE, methods);
        assertContains("Expected method", MOORGE_CAKE_DESSERT, methods);
        assertContains("Expected method", MOORGE_DESSERT_SCONE, methods);

        resolver.removeLessSpecificCandidates(candidates);
        assertEquals("removeLessSpecificCandidates", 1, candidates.size());

        resolver.removeLessSpecificReturnType(candidates);
        assertEquals("removeLessSpecificReturnType", 1, candidates.size());

        assertEquals("Should be one candidate", 1, candidates.size());

        MakerMethod method = candidates.firstElement();
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

    }
    
    private void assertContains(String msg, String target, Object [] values)
    {
        StringBuffer buff = new StringBuffer();
        for (int i=0; i<values.length; i++)
            if (target.equals(values[i].toString()))
                return;
            else
                buff.append("\n").append(values[i]);
        fail(msg + ": " + target + buff.toString());
    }

    public void testResolveJavaInterfaceMethods() throws Exception
    {
        ClassType moorge = maker.classToClassType(MoorgeInterface.class);

        MakerMethod method;
        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert.class)).Push(type(Scone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(Dessert.class)));
        assertEquals("Did not resolve method: ", toString("moorge", Cake.class, Dessert.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(ButteredScone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(byte.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, int.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert.class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(type(Scone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        try {
            maker.resolveMethod(moorge, "dummy", null);
            fail("Should throw ClassMakerException");
        } catch (ClassMakerException ex) {
            assertEquals("Class contains no method called \'dummy\'", ex.getMessage());
        }
    }

    public class MoorgeClass
    {
        public void moorge(Dessert d, Scone s) {}
        public void moorge(Cake c, Dessert d){}
        public void moorge(ChocolateCake cc, Scone s){}
        public void moorge(ChocolateCake cc, int x){}
    }

    public void testResolveJavaClassMethods() throws Exception
    {
        ClassType moorge = maker.classToClassType(MoorgeClass.class);

        MakerMethod method;
        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert.class)).Push(type(Scone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(Dessert.class)));
        assertEquals("Did not resolve method: ", toString("moorge", Cake.class, Dessert.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(ButteredScone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(type(byte.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, int.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert.class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(type(Scone.class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake.class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake.class, Scone.class), method.toString());

        try {
            maker.resolveMethod(moorge, "dummy", null);
            fail("Should throw ClassMakerException");
        } catch (ClassMakerException ex) {
            assertEquals("Class contains no method called \'dummy\'", ex.getMessage());
        }
    }

    public class MoorgeArray
    {
        public void moorge(Dessert[] d, Scone[] s) {}
        public void moorge(Cake[] c, Dessert[] d){}
        public void moorge(ChocolateCake[] cc, Scone[] s){}
    }

    public void testResolveJavaArrayMethods() throws Exception
    {
        ClassType moorge = maker.classToClassType(MoorgeArray.class);

        MakerMethod method;
        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert[].class)).Push(type(Scone[].class)));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert[].class, Scone[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake[].class)).Push(type(Dessert[].class)));
        assertEquals("Did not resolve method: ", toString("moorge", Cake[].class, Dessert[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake[].class)).Push(type(ButteredScone[].class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake[].class, Scone[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(Dessert[].class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", Dessert[].class, Scone[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(type(Scone[].class)));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake[].class, Scone[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(type(ChocolateCake[].class)).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake[].class, Scone[].class), method.toString());

        method = maker.resolveMethod(moorge, "moorge", maker.Push(ClassMaker.NULL_TYPE).Push(ClassMaker.NULL_TYPE));
        assertEquals("Did not resolve method: ", toString("moorge", ChocolateCake[].class, Scone[].class), method.toString());

        try {
            maker.resolveMethod(moorge, "dummy", null);
            fail("Should throw ClassMakerException");
        } catch (ClassMakerException ex) {
            assertEquals("Class contains no method called \'dummy\'", ex.getMessage());
        }
    }

    protected String toString(String name, Class left, Class right)
    {
        return "void " + name + "(" + ClassMaker.classToName(left) + ", " + ClassMaker.classToName(right) + ")";
    }

    protected void failToResolveMethod(String name, CallStack params)
    {
    	String signature = ClassMaker.toMethodString(name, (Type[])params.toArray(), null);
        try {
            maker.resolveMethod(maker.getClassType(), name, params);
            fail("Should not resolve method for " + signature);
        } catch (ClassMakerException ex) {
            assertEquals("Cannot resolve method call : " + signature, ex.getMessage());
        }
    }
}
