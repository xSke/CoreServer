/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javassist.ClassClassPath
 *  javassist.ClassPath
 *  javassist.ClassPool
 *  javassist.NotFoundException
 */
package io.netty.util.internal;

import io.netty.util.internal.NoOpTypeParameterMatcher;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.TypeParameterMatcher;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;

public final class JavassistTypeParameterMatcherGenerator {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JavassistTypeParameterMatcherGenerator.class);
    private static final ClassPool classPool = new ClassPool(true);

    public static void appendClassPath(ClassPath classpath) {
        classPool.appendClassPath(classpath);
    }

    public static void appendClassPath(String pathname) throws NotFoundException {
        classPool.appendClassPath(pathname);
    }

    public static TypeParameterMatcher generate(Class<?> type) {
        ClassLoader classLoader = PlatformDependent.getContextClassLoader();
        if (classLoader == null) {
            classLoader = PlatformDependent.getSystemClassLoader();
        }
        return JavassistTypeParameterMatcherGenerator.generate(type, classLoader);
    }

    /*
     * Exception decompiling
     */
    public static TypeParameterMatcher generate(Class<?> type, ClassLoader classLoader) {
        // This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
        // org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 3[CATCHBLOCK]
        // org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:418)
        // org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:470)
        // org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:2880)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:818)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:196)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:141)
        // org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:95)
        // org.benf.cfr.reader.entities.Method.analyse(Method.java:370)
        // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:852)
        // org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:753)
        // org.benf.cfr.reader.Main.doJar(Main.java:134)
        // org.benf.cfr.reader.Main.main(Main.java:188)
        throw new IllegalStateException("Decompilation failed");
    }

    private static String typeName(Class<?> type) {
        if (type.isArray()) {
            return JavassistTypeParameterMatcherGenerator.typeName(type.getComponentType()) + "[]";
        }
        return type.getName();
    }

    private JavassistTypeParameterMatcherGenerator() {
    }

    static {
        classPool.appendClassPath((ClassPath)new ClassClassPath(NoOpTypeParameterMatcher.class));
    }
}

