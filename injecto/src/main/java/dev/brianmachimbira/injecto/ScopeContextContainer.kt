package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.exceptions.InvalidScopeContextException
import dev.brianmachimbira.injecto.exceptions.InvalidScopeException
import dev.brianmachimbira.injecto.interfaces.ITypeResolver
import dev.brianmachimbira.injecto.utils.ListUtilWrapper
import java.lang.reflect.InvocationTargetException

class ScopeContextContainer(private val scopedContainer: ScopedContainer) : ITypeResolver {
    private val concreteContextMap: MutableMap<Class<*>, MutableList<Class<*>>>
    private fun register(abstractType: Class<*>, concreteContext: Class<*>) {
        if (!concreteContextMap.containsKey(abstractType)) {
            concreteContextMap[abstractType] = ArrayList()
        }
        val concreteContexts = concreteContextMap[abstractType]!!
        concreteContexts.add(concreteContext)
    }

    fun register(abstractType: Class<*>, vararg concreteContexts: Class<*>) {
        for (c in concreteContexts) {
            this.register(abstractType, c)
        }
    }

    @Throws(
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    override fun resolve(abstractType: Class<*>, injecto: Injecto?): Any? {
        checkScopeSafety(abstractType)
        checkContextValidity(abstractType)
        return scopedContainer.currentScope.context
    }

    override fun getTypeToResolveFor(type: Class<*>): Class<*>? {
        return null
    }

    override val registeredTypes: Set<Class<*>>
        get() {
            val registeredTypes: MutableSet<Class<*>> = HashSet()
            registeredTypes.add(scopedContainer.currentScope.context!!.javaClass)
            return registeredTypes
        }

    override fun isTypeRegistered(registeredType: Class<*>): Boolean {
        return scopedContainer.currentScope.context!!.javaClass == registeredType
    }

    private fun checkScopeSafety(abstractType: Class<*>) {
        val scope = scopedContainer.currentScope
            ?: throw InvalidScopeContextException(
                String.format(
                    "Dependency of type %s can not be satisfied outside of a scope.",
                    abstractType.name
                )
            )
    }

    private fun checkContextValidity(abstractType: Class<*>) {
        val context = scopedContainer.currentScope.context
            ?: throw InvalidScopeException(
                String.format(
                    "Dependency of type %s could not be satisfied because it depends on the current scope context which is null.",
                    abstractType
                )
            )
        val scopeContextType: Class<*> = context.javaClass
        val requiredScopeContextTypes: List<Class<*>> = concreteContextMap[abstractType]!!
        val classList = ListUtilWrapper(requiredScopeContextTypes)
        if (!requiredScopeContextTypes.contains(scopeContextType)) {
            throw InvalidScopeContextException(
                String.format(
                    "Dependency of type %s could not be satisfied by the current scope context %s. Scope context must be one of [%s]",
                    abstractType.name,
                    scopeContextType.name,
                    classList.toDelimitedString(",")
                )
            )
        }
    }

    init {
        concreteContextMap = HashMap()
    }
}