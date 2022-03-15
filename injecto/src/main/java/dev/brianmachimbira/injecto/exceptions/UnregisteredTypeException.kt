package dev.brianmachimbira.injecto.exceptions

import java.lang.RuntimeException

class UnregisteredTypeException(message: String?) : RuntimeException(message)