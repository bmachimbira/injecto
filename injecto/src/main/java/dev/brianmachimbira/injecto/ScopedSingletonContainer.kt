package dev.brianmachimbira.injecto

class ScopedSingletonContainer : SingletonContainer() {
    var context: Any? = null
}