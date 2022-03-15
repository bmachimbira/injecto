package dev.brianmachimbira.injecto.test.testmodels

interface ICyclicDependencyB {
    val dependencyA: ICyclicDependencyA?
}