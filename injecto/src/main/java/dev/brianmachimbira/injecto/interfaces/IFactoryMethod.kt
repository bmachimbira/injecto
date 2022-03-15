package dev.brianmachimbira.injecto.interfaces

interface IFactoryMethod<TConcrete> {
    fun create(): TConcrete
}