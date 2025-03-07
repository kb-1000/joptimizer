package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.util.stream
import java.util.stream.Collectors

// TODO: can any of these throw?
class LowLevelBinOpNode(var operation: Operation) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    enum class OperationClass(val op: String) {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%"),
        LEFT_SHIFT("<<"),
        RIGHT_SHIFT(">>"),
        UNSIGNED_RIGHT_SHIFT(">>>"),
        AND("&"),
        OR("|"),
        XOR("^"),
        ;

        companion object {
            val fromText = OperationClass.values().stream().collect(Collectors.toMap(OperationClass::op) { it }).toMap<String, OperationClass>()
        }

        override fun toString() = op
    }

    enum class Operation(private val text: String) {
        INTEGER_ADD("integer +"),
        LONG_ADD("long +"),
        FLOAT_ADD("float +"),
        DOUBLE_ADD("double +"),
        INTEGER_SUBTRACT("integer -"),
        LONG_SUBTRACT("long -"),
        FLOAT_SUBTRACT("float -"),
        DOUBLE_SUBTRACT("double -"),
        INTEGER_MULTIPLY("integer *"),
        LONG_MULTIPLY("long *"),
        FLOAT_MULTIPLY("float *"),
        DOUBLE_MULTIPLY("double *"),
        INTEGER_DIVIDE("integer /"),
        LONG_DIVIDE("long /"),
        FLOAT_DIVIDE("float /"),
        DOUBLE_DIVIDE("double /"),
        INTEGER_MODULO("integer %"),
        LONG_MODULO("long %"),
        FLOAT_MODULO("float %"),
        DOUBLE_MODULO("double %"),
        INTEGER_LEFT_SHIFT("integer <<"),
        LONG_LEFT_SHIFT("long <<"),
        INTEGER_RIGHT_SHIFT("integer >>"),
        LONG_RIGHT_SHIFT("long >>"),
        INTEGER_UNSIGNED_RIGHT_SHIFT("integer >>>"), // TODO: check if this is the right right shift
        LONG_UNSIGNED_RIGHT_SHIFT("long >>>"), // TODO: check if this is the right right shift
        INTEGER_AND("integer &"),
        LONG_AND("long &"),
        INTEGER_OR("integer |"),
        LONG_OR("long |"),
        INTEGER_XOR("integer ^"),
        LONG_XOR("long ^"),
        ;

        val type: VarType
        val opClass: OperationClass

        init {
            val (typeName, opName) = text.split(' ')
            type = when (typeName) {
                "integer" -> VarType.INTEGER
                "long" -> VarType.LONG
                "float" -> VarType.FLOAT
                "double" -> VarType.DOUBLE
                else -> throw IllegalStateException()
            }
            opClass = OperationClass.fromText.getOrElse(opName, ::TODO)
        }

        override fun toString() = text
    }

    override fun toString() = "ll_binop $operation"

    override val nodeInfo
        get() = LowLevelBinOpNode.nodeInfo
}