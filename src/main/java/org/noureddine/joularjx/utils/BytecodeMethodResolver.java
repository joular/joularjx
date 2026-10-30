// Features proposed by Rushin Dipak Makwana under supervision of Dr. Denis Pankratov and Dr. Yann-Gael Gueheneuc
package org.noureddine.joularjx.utils;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassLoaderRepository;
import org.apache.bcel.util.Repository;
import org.apache.bcel.util.SyntheticRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to resolve exact method signatures (including parameters) using BCEL 
 * by matching class names, method names, and line numbers from stack traces.
 */
public class BytecodeMethodResolver {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static Repository repository = null;

    private static Repository getRepository() {
        if (repository == null) {
            try {
                // Use the system class loader repository to find application classes
                repository = new ClassLoaderRepository(ClassLoader.getSystemClassLoader());
            } catch (Exception e) {
                repository = SyntheticRepository.getInstance();
            }
        }
        return repository;
    }

    public static String resolve(StackTraceElement ste) {
        if (ste.getLineNumber() < 0) {
            return ste.getClassName() + "." + ste.getMethodName();
        }

        String cacheKey = ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber();
        return cache.computeIfAbsent(cacheKey, k -> performResolution(ste));
    }

    private static String performResolution(StackTraceElement ste) {
        String className = ste.getClassName();
        String methodName = ste.getMethodName();
        int lineNumber = ste.getLineNumber();

        try {
            Repository repo = getRepository();
            JavaClass javaClass = repo.loadClass(className);

            List<Method> candidates = new ArrayList<>();
            for (Method method : javaClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    candidates.add(method);
                }
            }

            if (candidates.isEmpty()) return className + "." + methodName;

            // If there is only one method with this name, return it immediately.
            if (candidates.size() == 1) {
                return formatSignature(className, methodName, candidates.get(0).getArgumentTypes());
            }

            // For overloaded methods, use line number matching
            for (Method method : candidates) {
                LineNumberTable lineNumberTable = method.getLineNumberTable();
                if (lineNumberTable == null) continue;

                LineNumber[] lineNumbers = lineNumberTable.getLineNumberTable();
                if (lineNumbers.length == 0) continue;

                int startLine = lineNumbers[0].getLineNumber();
                int endLine = lineNumbers[lineNumbers.length - 1].getLineNumber();

                boolean foundLine = false;
                for (LineNumber ln : lineNumbers) {
                    if (ln.getLineNumber() == lineNumber) {
                        foundLine = true;
                        break;
                    }
                }

                if (foundLine || (lineNumber >= startLine && lineNumber <= endLine)) {
                    return formatSignature(className, methodName, method.getArgumentTypes());
                }
            }
        } catch (Exception e) {
            // Ignore loading errors
        }
        return className + "." + methodName;
    }

    private static String formatSignature(String className, String methodName, Type[] argTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(".").append(methodName).append("(");
        for (int i = 0; i < argTypes.length; i++) {
            sb.append(bcelToJavaType(argTypes[i]));
            if (i < argTypes.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static String bcelToJavaType(Type t) {
        if (t.equals(Type.INT)) return "int";
        if (t.equals(Type.BOOLEAN)) return "boolean";
        if (t.equals(Type.CHAR)) return "char";
        if (t.equals(Type.BYTE)) return "byte";
        if (t.equals(Type.SHORT)) return "short";
        if (t.equals(Type.LONG)) return "long";
        if (t.equals(Type.FLOAT)) return "float";
        if (t.equals(Type.DOUBLE)) return "double";
        if (t instanceof org.apache.bcel.generic.ArrayType) {
            org.apache.bcel.generic.ArrayType at = (org.apache.bcel.generic.ArrayType) t;
            return bcelToJavaType(at.getBasicType()) + "[]".repeat(at.getDimensions());
        }
        return t.toString();
    }
}
