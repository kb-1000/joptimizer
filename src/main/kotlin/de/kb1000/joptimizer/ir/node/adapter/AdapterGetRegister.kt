package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.lowlevel.VarNode
import de.kb1000.joptimizer.ir.node.lowlevel.VarType

class AdapterGetRegister(override var type: VarType, override var i: Int) : AdapterNode(), ValueNode, VarNode {
    override val isUse: Boolean
        get() = true
    override fun dropVarNode(block: Block) {
        throw IllegalStateException("Can't drop load node")
    }
    override fun toString() = "@$nodeIndex = adapter_get_register $type \$var$i"

    override fun isPure() = true
}
