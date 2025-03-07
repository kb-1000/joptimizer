package de.kb1000.joptimizer.ir.parser.asm

import de.kb1000.joptimizer.ir.*
import org.objectweb.asm.Type as AsmType

fun convertType(classPool: ClassPool, type: AsmType): Type = when (type.sort) {
    AsmType.VOID -> PrimitiveType.VOID
    AsmType.BOOLEAN -> PrimitiveType.BOOLEAN
    AsmType.CHAR -> PrimitiveType.CHAR
    AsmType.BYTE -> PrimitiveType.BYTE
    AsmType.SHORT -> PrimitiveType.SHORT
    AsmType.INT -> PrimitiveType.INT
    AsmType.FLOAT -> PrimitiveType.FLOAT
    AsmType.LONG -> PrimitiveType.LONG
    AsmType.DOUBLE -> PrimitiveType.DOUBLE
    AsmType.ARRAY -> ArrayType.from(convertType(classPool, type.elementType))
    AsmType.OBJECT -> classPool.findClass(type.internalName)
    else -> throw IllegalArgumentException("Unknown ASM type sort: ${type.sort}")
}

fun convertMethodType(classPool: ClassPool, descriptor: String): MethodType {
    val methodType = AsmType.getMethodType(descriptor)
    return MethodType(
        methodType.argumentTypes.map { convertType(classPool, it) },
        convertType(classPool, methodType.returnType)
    )
}
