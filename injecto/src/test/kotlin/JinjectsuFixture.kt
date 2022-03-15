package dev.brianmachimbira.injecto.test

import dev.brianmachimbira.injecto.Injecto
import dev.brianmachimbira.injecto.InjectoAnalyzer
import dev.brianmachimbira.injecto.configuration.InjectoConfiguration
import dev.brianmachimbira.injecto.exceptions.*
import dev.brianmachimbira.injecto.interfaces.IFactoryMethod
import dev.brianmachimbira.injecto.test.testmodels.*
import org.junit.Assert
import org.junit.Test

class InjectoFixture {
    private val configuration = InjectoConfiguration()

    @Test
    fun givenInjecto_WhenResolvingUnboundType_ThrowsException() {
        val injecto = Injecto(configuration)
        try {
            injecto.resolve<Any>(ITestInterfaceA::class.java)
            Assert.assertTrue(false)
        } catch (e: Exception) {
            Assert.assertTrue(true)
            e.printStackTrace()
        }
    }

    @Test
    fun givenInjectoWithBoundType_WhenBindingSameTypeAgain_ThrowsTypeAlreadyRegisteredException() {
        val injecto = Injecto(configuration)
        injecto.bind(TestConcreteC::class.java).lifestyleTransient(TestConcreteC::class.java)
        try {
            injecto.bind(TestConcreteC::class.java).lifestyleSingleton(TestConcreteC::class.java)
            Assert.assertTrue(false)
        } catch (e: TypeAlreadyRegisteredException) {
            Assert.assertTrue(true)
            Assert.assertTrue(e.message!!.contains("TRANSIENT"))
        }
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingInstance_ResolvesCorrectObject() {
        val injecto = Injecto(configuration)
        val concrete = TestConcreteA(TestConcreteB(TestConcreteC()))
        injecto
            .bind(ITestInterfaceA::class.java)
            .instance(concrete)
        val resolvedConcrete = injecto.resolve<TestConcreteA>(ITestInterfaceA::class.java)
        Assert.assertEquals(concrete, resolvedConcrete)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingTransient_ResolvesTreeCorrectly() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.lifestyleTransient(TestConcreteC::class.java)
        val resolved = injecto.resolve<ITestInterfaceA>(ITestInterfaceA::class.java)
        Assert.assertNotNull(resolved)
        Assert.assertNotNull(resolved!!.dependencyB)
        Assert.assertNotNull(resolved.dependencyB?.dependencyC)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingTransientAndInstance_ResolvesTreeCorrectly() {
        val injecto = Injecto(configuration)
        val testC: ITestInterfaceC = TestConcreteC()
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.instance(testC)
        val resolved = injecto.resolve<ITestInterfaceA>(ITestInterfaceA::class.java)
        Assert.assertNotNull(resolved)
        Assert.assertNotNull(resolved!!.dependencyB)
        Assert.assertNotNull(resolved.dependencyB?.dependencyC)
        Assert.assertEquals(testC, resolved.dependencyB?.dependencyC)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingScoped_ResolvesTSameInstanceWithinScope() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceC::class.java).lifeStyleScoped(TestConcreteC::class.java)
        injecto.beginScope()
        val resolved1 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        val resolved2 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        injecto.beginScope()
        val resolved3 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        Assert.assertNotNull(resolved1)
        Assert.assertNotNull(resolved2)
        Assert.assertNotNull(resolved3)
        Assert.assertEquals(resolved1, resolved2)
        Assert.assertNotEquals(resolved3, resolved1)
        Assert.assertNotEquals(resolved3, resolved2)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenResolvingNestedScopes_ResolvesSameInstanceWithinScope() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceC::class.java).lifeStyleScoped(TestConcreteC::class.java)
        injecto.beginScope()
        val resolved1 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        val resolved2 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.beginScope()
        val resolved3 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        injecto.endScope()
        Assert.assertNotNull(resolved1)
        Assert.assertNotNull(resolved2)
        Assert.assertNotNull(resolved3)
        Assert.assertEquals(resolved1, resolved2)
        Assert.assertNotEquals(resolved3, resolved1)
        Assert.assertNotEquals(resolved3, resolved2)
    }

    @Test
    fun givenScopedWithContext_WhenResolvingOutsideScope_ThrowsTypeNotRegisteredException() {
        val injecto = Injecto(configuration)
        injecto.bind(TestConcreteC::class.java).providedByScope()
        val dependencyA = TestConcreteC()
        injecto.beginScope(dependencyA)
        injecto.endScope()
        try {
            injecto.resolve<Any>(TestConcreteC::class.java)
            Assert.assertTrue(false)
        } catch (e: UnregisteredTypeException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun givenObjectWithInjectableFields_WhenInjecting_ResolvedCorrectly() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleSingleton(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleSingleton(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.lifestyleSingleton(TestConcreteC::class.java)
        val model = TestModel()
        injecto.inject(model)
        Assert.assertNotNull(model.testObjectA)
        Assert.assertNotNull(model.testObjectB)
        Assert.assertNotNull(model.testObjectC)
        Assert.assertEquals(model.testObjectA?.dependencyB, model.testObjectB)
        Assert.assertEquals(model.testObjectB?.dependencyC, model.testObjectC)
    }

    @Test
    @Throws(Exception::class)
    fun givenObjectWithInjectableFields_WhenInjectingIntoBaseType_ResolvedCorrectly() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleSingleton(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleSingleton(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.lifestyleSingleton(TestConcreteC::class.java)
        val model: TestModelWithNoFields = TestModel()
        injecto.inject(model)
        val castedModel = model as TestModel
        Assert.assertNotNull(castedModel.testObjectA)
        Assert.assertNotNull(castedModel.testObjectB)
        Assert.assertNotNull(castedModel.testObjectC)
        Assert.assertEquals(castedModel.testObjectA?.dependencyB, castedModel.testObjectB)
        Assert.assertEquals(castedModel.testObjectB?.dependencyC, castedModel.testObjectC)
    }

    @Test
    fun givenClassesWithCyclicDependency_WhenRegisterTransiently_ThrowsException() {
        val injecto = Injecto(configuration)
        try {
            injecto
                .bind(ICyclicDependencyA::class.java)
                .lifestyleTransient(CyclicDependencyA::class.java)
                ?.bind(ICyclicDependencyB::class.java)
                ?.lifestyleTransient(CyclicDependencyB::class.java)
            Assert.assertTrue(false)
        } catch (e: Exception) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenClassesWithCyclicDependency_WhenRegisterSingleton_ThrowsException() {
        val injecto = Injecto(configuration)
        try {
            injecto
                .bind(ICyclicDependencyA::class.java)
                .lifestyleSingleton(CyclicDependencyA::class.java)
                ?.bind(ICyclicDependencyB::class.java)
                ?.lifestyleSingleton(CyclicDependencyB::class.java)
            Assert.assertTrue(false)
        } catch (e: Exception) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenClassesWithCyclicDependency_WhenRegisterScoped_ThrowsCyclicDependencyException() {
        val injecto = Injecto(configuration)
        try {
            injecto.beginScope()
            injecto
                .bind(ICyclicDependencyA::class.java)
                .lifestyleSingleton(CyclicDependencyA::class.java)
                ?.bind(ICyclicDependencyB::class.java)
                ?.lifestyleSingleton(CyclicDependencyB::class.java)
            Assert.assertTrue(false)
        } catch (e: CyclicDependencyException) {
            Assert.assertTrue(true)
        } catch (exception: Exception) {
            Assert.assertTrue(false)
        }
        injecto.endScope()
    }

    @Test
    fun givenDependencyWithInaccessibleConstructor_WhenResolving_ThrowsConstructorResolutionException() {
        val injecto = Injecto(configuration)
        injecto.bind(DependencyWithPrivateConstructor::class.java).lifestyleSingleton(
            DependencyWithPrivateConstructor::class.java
        )
        try {
            injecto.resolve<DependencyWithPrivateConstructor>(
                DependencyWithPrivateConstructor::class.java
            )
            Assert.assertTrue(false)
        } catch (e: ConstructorResolutionException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenDependencyWithErrorInConstructor_WhenResolving_ThrowsConstructorResolutionException() {
        val injecto = Injecto(configuration)
        injecto.bind(DependencyWithConstructorException::class.java).lifestyleSingleton(
            DependencyWithConstructorException::class.java
        )
        try {
            injecto.resolve<DependencyWithPrivateConstructor>(
                DependencyWithConstructorException::class.java
            )
            Assert.assertTrue(false)
        } catch (e: ConstructorResolutionException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenInjectoWithMissingRegistrations_WhenValidatingRegistration_ReturnsInvalid() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.validateTypeRegistration()
        Assert.assertFalse(validRegistrations)
    }

    @Test
    fun givenInjectoWithCompleteRegistrations_WhenValidatingRegistration_ReturnsValid() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)
            ?.lifestyleSingleton(object : IFactoryMethod<TestConcreteC> {
                override fun create(): TestConcreteC {
                    return TestConcreteC()
                }
            })
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.validateTypeRegistration()
        Assert.assertTrue(validRegistrations)
    }

    @Test
    fun givenInjectoWithScopeContextRegistration_WhenValidatingRegistration_ReturnsValid() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleSingleton(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.providedByScope()!!
            .satisfiedBy(TestConcreteC::class.java)
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.validateTypeRegistration()
        Assert.assertTrue(validRegistrations)
    }

    @Test
    fun givenInjectoWithMissingRegistrations_WhenDryRunning_ReturnsInvalid() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.dryRun()
        Assert.assertFalse(validRegistrations)
    }

    @Test
    fun givenInjectoWithExceptionThrowingDependencies_WhenDryRunning_ReturnsInvalid() {
        val injecto = Injecto(configuration)
        injecto.bind(DependencyWithConstructorException::class.java).lifestyleTransient(
            DependencyWithConstructorException::class.java
        )
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.dryRun()
        Assert.assertFalse(validRegistrations)
    }

    @Test
    fun givenInjectoWithCompleteRegistrations_WhenDryRunning_ReturnsValid() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceA::class.java).lifestyleTransient(TestConcreteA::class.java)
            ?.bind(ITestInterfaceB::class.java)?.lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.lifestyleTransient(TestConcreteC::class.java)
        val analyzer = InjectoAnalyzer(injecto)
        val validRegistrations = analyzer.validateTypeRegistration()
        Assert.assertTrue(validRegistrations)
    }

    @Test
    fun givenInjectoWithScopeContextDependencies_WhenResolveInsideScopeWithoutContext_ThrowsException() {
        val injecto = Injecto(configuration)
        injecto.bind(ITestInterfaceB::class.java).lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.providedByScope()!!
            .satisfiedBy(TestConcreteC::class.java)
        try {
            injecto.beginScope()
            injecto.resolve<Any>(ITestInterfaceC::class.java)
            injecto.endScope()
        } catch (e: InvalidScopeException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenInjectoWithScopeContextDependencies_WhenResolveInsideWrongContext_ThrowsException() {
        val injecto = Injecto(configuration)
        injecto.bind(ITestInterfaceB::class.java).lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.providedByScope()!!
            .satisfiedBy(TestConcreteC::class.java)
        try {
            injecto.beginScope(Any())
            injecto.resolve<Any>(ITestInterfaceC::class.java)
            injecto.endScope()
        } catch (e: InvalidScopeContextException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun givenInjectoWithScopeContextDependenciesSatisfiedByOne_WhenResolveInsideContext_ResolvesCorrectly() {
        val injecto = Injecto(configuration)
        injecto.bind(ITestInterfaceB::class.java).lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.providedByScope()!!
            .satisfiedBy(TestConcreteC::class.java)
        injecto.beginScope(TestConcreteC())
        val resolved = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        Assert.assertNotNull(resolved)
    }

    @Test
    fun givenInjectoWithScopeContextDependenciesSatisfiedByMany_WhenResolveInsideContext_ResolvesCorrectly() {
        val injecto = Injecto(configuration)
        injecto.bind(ITestInterfaceB::class.java).lifestyleTransient(TestConcreteB::class.java)
            ?.bind(ITestInterfaceC::class.java)?.providedByScope()!!
            .satisfiedBy(TestConcreteC2::class.java, TestConcreteC::class.java)
        injecto.beginScope(TestConcreteC())
        val resolved = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        injecto.beginScope(TestConcreteC2())
        val resolved2 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        injecto.endScope()
        Assert.assertNotNull(resolved)
        Assert.assertNotNull(resolved2)
        Assert.assertEquals(resolved!!.javaClass, TestConcreteC::class.java)
        Assert.assertEquals(resolved2!!.javaClass, TestConcreteC2::class.java)
    }

    @Test
    fun givenInjecto_WhenInjectingIntoCustomAnnotation_InjectsCorrectly() {
        val injecto = Injecto(
            InjectoConfiguration().withCustomAnnotation(
                CustomAnnotationTest::class.java
            )
        )
        injecto.bind(ITestInterfaceC::class.java).lifestyleTransient(TestConcreteC::class.java)
        val testModel = CustomAnnotationModel()
        injecto.inject(testModel)
        Assert.assertNotNull(testModel.dependency1)
        Assert.assertNull(testModel.dependency2)
    }

    @Test
    fun givenInjectoConfiguration_WhenSettingInvalidCustomAnnotationType_ThrowsInvalidAnnotationException() {
        val config = InjectoConfiguration()
        try {
            config.withCustomAnnotation(TestConcreteC::class.java)
            Assert.assertTrue(false)
        } catch (e: InvalidAnnotationClassException) {
            Assert.assertTrue(true)
        }
    }
}