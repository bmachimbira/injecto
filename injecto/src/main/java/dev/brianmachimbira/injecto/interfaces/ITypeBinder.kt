package dev.brianmachimbira.injecto.interfaces

import dev.brianmachimbira.injecto.Injecto

interface ITypeBinder {
    fun <TConcrete> instance(concrete: TConcrete): Injecto?
    fun lifestyleTransient(concreteType: Class<*>?): Injecto?
    fun lifestyleSingleton(concreteType: Class<*>?): Injecto?
    fun providedByScope(): IScopeContextBinder?
    fun <TConcrete> lifestyleSingleton(factoryMethod: IFactoryMethod<TConcrete>?): Injecto?
    fun lifeStyleScoped(concreteType: Class<*>?): Injecto?
}