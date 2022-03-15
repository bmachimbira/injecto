package dev.brianmachimbira.injecto.test

import dev.brianmachimbira.injecto.Injecto
import dev.brianmachimbira.injecto.configuration.InjectoConfiguration
import dev.brianmachimbira.injecto.exceptions.TypeAlreadyRegisteredException
import dev.brianmachimbira.injecto.interfaces.IFactoryMethod
import dev.brianmachimbira.injecto.test.testmodels.ITestInterfaceC
import dev.brianmachimbira.injecto.test.testmodels.TestConcreteC
import org.junit.Assert
import org.junit.Test

class SingletonFixture {

    private val configuration = InjectoConfiguration()

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingSingleton_ResolvesTSameInstance() {
        val injecto = Injecto(configuration)
        injecto
            .bind(ITestInterfaceC::class.java).lifestyleSingleton(TestConcreteC::class.java)
        val resolved1 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        val resolved2 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        Assert.assertNotNull(resolved1)
        Assert.assertNotNull(resolved2)
        Assert.assertEquals(resolved1, resolved2)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingSingletonFactoryMethod_FactoryIsInvokedAndResolvesTSameInstance() {
        val injecto = Injecto(configuration)
        val factoryTracker = booleanArrayOf(false)
        injecto.bind(ITestInterfaceC::class.java)
            .lifestyleSingleton<TestConcreteC>(object : IFactoryMethod<TestConcreteC> {
                override fun create(): TestConcreteC {
                    factoryTracker[0] = true
                    return TestConcreteC()
                }
            })
        val resolved1 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        val resolved2 = injecto.resolve<ITestInterfaceC>(ITestInterfaceC::class.java)
        Assert.assertTrue(factoryTracker[0])
        Assert.assertNotNull(resolved1)
        Assert.assertNotNull(resolved2)
        Assert.assertEquals(resolved1, resolved2)
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingSingletonFactoryMethodOfExistingRegistration_ThrowsTypeAlreadyRegisteredException() {
        val injecto = Injecto(configuration)
        injecto.bind(TestConcreteC::class.java).lifestyleSingleton(TestConcreteC::class.java)
        try {
            injecto.bind(TestConcreteC::class.java).lifestyleSingleton<TestConcreteC>(
                object : IFactoryMethod<TestConcreteC> {
                    override fun create(): TestConcreteC {
                        return TestConcreteC()
                    }
                })
            Assert.assertTrue(false)
        } catch (e: TypeAlreadyRegisteredException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun givenInjecto_WhenBindingSingletonOfExistingRegistration_ThrowsTypeAlreadyRegisteredException() {
        val injecto = Injecto(configuration)
        injecto.bind(TestConcreteC::class.java)
            .lifestyleSingleton<TestConcreteC>(object : IFactoryMethod<TestConcreteC> {
                override fun create(): TestConcreteC {
                    return TestConcreteC()
                }
            })
        try {
            injecto.bind(TestConcreteC::class.java).lifestyleSingleton(TestConcreteC::class.java)
            Assert.assertTrue(false)
        } catch (e: TypeAlreadyRegisteredException) {
            Assert.assertTrue(true)
        }
    }
}