package de.kb1000.joptimizer.ir

import de.kb1000.joptimizer.data.KnownClass
import de.kb1000.joptimizer.ir.Class.Companion.createUnknownClass

// TODO: what about naming this OptimizerContext?
class ClassPool {
    companion object {
        val regex = Regex("^[^\\[]+$")
    }

    private enum class Phase {
        PARSING, RESOLVING, TRANSFORMING, WRITING
    }

    private val phase = Phase.PARSING
    private val classes = hashMapOf<String, Class>()
    private val libraryClasses = hashMapOf<String, Class>()
    private val unknownClasses = hashMapOf<String, Class>()

    fun getClasses(): Map<String, Class> = classes

    fun add(clazz: Class) {
        classes[clazz.internalName] = clazz
        unknownClasses.remove(clazz.internalName)
    }

    private fun findClass(name: String, loadFromData: Boolean, create: Class.ClassSource?): Class {
        if (!regex.matches(name)) {
            throw IllegalArgumentException("$name is not a valid class name")
        }
        return if (phase == Phase.PARSING) {
            val applicationClass = classes[name]
            applicationClass ?: unknownClasses.computeIfAbsent(name) { internalName ->
                createUnknownClass(
                    this,
                    internalName
                )
            }
        } else {
            val unknownClass = unknownClasses[name]
            if (unknownClass != null) {
                val data = KnownClass.KNOWN_CLASSES[name]
                if (data !== null) {
                    unknownClasses.remove(name)
                    //assert !unknownClasses.containsValue(unknownClass) && unknownClass.internalName.equals(name);
                    libraryClasses[name] = unknownClass
                    unknownClass.libraryClass = data
                    if (loadFromData) {
                        unknownClass.loadFromData()
                    }
                    return unknownClass
                }
                throw IllegalArgumentException("$name: no such application or library class exists")
            }
            val applicationClass = classes[name]
            if (applicationClass != null) {
                return applicationClass
            }
            val libraryClass = libraryClasses[name]
                ?: throw IllegalArgumentException("$name: no such application or library class exists")
            if (!libraryClass.isLoaded) {
                if (!libraryClass.hasKnownClass()) {
                    val data = KnownClass.KNOWN_CLASSES[name]
                        ?: throw IllegalStateException("Class \"$name\" not loaded, but not in KnownClass.KNOWN_CLASSES")
                    libraryClass.libraryClass = data
                }
                if (loadFromData) {
                    libraryClass.loadFromData()
                }
            }
            libraryClass
        }
    }

    @JvmOverloads
    fun findClass(name: String, loadFromData: Boolean = false) = findClass(name, loadFromData, null)

    internal fun findClass(name: String, create: Class.ClassSource) = findClass(name, false, create)
}
