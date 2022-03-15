package dev.brianmachimbira.injecto

import java.util.HashSet
import dev.brianmachimbira.injecto.exceptions.CyclicDependencyException
import java.util.HashMap

class CyclicDependencyChecker internal constructor() {
    private val dependencyMap: MutableMap<Class<*>, List<Class<*>>>
    fun registerDependency(type: Class<*>, dependencies: List<Class<*>>) {
        dependencyMap[type] = dependencies
        checkCyclicDependencies(type)
    }

    private fun checkCyclicDependencies(type: Class<*>) {
        for ((key) in dependencyMap) {
            if (hasCyclicDependency(key, HashSet())) {
                throw CyclicDependencyException(
                    String.format(
                        "Cyclic dependency detected when registering type %s",
                        type.name
                    )
                )
            }
        }
    }

    private fun hasCyclicDependency(type: Class<*>, typeTrail: MutableSet<Class<*>>): Boolean {
        if (typeTrail.contains(type)) {
            return true
        }
        typeTrail.add(type)
        if (!dependencyMap.containsKey(type)) {
            return false
        }
        val dependencies = dependencyMap[type]
        if (dependencies == null || dependencies.isEmpty()) {
            return false
        }
        for (dependency in dependencies) {
            return hasCyclicDependency(dependency, typeTrail)
        }
        return false
    }

    init {
        dependencyMap = HashMap()
    }
}