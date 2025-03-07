package de.kb1000.joptimizer.ir.node.constant

class LongConstant(val value: Long) : Constant {
    override fun toString() = "long $value"

    override fun equals(other: Any?): Boolean {
        return other === this || (other is LongConstant && other.value == value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
