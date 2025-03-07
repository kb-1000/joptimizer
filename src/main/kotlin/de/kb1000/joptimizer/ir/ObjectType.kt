package de.kb1000.joptimizer.ir

interface ObjectType : Type {
    override val length: Int
        get() = 1
}
