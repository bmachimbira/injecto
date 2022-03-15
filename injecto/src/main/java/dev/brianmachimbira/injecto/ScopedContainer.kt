package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.interfaces.ITypeResolver
import java.lang.reflect.InvocationTargetException
import java.util.*

class ScopedContainer internal constructor() : ITypeResolver {
    private val typeMap: MutableMap<Class<*>, Class<*>>

    private val scopeContainerStack: Stack<ScopedSingletonContainer> = Stack()

    fun push(singletonContainer: ScopedSingletonContainer) {
        typeMap.forEach {
            singletonContainer.register(it.key, it.value)
        }
        scopeContainerStack.push(singletonContainer)
    }

    fun pop(): ScopedSingletonContainer {
        return scopeContainerStack.pop()
    }

    val currentScope: ScopedSingletonContainer
        get() = scopeContainerStack.peek()

    fun register(abstractType: Class<*>, concreteType: Class<*>) {
        typeMap[abstractType] = concreteType
    }

    @Throws(
        IllegalAccessException::class,
        InstantiationException::class,
        InvocationTargetException::class
    )
    override fun resolve(abstractType: Class<*>, injecto: Injecto?): Any? {
        return currentScope.resolve(abstractType, injecto)
    }

    override fun getTypeToResolveFor(type: Class<*>): Class<*>? {
        return typeMap[type]
    }

    override val registeredTypes: Set<Class<*>>
        get() = typeMap.keys

    override fun isTypeRegistered(registeredType: Class<*>): Boolean {
        return typeMap.containsKey(registeredType)
    }

    init {
        typeMap = HashMap()
    }
}