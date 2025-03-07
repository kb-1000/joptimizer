package de.kb1000.joptimizer.ir.node.lowlevel

/**
 * While generic nodes are classified as [LowLevelNode]s, they're _also_ valid SSA nodes (even though the type system
 * won't say so), because they don't use anything specific to [LowLevelNode]s.
 */
interface GenericNode
