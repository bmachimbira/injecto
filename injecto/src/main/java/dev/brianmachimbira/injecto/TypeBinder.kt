package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.interfaces.IFactoryMethod
import dev.brianmachimbira.injecto.interfaces.IScopeContextBinder
import dev.brianmachimbira.injecto.interfaces.ITypeBinder

internal class TypeBinder(private val abstractType: Class<*>, private val injecto: Injecto) :
    ITypeBinder {
    override fun <TConcrete> instance(concrete: TConcrete): Injecto {
        injecto.registerInstance(abstractType, concrete)
        return injecto
    }

    override fun lifestyleSingleton(concreteType: Class<*>?): Injecto {
        injecto.registerSingleton(abstractType, concreteType!!)
        return injecto
    }

    override fun providedByScope(): IScopeContextBinder {
        return ScopeContextBinder(injecto, abstractType)
    }

    override fun <TConcrete> lifestyleSingleton(factoryMethod: IFactoryMethod<TConcrete>?): Injecto {
        injecto.registerSingleton(abstractType, factoryMethod!!)
        return injecto
    }

    override fun lifeStyleScoped(concreteType: Class<*>?): Injecto {
        injecto.registerScoped(abstractType, concreteType!!)
        return injecto
    }

    override fun lifestyleTransient(concreteType: Class<*>?): Injecto {
        injecto.registerTransient(abstractType, concreteType!!)
        return injecto
    }
}