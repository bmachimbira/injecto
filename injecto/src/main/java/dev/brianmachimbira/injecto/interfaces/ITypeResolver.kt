package dev.brianmachimbira.injecto.interfaces

import kotlin.Throws
import java.lang.IllegalAccessException
import java.lang.reflect.InvocationTargetException
import java.lang.InstantiationException
import dev.brianmachimbira.injecto.Injecto

interface ITypeResolver {
    @Throws(
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    fun resolve(abstractType: Class<*>, injecto: Injecto?): Any?
    fun getTypeToResolveFor(type: Class<*>): Class<*>?
    val registeredTypes: Set<Class<*>>
    fun isTypeRegistered(registeredType: Class<*>): Boolean
}