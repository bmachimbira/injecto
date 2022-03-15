package dev.brianmachimbira.injecto.test.testmodels

class CyclicDependencyB(private val cyclicDependencyA: ICyclicDependencyA) : ICyclicDependencyB {
    override val dependencyA: ICyclicDependencyA?
        get() = cyclicDependencyA
}