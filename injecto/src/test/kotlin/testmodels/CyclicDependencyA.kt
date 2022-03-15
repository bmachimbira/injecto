package dev.brianmachimbira.injecto.test.testmodels

class CyclicDependencyA(private val cyclicDependencyB: ICyclicDependencyB) : ICyclicDependencyA {

    override val dependencyB: ICyclicDependencyB?
        get() = cyclicDependencyB
}