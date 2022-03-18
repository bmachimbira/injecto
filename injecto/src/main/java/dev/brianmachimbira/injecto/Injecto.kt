package dev.brianmachimbira.injecto


import dev.brianmachimbira.injecto.configuration.IInjectoConfiguration
import dev.brianmachimbira.injecto.exceptions.ConstructorResolutionException
import dev.brianmachimbira.injecto.exceptions.InjectionException
import dev.brianmachimbira.injecto.exceptions.TypeAlreadyRegisteredException
import dev.brianmachimbira.injecto.exceptions.UnregisteredTypeException
import dev.brianmachimbira.injecto.interfaces.IFactoryMethod
import dev.brianmachimbira.injecto.interfaces.ITypeBinder
import dev.brianmachimbira.injecto.interfaces.ITypeResolver
import java.lang.reflect.InvocationTargetException

@Suppress("UNCHECKED_CAST")
class Injecto(configuration: IInjectoConfiguration) {
    private val injectionAnnotation by lazy {
        configuration.injectionAnnotation
    }
    var instanceContainer: InstanceContainer = InstanceContainer()
    var transientContainer: TransientContainer = TransientContainer()
    var singletonContainer: SingletonContainer = SingletonContainer()
    var scopedContainer: ScopedContainer = ScopedContainer()
    var scopeContextContainer: ScopeContextContainer = ScopeContextContainer(scopedContainer)
    var registrationTypeMap: MutableMap<Class<*>, RegistrationType> = HashMap()
    private val resolverMap: MutableMap<RegistrationType?, ITypeResolver>
    private val cyclicDependencyChecker: CyclicDependencyChecker = CyclicDependencyChecker()

    fun inject(target: Any) {
        val fields = target.javaClass.declaredFields
        for (field in fields) {
            if (injectionAnnotation != null && !field.isAnnotationPresent(injectionAnnotation as Class<out Annotation>)) {
                continue
            }
            field.isAccessible = true
            try {
                field[target] = this.resolve(field.type)
            } catch (e: IllegalAccessException) {
                throw InjectionException(
                    String.format(
                        "Could not inject property %s of type %s because it is inaccessible.",
                        field.name,
                        target.javaClass.name
                    ), e
                )
            }
        }
    }

    fun bind(abstractType: Class<*>): ITypeBinder {
        if (registrationTypeMap.containsKey(abstractType)) {
            throw TypeAlreadyRegisteredException(
                String.format(
                    "Type %s has already been registered under lifestyle %s.",
                    abstractType.name,
                    registrationTypeMap[abstractType].toString()
                )
            )
        }
        return TypeBinder(abstractType, this)
    }

    fun <TInterface> resolve(abstractType: Class<*>): TInterface? {
        if (!registrationTypeMap.containsKey(abstractType)) {
            throw UnregisteredTypeException(
                String.format(
                    "Type %s was not registered.",
                    abstractType.name
                )
            )
        }
        val registrationType = registrationTypeMap[abstractType]
        val resolver = resolverMap[registrationType]
        return try {
            resolver!!.resolve(abstractType, this) as TInterface?
        } catch (e: InstantiationException) {
            throw ConstructorResolutionException(
                String.format(
                    "An error occurred while resolving type {%s}.",
                    abstractType.name
                ), e
            )
        } catch (e: IllegalAccessException) {
            throw ConstructorResolutionException(
                String.format(
                    "Could not constructor inject type {%s} because its constructor is inaccessible.",
                    abstractType.name
                ), e
            )
        } catch (e: InvocationTargetException) {
            throw ConstructorResolutionException(
                String.format(
                    "An error occurred while resolving type {%s}.",
                    abstractType.name
                ), e
            )
        }
    }

    fun beginScope() {
        scopedContainer.push(ScopedSingletonContainer())
    }

    fun endScope() {
        val container = scopedContainer.pop()
        val context = container.context
        if (context != null) {
            registrationTypeMap.remove(context.javaClass)
        }
    }

    fun beginScope(context: Any?) {
        val container = ScopedSingletonContainer()
        container.context = context
        scopedContainer.push(container)
    }

    fun getTypeRegisteredUnder(registeredType: Class<*>): Class<*>? {
        if (singletonContainer.isTypeRegistered(registeredType)) {
            return singletonContainer.getTypeToResolveFor(registeredType)
        }
        if (transientContainer.isTypeRegistered(registeredType)) {
            return transientContainer.getTypeToResolveFor(registeredType)
        }
        if (scopedContainer.isTypeRegistered(registeredType)) {
            return scopedContainer.getTypeToResolveFor(registeredType)
        }
        if (instanceContainer.isTypeRegistered(registeredType)) {
            return instanceContainer.getTypeToResolveFor(registeredType)
        }
        throw UnregisteredTypeException(
            String.format(
                "Type %s was not regstered.",
                registeredType.name
            )
        )
    }

    fun <TConcrete> registerInstance(abstractType: Class<*>, instance: TConcrete) {
        registrationTypeMap[abstractType] = RegistrationType.INSTANCE
        instanceContainer.register(abstractType, instance)
    }

    fun registerTransient(abstractType: Class<*>, concreteType: Class<*>) {
        cyclicDependencyChecker.registerDependency(
            abstractType,
            listOf(*getConstructorDependenciesForType(concreteType))
        )
        registrationTypeMap[abstractType] = RegistrationType.TRANSIENT
        transientContainer.register(abstractType, concreteType)
    }

    fun registerSingleton(abstractType: Class<*>, concreteType: Class<*>) {
        cyclicDependencyChecker.registerDependency(
            abstractType,
            listOf(*getConstructorDependenciesForType(concreteType))
        )
        registrationTypeMap[abstractType] = RegistrationType.SINGLETON
        singletonContainer.register(abstractType, concreteType)
    }

    fun registerSingleton(abstractType: Class<*>, factoryMethod: IFactoryMethod<*>) {
        val concreteType = factoryMethod.javaClass.methods[0].returnType
        cyclicDependencyChecker.registerDependency(
            abstractType,
            listOf(*getConstructorDependenciesForType(concreteType))
        )
        registrationTypeMap[abstractType] = RegistrationType.SINGLETON
        singletonContainer.register(abstractType, factoryMethod)
    }

    fun registerScoped(abstractType: Class<*>, concreteType: Class<*>) {
        cyclicDependencyChecker.registerDependency(
            abstractType,
            listOf(*getConstructorDependenciesForType(concreteType))
        )
        registrationTypeMap[abstractType] = RegistrationType.SCOPED
        scopedContainer.register(abstractType, concreteType)
    }

    fun registerScopeContext(abstractType: Class<*>, vararg concreteContexts: Class<*>) {
        registrationTypeMap[abstractType] = RegistrationType.SCOPE_CONTEXT
        scopeContextContainer.register(abstractType, *concreteContexts)
    }

    @Throws(
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    fun constructorResolve(type: Class<*>): Any {
        val constructor = type.declaredConstructors[0]
        val parameterTypes = constructor.parameterTypes
        if (parameterTypes.isEmpty()) {
            return constructor.newInstance()
        }
        val parameterValues = arrayOfNulls<Any>(parameterTypes.size)
        for (i in parameterTypes.indices) {
            parameterValues[i] = this.resolve<Any>(parameterTypes[i])
        }
        return constructor.newInstance(*parameterValues)
    }

    @Throws(
        IllegalAccessException::class
    )
    fun getConstructorDependenciesForType(type: Class<*>): Array<Class<*>> {
        val constructors = type.declaredConstructors
        val constructor = constructors[0]
        return constructor.parameterTypes
    }

    init {
        resolverMap = HashMap()
        resolverMap[RegistrationType.INSTANCE] = instanceContainer
        resolverMap[RegistrationType.TRANSIENT] = transientContainer
        resolverMap[RegistrationType.SINGLETON] = singletonContainer
        resolverMap[RegistrationType.SCOPED] = scopedContainer
        resolverMap[RegistrationType.SCOPE_CONTEXT] = scopeContextContainer
    }
}