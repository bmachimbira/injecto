package dev.brianmachimbira.injecto.test

import dev.brianmachimbira.injecto.utils.ListUtilWrapper
import dev.brianmachimbira.injecto.test.testmodels.TestConcreteA
import dev.brianmachimbira.injecto.test.testmodels.TestConcreteB
import dev.brianmachimbira.injecto.test.testmodels.TestConcreteC
import org.junit.Assert
import org.junit.Test

class UtilFixture {

    @Test
    fun givenList_WhenConvertToDelimitedString_ConvertsCorrectly() {
        val expectedString =
            "class dev.brianmachimbira.injecto.test.testmodels.TestConcreteA,class dev.brianmachimbira.injecto.test.testmodels.TestConcreteB,class dev.brianmachimbira.injecto.test.testmodels.TestConcreteC"
        val classes: MutableList<Class<*>> = ArrayList()
        classes.add(TestConcreteA::class.java)
        classes.add(TestConcreteB::class.java)
        classes.add(TestConcreteC::class.java)
        val wrapper = ListUtilWrapper(classes)

        val actualString = wrapper.toDelimitedString(",")

        Assert.assertEquals(expectedString, actualString)
    }
}