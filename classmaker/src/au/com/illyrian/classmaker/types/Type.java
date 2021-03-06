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

package au.com.illyrian.classmaker.types;

/**
 * Base class for all ClassMaker types.
 *
 * @author dstrong
 */
public class Type
{
    /** The name of the type */
    private final String name;

    /** The JVM signature for the type */
    private final String signature;

    /** The java Class which this type is based upon */
    private Class javaClass = null;

    /**
     * Base constructor for all ClassMaker types.
     * @param name name of the type
     * @param signature JVM signature for the type
     */
    public Type(String name, String signature)
    {
        this.name = name;
        this.signature = signature;
    }
    
    public Type getType()
    {
        return this;
    }

    public Type toType()
    {
        return this;
    }

    /**
     * Convert this <code>Type</code> to a <code>PrimitiveType</code>.
     * @return a <code>PrimitiveType</code> if appropriate; otherwise null
     */
    public PrimitiveType toPrimitive()
    {
        return null;
    }

    /**
     * Convert this <code>Type</code> to a <code>ClassType</code>.
     * @return a <code>ClassType</code> if appropriate; otherwise null
     */
    public ClassType toClass()
    {
        return null;
    }

    /**
     * Convert this <code>Type</code> to an <code>ArrayType</code>.
     * @return an <code>ArrayType</code> if appropriate; otherwise null
     */
    public ArrayType toArray()
    {
        return null;
    }
    
    /**
     * The name of this type.
     * @return a type name String
     */
    public String getName()
    {
        return name;
    }

    /**
     * The signature for this type.
     * @return a signature String
     */
    public String getSignature()
    {
        return signature;
    }

    /**
     * The java Class that this type is based upon.
     * @return a java Class
     */
    public Class getJavaClass()
    {
        return javaClass;
    }
    
    /**
     * Sets the java class for this type.
     * @param javaClass a java class
     */
    public void setJavaClass(Class javaClass)
    {
        this.javaClass = javaClass;
    }

    /**
     * A string that describes the type.
     */
    public String toString()
    {
        return getClass().getSimpleName() + '(' + name + ')';
    }

    /**
     * Compares two types and returns <code>true</code> if they are equal.
     * <br/>
     * Compares the names of the types.
     */
    public boolean equals(Object obj)
    {
        if (obj != null && getClass().equals(obj.getClass()))
            return name.equals(((Type) obj).name);
        return false;
    }

    /**
     * A hashcode for the <code>Type</code>.
     * <br/>
     * The hashcode is based on the <code>this.getClass()</code> and the name.
     * @return a hashcode
     */
    public int hashcode()
    {
        return getClass().hashCode() ^ name.hashCode();
    }
}
