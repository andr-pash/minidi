#MiniDI

## Getting started

## Concepts
At the core of MiniDI is the injector. 
This is what handles the object creation and dependency resolution for you.
To be able to do so, the injector needs to know what the dependencies of your application are, and how it should provide them.
This is done by defining bindings that link a type to a recipe on how to provide a concrete implementation / value for this type.

``` java
MiniDI.create( )
    .bind( MyAbstraction.class ).toClass( MyImplementation.class )
```
This will tell the injector that every time the application requests `MyAbstraction` it should provide it with `MyImplemenation`.

### Injector hierarchy
Injectors can form a hierarchy, so that shared dependencies are shared and use case specific dependencies are kept separate.
Let's set up such a hierarchy.

``` java
Injector rootInjector = MiniDI.create( )
    .bind( ServiceA.class ).toClass( ServiceA.class )
    .initialize( );

Injector childInjector1 = rootInjector.createChild( )
    .bind( ServiceB.class ).toClass( ServiceB.class )
    .initialize( );

Injector childInjector2 = rootInjector.createChild( )
    .bind( ServiceC.class ).toClass( ServiceC.class )
    .initialize( );
```

In this scenario `childInjector1` and `childInjector2` will have access to `ServiceA` which is provided in the `rootInjector`,
but e.g. `rootInjector` and `childInjector2` will not have access to `ServiceB`. 

## Bindings
A binding defines 'what' and 'how' an injector can fulfill requests for a specific type. It is basically a key value pair.
The key is the type you want to provide and the value is the specific recipe how to provide it.
MiniDI comes with 4 different ways you can define such a binding. 

### Class binding
Class bindings use the constructor of a class to provide the requested instance. If the specified class has a constructor that is
annotated with `MiniDI.Inject` then this will be used. Otherwise `MiniDI` will try to use the default constructor.

### Factory binding
A factory binding delegates the object creation to a factory. This may be useful if the concrete implementation you want to use may depend 
on other factors in the environment. To create a factory binding you first have to define a factory. You do this by creating a class that
implements the `MiniDI.Factory` interface.

The interface requires exactly one method to implemented, the `Factory#create` method. 
Any dependencies that should be injected are then defined on the factory class itself. 

*Example:*

Let's assume an interface `PaymentService` that's implemented by the classes `RealPaymentService` and `DummyPaymentService`.
Depending on some external configuration, we now want to decide at runtime which implementation should be provided.

To do so we implement the `Factory` interface.
``` java
class PaymentServiceFactory implements Factory<PaymentService>
{
    @MiniDI.Inject
    private Config config;

    @Override
    public PaymentService create( )
    {
        return this.config.isEnabled( ) ? new RealPaymentService( ) : new DummyPaymentService( ); 
    }
}
```

The binding can then be defined like this:
``` java
MiniDI.create( )
    .bind( PaymentService.class ).toFactory( PaymentServiceFactory.class )
    .bind( Config.class ).toClass( Config.class )
    .initialize( );
```

### Instance binding
Sometimes you may already have a specific instance you want to be provided. In this case you can define an instance binding.
In contrast to the previous binding mechanisms, `MiniDI` will not perform any injection on the provided instance itself. 
Further, instance bindings are always singleton scoped (See [Scopes](#scopes-a-hrefscopes)).

Example:

Let's now assume we don't want `MiniDI` to create the `Config` instance for us, but we already have an instance we want to reuse.
``` java
final Config config = new Config( );

MiniDI.create( )
    .bind( PaymentService.class ).toFactory( PaymentServiceFactory.class )
    .bind( Config.class ).toInstance( config )
    .initialize( );
```  

### Dynamic binding
In rare cases you may not know how a dependency will be provided at the time you configure your injector.
Since `MiniDI` validates your dependency graph during injector initialisation, you have to explicitly tell it about which injection tokens it
should just assume to have a binding for it provided later.

If you try to get an instance from the injector later which has a dependency on a dynamic binding, but missed to provide said binding you will
receive a ``MissingBindingException`` or `InstantiationException`.

Example:

Let's assume we don't know how our ``Config`` instance should be provided at the time we configure our injector.
We then mark the ``Config`` class as dynamic.

``` java
final Injector injector = MiniDI.create( )
    .bind( PaymentService.class ).toFactory( PaymentServiceFactory.class )
    .dynamic( Config.class )
    .initialize( );
```

At some later point in our application we can then tell the injector about the concrete binding we want to use for the 
``Config.class`` injection token.

``` java
injector
    .bindDynamic( Config.class ).toInstance( config );
```

### Injector private
Injectors can be organised in a hierarchy. Per default a child can look up dependencies in its parent.
In some use cases you may want to prevent children from doing just that. In this case you can tell an injector to regard 
a binding as private.

Example:

Let's assume now we have a root injector and one child. The child should not be able to gain access to the 
``Config`` binding defined in the parent.

``` java
Injector rootInjector = MiniDI.create( )
    .bind( Config.class ).toClass( Config.class )
    .injectorPrivate( Config.class )
    .initialize( );

/* This will throw a MissingBindingException, because Config is inaccessible from the child injector */
Injector childInjector1 = rootInjector.createChild( )
    .bind( PaymentService.class ).toClass( PaymentServiceFactory.class )
    .initialize( );
```

## Scopes <a href="#Scopes">
There are two strategies available on how ``MiniDI`` should handle created instances
### Transient (default)
By default ``MiniDI`` uses the ``BindingScope.TRANSIENT`` strategy, which effectively means 
everytime an instance is requested from an injector, a new one is created.

Since it is the default the following two statements have the same result.

``` java
/* With explicit scope definition */
MiniDI.create( )
    .bind( Config.class ).withScope(BindingScope.TRANSIENT).toClass( Config.class )
    .initialize( );

/* With implicit scope definition */
MiniDI.create( )
    .bind( Config.class ).toClass( Config.class )
    .initialize( );
```

### Singleton
The ``BindingScope.SINGLETON`` tells `MiniDI` to reuse an instance once it has been created.
This is useful if instantiation is expensive, or you need shared state.

``` java
MiniDI.create( )
    .bind( Config.class ).withScope(BindingScope.SINGLETON).toClass( Config.class )
    .initialize( );
```

## Lazy initialization
By default instances are created as soon as they are requested. In our example this means, as soon
as you request an instance of ``PaymentService`` it and all of the dependencies in the dependency graph will be 
created. 

This is fine for most cases, but sometimes you may want to defer the instantiation of the dependencies until
they are actually needed, e.g. when this involves establishing a database connection.

To enable this behavior you can use the ``@MiniDI.Lazy`` annotation, which can be put on fields or 
constructor parameters.

``` java
class MyClass {
    @MiniDI.Inject
    public MyClass( @MiniDI.Lazy final MyDependency dependency ) 
    {
    
    }
}
```

``` java
class MyClass {
    @MiniDI.Inject
    @MiniDI.Lazy
    final MyDependency dependency;
}
```

**Note:**

Lazy initialisation only works with interfaces, because ``MiniDI`` relies on 
the reflection api for this feature.  

## Examples
Take a look at the ``examples`` package in the test folder for working examples of the discussed concepts above.