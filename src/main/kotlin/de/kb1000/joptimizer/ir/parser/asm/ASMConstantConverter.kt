package de.kb1000.joptimizer.ir.parser.asm

import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.constant.*
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.Type.*

fun convertConstant(classPool: ClassPool, value: Any): Constant {
    return when (value) {
        is Int -> IntConstant(value)
        is Float -> FloatConstant(value)
        is Long -> LongConstant(value)
        is Double -> DoubleConstant(value)
        is String -> StringConstant(value)
        is Type -> when (value.sort) {
            ARRAY, OBJECT -> ClassConstant(convertType(classPool, value) as ObjectType)
            METHOD -> MethodTypeConstant(
                MethodType(
                    value.argumentTypes.map { convertType(classPool, it) },
                    convertType(classPool, value.returnType)
                )
            )
            else -> throw IllegalArgumentException("Unknown ASM Type sort ${value.sort}")
        }
        is Handle -> MethodHandleConstant(
            when (value.tag) {
                H_GETFIELD -> MethodHandleConstant.MethodHandleType.GET_INSTANCE
                H_GETSTATIC -> MethodHandleConstant.MethodHandleType.GET_STATIC
                H_PUTFIELD -> MethodHandleConstant.MethodHandleType.PUT_INSTANCE
                H_PUTSTATIC -> MethodHandleConstant.MethodHandleType.PUT_STATIC
                H_INVOKEVIRTUAL -> MethodHandleConstant.MethodHandleType.INVOKE_VIRTUAL
                H_INVOKESTATIC -> MethodHandleConstant.MethodHandleType.INVOKE_STATIC
                H_INVOKESPECIAL -> MethodHandleConstant.MethodHandleType.INVOKE_SPECIAL
                H_NEWINVOKESPECIAL -> MethodHandleConstant.MethodHandleType.NEW_INVOKE_SPECIAL
                H_INVOKEINTERFACE -> MethodHandleConstant.MethodHandleType.INVOKE_INTERFACE
                else -> throw IllegalStateException("Unknown MethodHandle tag ${value.tag}")
            }, classPool.findClass(value.owner), value.name, value.desc, value.isInterface
        )
        is ConstantDynamic -> TODO()
        else -> throw IllegalArgumentException("Unknown object of type ${value.javaClass}")
    }
}