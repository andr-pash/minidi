#MiniDI

## Getting started

## Concepts
At the core of MiniDI is the injector. 
This is what handles the object creation and dependency resolution for you.
To be able to do so, the injector needs to know what the dependencies of your application are, and how it should provide them.
This is done by defining bindings that link a type to a recipe on how to provide a concrete implementation / value for this type.

``` java
MiniDI.create()
    .bind(MyAbstraction.class).toClass(MyImplementation.class)
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
### Instance binding
(coming soon)
### Dynamic binding
(coming soon)
## Scopes
(coming soon)
### Transient (default)
(coming soon)
### Singleton
(coming soon)
### Injector private
(coming soon)

## Lazy initialization
(coming soon)
