package de.kb1000.joptimizer.ir.node.constant

class IntConstant(val value: Int) : Constant {
    override fun toString() = "int $value"

    override fun equals(other: Any?): Boolean {
        return other === this || (other is IntConstant && other.value == value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
