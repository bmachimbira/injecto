package dev.brianmachimbira.injecto.exceptions

import java.lang.RuntimeException

class InjectionException(message: String?, throwable: Throwable?) :
    RuntimeException(message, throwable)