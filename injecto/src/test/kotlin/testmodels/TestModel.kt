package dev.brianmachimbira.injecto.test.testmodels

import javax.inject.Inject

class TestModel : TestModelWithNoFields() {
    @Inject
    val testObjectA: ITestInterfaceA? = null

    @Inject
    val testObjectB: ITestInterfaceB? = null

    @Inject
    val testObjectC: ITestInterfaceC? = null
}