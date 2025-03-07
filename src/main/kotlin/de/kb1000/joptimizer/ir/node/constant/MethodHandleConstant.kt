package de.kb1000.joptimizer.ir.node.constant

import de.kb1000.joptimizer.ir.Class

class MethodHandleConstant(
    val type: MethodHandleType,
    val owner: Class,
    val member: String,
    val descriptor: String,
    val isInterface: Boolean
) : Constant {
    enum class MethodHandleType {
        GET_INSTANCE,
        GET_STATIC,
        PUT_INSTANCE,
        PUT_STATIC,
        INVOKE_VIRTUAL,
        INVOKE_STATIC,
        INVOKE_SPECIAL,
        NEW_INVOKE_SPECIAL,
        INVOKE_INTERFACE,
    }

    @Suppress("SpellCheckingInspection")
    override fun toString() =
        "methodhandle $type${if (isInterface) " interface" else ""} ${owner.internalName}->$member$descriptor"
}
