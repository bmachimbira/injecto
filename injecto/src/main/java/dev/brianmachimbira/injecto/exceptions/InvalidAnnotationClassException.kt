package dev.brianmachimbira.injecto.exceptions

import java.lang.RuntimeException

class InvalidAnnotationClassException(attemptedAnnotationClass: Class<*>) : RuntimeException(
    String.format("Type %s was expected to be an annotation.", attemptedAnnotationClass.name)
)