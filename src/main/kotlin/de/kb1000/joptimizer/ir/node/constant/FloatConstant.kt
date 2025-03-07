package de.kb1000.joptimizer.ir.node.constant

class FloatConstant(val value: Float) : Constant {
    override fun toString() = "float ${java.lang.Float.toHexString(value)}"

    override fun equals(other: Any?): Boolean {
        return other === this || (other is FloatConstant && other.value == value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
