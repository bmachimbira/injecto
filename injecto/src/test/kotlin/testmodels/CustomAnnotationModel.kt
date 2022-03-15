package dev.brianmachimbira.injecto.test.testmodels

import javax.inject.Inject

class CustomAnnotationModel {
    @CustomAnnotationTest
    var dependency1: ITestInterfaceC? = null

    @Inject
    var dependency2: ITestInterfaceC? = null
}