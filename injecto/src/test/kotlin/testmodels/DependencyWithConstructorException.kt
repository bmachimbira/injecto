package dev.brianmachimbira.injecto.test.testmodels

import java.lang.RuntimeException

class DependencyWithConstructorException {
    init {
        throw RuntimeException()
    }
}