package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.exceptions.UnregisteredTypeException
import dev.brianmachimbira.injecto.interfaces.ITypeResolver
import java.lang.reflect.InvocationTargetException

class TransientContainer : ITypeResolver {
    private val concreteTypeMap: MutableMap<Class<*>, Class<*>>
    fun register(abstractType: Class<*>, concreteType: Class<*>) {
        concreteTypeMap[abstractType] = concreteType
    }

    @Throws(
        IllegalAccessException::class,
        InstantiationException::class,
        InvocationTargetException::class
    )
    override fun resolve(abstractType: Class<*>, injecto: Injecto?): Any {
        if (concreteTypeMap.containsKey(abstractType)) {
            return injecto!!.constructorResolve(concreteTypeMap[abstractType]!!)
        }
        throw UnregisteredTypeException(
            String.format(
                "Type {0} was not registered transiently.",
                abstractType.name
            )
        )
    }

    override fun getTypeToResolveFor(type: Class<*>): Class<*> {
        return concreteTypeMap[type]!!
    }

    override val registeredTypes: Set<Class<*>>
        get() = concreteTypeMap.keys

    override fun isTypeRegistered(registeredType: Class<*>): Boolean {
        return concreteTypeMap.containsKey(registeredType)
    }

    init {
        concreteTypeMap = HashMap()
    }
}