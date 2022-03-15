package dev.brianmachimbira.injecto.exceptions

import java.lang.RuntimeException

class CyclicDependencyException(message: String?) : RuntimeException(message)