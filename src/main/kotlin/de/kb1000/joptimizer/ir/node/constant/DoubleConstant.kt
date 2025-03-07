package de.kb1000.joptimizer.ir.node.constant

class DoubleConstant(val value: Double) : Constant {
    override fun toString() = "double ${java.lang.Double.toHexString(value)}"

    override fun equals(other: Any?): Boolean {
        return other === this || (other is DoubleConstant && other.value == value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
