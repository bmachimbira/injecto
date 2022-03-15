package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.interfaces.IScopeContextBinder

internal class ScopeContextBinder(
    private val injecto: Injecto,
    private val abstractType: Class<*>
) : IScopeContextBinder {

    override fun satisfiedBy(vararg concreteScopeContexts: Class<*>): Injecto? {
        injecto.registerScopeContext(abstractType, *concreteScopeContexts)
        return injecto
    }
}