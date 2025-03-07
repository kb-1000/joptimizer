package de.kb1000.joptimizer.ir

class TryCatch(var startBlock: Block, var endBlock: Block, var handlerBlock: Block, var clazz: Class?) {
    override fun toString() =
        "try &${startBlock.i}-&${endBlock.i} catch${clazz?.let { " ${it.internalName}" } ?: ""} &${handlerBlock.i}"

    /**
     * @param matchClass the class to match with (can be null for unknown, will only match if this matches all exceptions)
     * @return whether this catch block matches [matchClass] (can be null for unknown)
     */
    fun matches(matchClass: Class?): Boolean? {
        val clazz = clazz
        if (clazz == null || clazz.internalName == "java/lang/Throwable") {
            return true
        }
        return null // TODO
    }
}
