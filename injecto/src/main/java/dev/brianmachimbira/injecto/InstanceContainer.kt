package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.interfaces.ITypeResolver

class InstanceContainer : ITypeResolver {
    private val instanceMap: MutableMap<Class<*>, Any>
    fun <TConcrete> register(interfaceType: Class<*>, instance: TConcrete) {
        instanceMap[interfaceType] = instance!!
    }

    override fun getTypeToResolveFor(type: Class<*>): Class<*>? {
        return instanceMap[type]!!.javaClass
    }

    override fun isTypeRegistered(registeredType: Class<*>): Boolean {
        return instanceMap.containsKey(registeredType)
    }


    override fun resolve(abstractType: Class<*>, injecto: Injecto?): Any? {
        return instanceMap[abstractType]
    }

    override val registeredTypes: Set<Class<*>>
        get() = instanceMap.keys

    init {
        instanceMap = HashMap()
    }
}