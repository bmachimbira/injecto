# Injecto
Injecto is a lightweight IoC container designed with Android in mind.
Injecto is primarily meant for constructor injection but supports property injection to provide an injection entry point for Android projects.

## Example setup
Injecto setup will typically look as follows:

```Java
new Injecto()
    .bind(IInterfaceA.class).lifestyleSingleton(ImplementationA.class)
    .bind(IInterfaceB.class).lifestyleScoped(ImplementationB.class)
    .bind(IInterfaceD.class).lifestyleTransient(ImplementationD.class)
    .bind(IInterfaceC.class).instance(new ImplementationC());
```

## Example usage

Android projects will typically consume Jinsectsu like so:
```Java
@Inject
private IDependencyA depA;

protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    injectoInstance.inject(this);
}
```
The initial injection happens through property injection. From there on out the dependency tree is resolved through constructor injection.

Alternatively, if you would like to keep Jinsectsu out of most of your project, you can use the Application.ActivityLifecycleCallbacks interface:

```Java
@Override
public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    this.injecto.beginScope(activity);
    this.injecto.inject(activity);
}
//...
@Override
public void onActivityDestroyed(Activity activity) {
    this.injecto.endScope();
}
```
Just be careful with activity stacks. You may need to implement a lookup mechanism to ensure that activities only end their own scopes.

The repo contains an example app to show usage in more detail.

## Multiple lifecycle support
Injecto supports *transient*, *singleton*, *instance* and *scoped* type registrations.

### Transient Binding
Each resolve will provide a new instance of the implementation registered under the interface type.

### Singleton Binding
Each resolve will provide the same instance of the dependency.

Injecto also supports singleton factory method registration if you wish to execute some code when your singleton is first created:
```Java
injecto.bind(ITestA.class).lifestyleSingleton(() -> new TestA());
```

### Instance Binding
The consuming code can new up an appropriate instace of a dependency which  will be provided to all dependent classes.

### Scoped Binding
Consuming code can define custom resolve scopes. Dependencies resolved within a scope will act as a singleton for the lifetime of the scope.
Scopes are primarily intended for Android activities and unit testing.
Injecto also supports nested scopes. For example:

```Java
container.beginScope();
    IDependencyA depA1 = container.resolve(IDependencyA.class);
    IDependencyA depA2 = container.resolve(IDependencyA.class);
    // depA1 == depA2
    container.beginScope();
        IDependencyA depA3 = container.resolve(IDependencyA.class);
        // depA3 != depA1
    container.endScope();
container.endScope();
```
*Please note that the code snippet above is a terrible example of how to use this container. Calling the resolve method manually most likely indicates incorrect usage.*

### Scope context binding

Injecto allows you to bind a dependency to a scope context. Although not a typical IoC container feature, it can be useful for cases where your dependency may be satisfied by different classes depending on the current app state. This was designed for use in Android but can be used anywhere.

Example registration:

```Java
injecto.bind(Context.class).providedByScope().satisfiedBy(MainActivity.class, OtherActivity.class);
```

In the consuming code:
```Java
// MainActivity
@Inject
private MyDependency dependency; // Requires Context in constructor.

protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    injecto.beginScope(this); // The scope context is now MainActivity
    injecto.inject(this); // The dependecy is satisfied by injecting MainActivity into MyDependency as Context
}
```
## Custom annotation configuration
Injecto uses the javax.inject.Inject annotation for property injection by default. This may cause some conflicts with other injection libraries (if you have perhaps a view injector for example that will try to bind views using @Inject). It is possible to configure the annotation to be used for property injection like so:
```Java
InjectoConfiguration config = new InjectoConfiguration().withCustomAnnotation(MyAnnotation.class);

Injecto container = new Injecto(config);
```

## Unit test setup
Injecto provides the *validateTypeRegistration* helper to help you ensure that your container is not missing any dependency registrations.

```Java
@Test
public void givenInjectoContainer_WhenValidatingRegistration_ReturnsValid() {
    Injecto yourInjectoContainer = yourJinectsuSetupMethod();

    InjectoAnalyzer analyzer = new InjectoAnalyzer(yourInjectoContainer);

    boolean validRegistration = analyzer.validateTypeRegistration();

    Assert.assertTrue(validRegistration);
}
```

Additionally the *dryRun* method can be used to initiate a full dependency tree resolution in a unit test. This can be useful to identify erronous constructor logic.

```Java
@Test
public void givenInjectoContainer_WhenValidatingRegistration_ReturnsValid() {
    Injecto yourInjectoContainer = yourJinectsuSetupMethod();

    InjectoAnalyzer analyzer = new InjectoAnalyzer(yourInjectoContainer);

    boolean validRegistration = analyzer.dryRun();

    Assert.assertTrue(validRegistration);
}
```
*Take care not to use dryRun() in actual production code as singletons will be created and every constructor in your dependency tree will be invoked.*
