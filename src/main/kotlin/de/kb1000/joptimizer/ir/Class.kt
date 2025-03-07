package de.kb1000.joptimizer.ir

import de.kb1000.joptimizer.data.KnownClass

class Class private constructor(
    @JvmField
    val classPool: ClassPool,
    @JvmField
    var internalName: String,
    @JvmField
    internal var source: ClassSource,
    @JvmField
    val methods: MutableList<Method>,
) : Type, ObjectType {
    constructor(classPool: ClassPool, internalName: String) : this(
        classPool,
        internalName,
        ClassSource.APPLICATION,
        mutableListOf()
    )

    enum class ClassSource {
        UNKNOWN, APPLICATION, LIBRARY
    }

    enum class ClassType {
        CLASS, INTERFACE, ANNOTATION, ENUM
    }


    private var superClass: Class? = null
    private var interfaces: List<Class>? = null
    private val innerClasses = linkedSetOf<InnerClass>()
    var sourceFile: String? = null


    var isLoaded = false
        internal set
    var libraryClass: (() -> KnownClass)? = null
        @JvmName("getLibraryClass$")
        get
        set(value) {
            if (field !== null) {
                throw IllegalStateException("libraryClass is already set")
            }
            field = value
        }

    fun initSuperClass(superClass: Class, interfaces: List<Class>) {
        check(this.superClass === null) { "superClass already set for $internalName" }
        check(this.interfaces === null) { "interfaces already set for $internalName" }
        this.superClass = superClass
        this.interfaces = interfaces
    }

    fun addInnerClass(innerClass: InnerClass) {
        innerClasses.add(innerClass)
    }

    fun loadFromData() {
        val knownClass = libraryClass!!()
        isLoaded = true
    }

    // XXX: remove, and fix data tree/loadFromData
    fun getLibraryClass() = libraryClass

    fun hasKnownClass() = libraryClass !== null

    override val descriptor
        get() = "L$internalName;"

    override val humanName
        get() = internalName.replace('/', '.')

    override fun toString() = internalName

    fun dump(): String {
        return "class $internalName {\n${methods.joinToString("\n\n")}\n}"
    }

    companion object {
        @JvmStatic
        fun createUnknownClass(classPool: ClassPool, internalName: String): Class {
            return Class(classPool, internalName, ClassSource.UNKNOWN, mutableListOf())
        }
    }
}
