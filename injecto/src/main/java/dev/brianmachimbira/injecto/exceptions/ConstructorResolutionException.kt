package dev.brianmachimbira.injecto.exceptions

import java.lang.RuntimeException

class ConstructorResolutionException(message: String?, throwable: Throwable?) :
    RuntimeException(message, throwable)