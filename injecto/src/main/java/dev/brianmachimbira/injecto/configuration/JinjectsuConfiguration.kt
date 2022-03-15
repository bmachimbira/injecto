package dev.brianmachimbira.injecto.configuration

import dev.brianmachimbira.injecto.exceptions.InvalidAnnotationClassException

class InjectoConfiguration : IInjectoConfiguration {
    override var injectionAnnotation: Class<*>? = null
        private set

    fun withCustomAnnotation(customAnnotation: Class<*>): InjectoConfiguration {
        if (!customAnnotation.isAnnotation) {
            throw InvalidAnnotationClassException(customAnnotation)
        }
        injectionAnnotation = customAnnotation
        return this
    }
}