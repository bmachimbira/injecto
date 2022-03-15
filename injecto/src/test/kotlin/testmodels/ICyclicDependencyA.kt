package dev.brianmachimbira.injecto.test.testmodels

interface ICyclicDependencyA {
    val dependencyB: ICyclicDependencyB?
}