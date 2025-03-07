package de.kb1000.joptimizer.ir.node.lowlevel

// TODO: can this throw?
class LowLevelMonitorNode(var type: MonitorNodeType) : LowLevelNode() {
    enum class MonitorNodeType(private val text: String) {
        ENTER("enter"),
        EXIT("exit"),
        ;

        override fun toString() = text
    }
    override fun toString() = "ll_monitor $type"
}