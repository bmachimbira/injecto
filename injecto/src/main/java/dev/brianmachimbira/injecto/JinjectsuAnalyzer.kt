package dev.brianmachimbira.injecto

import dev.brianmachimbira.injecto.exceptions.UnregisteredTypeException

class InjectoAnalyzer(private val injecto: Injecto) {

    fun validateTypeRegistration(): Boolean {
        val allRegisteredTypes: MutableSet<Class<*>> = HashSet()
        allRegisteredTypes.addAll(injecto.singletonContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.transientContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.instanceContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.scopedContainer.registeredTypes)

        for (type in allRegisteredTypes) {
            val concreteType = injecto.getTypeRegisteredUnder(type)
            val dependencies = concreteType?.let { injecto.getConstructorDependenciesForType(it) }
            if (dependencies != null) {
                for (dependency in dependencies) {
                    if (injecto.registrationTypeMap.containsKey(dependency)) {
                        continue
                    }
                    try {
                        if (!allRegisteredTypes.contains(dependency)) {
                            throw UnregisteredTypeException(
                                String.format(
                                    "Type %s was not registered.",
                                    dependency.name
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return false
                    }
                }
            }
        }
        return true
    }

    fun dryRun(): Boolean {
        val allRegisteredTypes: MutableSet<Class<*>> = HashSet()
        allRegisteredTypes.addAll(injecto.singletonContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.transientContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.instanceContainer.registeredTypes)
        allRegisteredTypes.addAll(injecto.scopedContainer.registeredTypes)
        for (type in allRegisteredTypes) {
            try {
                injecto.resolve<Any>(type)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }
}