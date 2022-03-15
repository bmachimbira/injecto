package dev.brianmachimbira.injecto.interfaces

import dev.brianmachimbira.injecto.Injecto

interface IScopeContextBinder {
    fun satisfiedBy(vararg concreteScopeContexts: Class<*>): Injecto?
}