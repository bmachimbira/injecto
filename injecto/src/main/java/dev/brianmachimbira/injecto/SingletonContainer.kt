package dev.brianmachimbira.injecto


import dev.brianmachimbira.injecto.interfaces.ITypeResolver
import dev.brianmachimbira.injecto.interfaces.IFactoryMethod
import dev.brianmachimbira.injecto.exceptions.TypeAlreadyRegisteredException
import kotlin.Throws
import java.lang.IllegalAccessException
import java.lang.InstantiationException
import java.lang.reflect.InvocationTargetException
import dev.brianmachimbira.injecto.exceptions.UnregisteredTypeException
import java.util.HashSet
import java.util.HashMap

open class SingletonContainer : ITypeResolver {
    private val singletonLookup: MutableMap<Class<*>, Any>
    private val singletonTypeMap: MutableMap<Class<*>, Class<*>>
    private val singletonFactoryMethodMap: MutableMap<Class<*>, IFactoryMethod<*>>

    fun register(abstractType: Class<*>, concreteType: Class<*>) {
        if (singletonFactoryMethodMap.containsKey(abstractType)) {
            throw TypeAlreadyRegisteredException(
                String.format(
                    "Type %s has already been registered as a singleton to be resolved using a factory method.",
                    abstractType.name
                )
            )
        }
        singletonTypeMap[abstractType] = concreteType
    }

    fun register(abstractType: Class<*>, factoryMethod: IFactoryMethod<*>) {
        if (singletonTypeMap.containsKey(abstractType)) {
            throw TypeAlreadyRegisteredException(
                String.format(
                    "Type %s has already been registered as a singleton.",
                    abstractType.name
                )
            )
        }
        singletonFactoryMethodMap[abstractType] = factoryMethod
    }

    @Throws(
        IllegalAccessException::class,
        InstantiationException::class,
        InvocationTargetException::class
    )
    private fun createSingleton(abstractClass: Class<*>, injecto: Injecto?): Any {
        if (singletonTypeMap.containsKey(abstractClass)) {
            val concreteClass = singletonTypeMap[abstractClass]!!
            return injecto!!.constructorResolve(concreteClass)
        }
        if (singletonFactoryMethodMap.containsKey(abstractClass)) {
            return singletonFactoryMethodMap[abstractClass]!!.create()!!
        }
        throw UnregisteredTypeException(
            String.format(
                "Type %s was not registered as a singleton.",
                abstractClass.name
            )
        )
    }

    @Throws(
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    override fun resolve(abstractType: Class<*>, injecto: Injecto?): Any? {
        val singletonExists = singletonLookup.containsKey(abstractType)
        if (!singletonExists) {
            singletonLookup[abstractType] = createSingleton(abstractType, injecto)
        }
        return singletonLookup[abstractType]
    }

    override fun getTypeToResolveFor(type: Class<*>): Class<*> {
        if (singletonTypeMap.containsKey(type)) {
            return singletonTypeMap[type]!!
        }
        val factoryMethod = singletonFactoryMethodMap[type]!!
        return factoryMethod.javaClass.methods[0].returnType
    }

    override val registeredTypes: Set<Class<*>>
        get() {
            val types: MutableSet<Class<*>> = HashSet()
            types.addAll(singletonTypeMap.keys)
            types.addAll(singletonFactoryMethodMap.keys)
            return types
        }

    override fun  isTypeRegistered(registeredType: Class<*>): Boolean {
        return singletonTypeMap.containsKey(registeredType) || singletonFactoryMethodMap.containsKey(
            registeredType
        )
    }

    init {
        singletonLookup = HashMap()
        singletonTypeMap = HashMap()
        singletonFactoryMethodMap = HashMap()
    }
}