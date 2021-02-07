package com.github.andrpash.minidi;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class MiniDI
{
	private static final List<Class<? extends Annotation>> INJECT_ANNOTATIONS = new ArrayList<>( );

	static
	{
		INJECT_ANNOTATIONS.add( MiniDI.Inject.class );
		if ( jsr330supported( ) )
		{
			try
			{
				INJECT_ANNOTATIONS.add( ( Class<? extends Annotation> ) Class.forName( "javax.inject.Inject" ) );
			}
			catch ( final ClassNotFoundException ignored )
			{

			}
		}
	}

	private static boolean jsr330supported( )
	{
		try
		{
			Class.forName( "javax.inject.Inject" );
			return true;
		}
		catch ( final ClassNotFoundException e )
		{
			return false;
		}
	}

	public interface InjectorBuilder
	{
		<T, U extends T> BindingBuilder<T, U> bind( Class<T> clazz );

		InjectorBuilder injectorPrivate( Class<?>... classes );

		InjectorBuilder dynamic( Class<?>... classes );

		Injector initialize( );
	}

	public interface Injector
	{
		<T> T get( Class<T> clazz );

		InjectorBuilder createChild( );

		<T, U extends T> BindingBuilder<T, U> bindDynamic( Class<T> clazz );
	}

	static class InjectorImpl implements Injector, InjectorBuilder
	{
		InjectorImpl parent = null;
		Registry registry = new Registry( );

		List<Class<?>> dynamicBindings = new ArrayList<>( );
		List<Class<?>> injectorPrivates = new ArrayList<>( );

		private InjectorImpl( final InjectorImpl parent )
		{
			this.parent = parent;
		}

		InjectorImpl( )
		{

		}

		@Override
		public <T, U extends T> BindingBuilder<T, U> bind( final Class<T> clazz )
		{
			return new BindingBuilder<>( clazz, this );
		}

		@Override
		public <T, U extends T> BindingBuilder<T, U> bindDynamic( final Class<T> clazz )
		{
			return new BindingBuilder<>( clazz, this, true );
		}

		@Override
		public InjectorBuilder injectorPrivate( final Class<?>... classes )
		{
			this.injectorPrivates.addAll( Arrays.asList( classes ) );

			return this;
		}

		@Override
		public InjectorBuilder dynamic( final Class<?>... classes )
		{
			this.dynamicBindings.addAll( Arrays.asList( classes ) );

			return this;
		}

		@Override
		public Injector initialize( )
		{
			/* provide binding to injector instance */
			this.bind( Injector.class ).toInstance( this );

			for ( final Binding<?> binding : this.registry.getBindings( ) )
			{
				validateBinding( binding.clazz );
			}

			return this;
		}

		@Override
		public InjectorBuilder createChild( )
		{
			return new InjectorImpl( this );
		}

		@Override
		synchronized public <T> T get( final Class<T> clazz )
		{
			T instance = resolveInstance( clazz );
			if ( instance == null )
			{
				final Binding<T> binding = resolveBinding( clazz );
				instance = createInstance( binding );
			}

			return instance;
		}

		private <T> T resolveInstance( final Class<T> clazz )
		{
			T instance = null;
			if ( this.registry.hasInstance( clazz ) )
			{
				instance = this.registry.getInstance( clazz );
			}
			else if ( this.isChildInjector( ) )
			{
				instance = this.parent.resolveInstance( clazz );
			}

			return instance;
		}

		private <T> Binding<T> resolveBinding( final Class<T> clazz )
		{
			return resolveBinding( clazz, this );
		}

		private <T> Binding<T> resolveBinding( final Class<T> clazz, final InjectorImpl requestingInjector )
		{
			final boolean isRequestToSelf = this == requestingInjector;
			final boolean accessByChildAllowed = !this.injectorPrivates.contains( clazz );

			Binding<T> binding = null;
			if ( ( isRequestToSelf || accessByChildAllowed ) && this.registry.hasBinding( clazz ) )
			{
				binding = this.registry.getBinding( clazz );
			}
			else if ( this.isChildInjector( ) )
			{
				binding = this.parent.resolveBinding( clazz, this );
			}

			return binding;
		}

		private boolean isChildInjector( )
		{
			return this.parent != null;
		}

		private <T> T createInstance( final Binding<T> binding )
		{
			try
			{
				final List<? extends Dependency> constructorDependencies = binding.getConstructorDependencies( );
				final Object[] constructorDependencyInstances = getDependencyInstances( constructorDependencies );

				final List<? extends Dependency> fieldDependencies = binding.getFieldDependencies( );
				final Object[] fieldDependencyInstances = getDependencyInstances( fieldDependencies );

				return binding.construct( constructorDependencyInstances, fieldDependencyInstances );
			}
			catch ( final Exception e )
			{
				throw new InstantiationException( e );
			}
		}

		private Object[] getDependencyInstances( final List<? extends Dependency> dependencies )
		{
			final ArrayList<Object> dependencyInstances = new ArrayList<>( );
			for ( final Dependency dependency : dependencies )
			{
				Object instance = resolveInstance( dependency.type );
				if ( instance == null )
				{
					if ( dependency.lazy )
					{
						instance = createProxy( dependency.type );
					}
					else
					{
						instance = get( dependency.type );
					}
				}

				dependencyInstances.add( instance );
			}

			return dependencyInstances.toArray( );
		}

		private <T> T createProxy( final Class<T> clazz )
		{
			return ( T ) Proxy.newProxyInstance(
				clazz.getClassLoader( ),
				new Class<?>[] { clazz },
				new LazyInitProxy<>( clazz, this )
			);
		}

		private void validateBinding( final Class<?> clazz )
		{
			validateBinding( clazz, new ArrayList<>( ) );
		}

		private void validateBinding( final Class<?> clazz, final List<Class<?>> previousDependencies )
		{
			if ( previousDependencies.contains( clazz ) )
			{
				throw new CircularDependencyException( previousDependencies.get( 0 ) );
			}
			previousDependencies.add( clazz );

			final Binding<?> binding = resolveBinding( clazz );
			if ( this.dynamicBindings.contains( clazz ) && binding == null )
			{
				return;
			}

			if ( binding == null )
			{
				throw new MissingBindingException( clazz );
			}

			for ( final Dependency dependency : binding.dependencyInformation.getAllDependencies( ) )
			{
				/* every subtree has to have it's own list copy to allow for duplicate injections of the same type */
				validateBinding( dependency.type, new ArrayList<>( previousDependencies ) );
			}
		}
	}

	@Target( { ElementType.FIELD, ElementType.CONSTRUCTOR } )
	@Retention( RetentionPolicy.RUNTIME )
	public @interface Inject
	{
	}

	@Target( { ElementType.FIELD, ElementType.PARAMETER } )
	@Retention( RetentionPolicy.RUNTIME )
	public @interface Lazy
	{
	}

	@Target( { ElementType.TYPE } )
	@Retention( RetentionPolicy.RUNTIME )
	public @interface Singleton
	{
	}

	static class LazyInitProxy<T> implements InvocationHandler
	{
		private final Class<T> clazz;
		private final InjectorImpl container;
		private T instance = null;

		public LazyInitProxy( final Class<T> clazz, final InjectorImpl container )
		{
			this.clazz = clazz;
			this.container = container;
		}

		@Override
		public Object invoke( final Object proxy, final Method method, final Object[] args ) throws Throwable
		{
			/* Note: debugging this actually invokes the toString() method, which triggers the object creation */
			if ( this.instance == null )
			{
				this.instance = this.container.get( this.clazz );
			}

			return method.invoke( this.instance, args );
		}
	}

	public static class Registry
	{
		Map<Class<?>, Binding<?>> bindingRegistry = new HashMap<>( );

		<T> Binding<T> getBinding( final Class<T> clazz )
		{
			return ( Binding<T> ) this.bindingRegistry.get( clazz );
		}

		<T> void putBinding( final Binding<T> binding )
		{
			this.bindingRegistry.put( binding.clazz, binding );
		}

		boolean hasBinding( final Class<?> clazz )
		{
			return this.bindingRegistry.containsKey( clazz );
		}

		<T> T getInstance( final Class<T> clazz )
		{
			T instance = null;
			if ( this.hasBinding( clazz ) )
			{
				instance = this.getBinding( clazz ).instance;
			}

			return instance;
		}

		boolean hasInstance( final Class<?> clazz )
		{
			boolean hasInstance = false;
			if ( this.hasBinding( clazz ) )
			{
				hasInstance = this.getBinding( clazz ).instance != null;
			}

			return hasInstance;
		}

		Collection<Binding<?>> getBindings( )
		{
			return this.bindingRegistry.values( );
		}
	}

	public interface ScopedBindingBuilder<T>
	{
		InjectorBuilder toClass( final Class<? extends T> clazz );

		InjectorBuilder toFactory( final Class<? extends Factory<T>> clazz );
	}

	public interface ConfiguredBindingBuilder<T, U extends T>
	{
		ScopedBindingBuilder<T> withScope( final BindingScope bindingScope );

		InjectorBuilder toInstance( U instance );
	}

	public static class BindingBuilder<T, U extends T>
		implements ConfiguredBindingBuilder<T, U>, ScopedBindingBuilder<T>
	{
		private final Class<T> clazz;
		private BindingScope bindingScope = null;
		private final InjectorImpl container;
		private boolean validateOnCreation = false;

		private BindingBuilder( final Class<T> clazz, final InjectorImpl container )
		{
			this.clazz = clazz;
			this.container = container;
		}

		private BindingBuilder( final Class<T> clazz, final InjectorImpl container, final boolean validateOnCreation )
		{
			this.validateOnCreation = validateOnCreation;
			this.clazz = clazz;
			this.container = container;
		}

		@Override
		public InjectorBuilder toClass( final Class<? extends T> clazz )
		{
			final Binding<T> binding = new ClassBinding<>( this.clazz, clazz );
			binding.bindingScope = determineBindingScope( clazz );
			registerBinding( binding );

			return this.container;
		}

		@Override
		public InjectorBuilder toInstance( final U instance )
		{
			final Binding<T> binding = new InstanceBinding<>( this.clazz, instance );
			registerBinding( binding );

			return this.container;
		}

		@Override
		public InjectorBuilder toFactory( final Class<? extends Factory<T>> factory )
		{
			final Binding<T> binding = new FactoryBinding<>( this.clazz, factory );
			binding.bindingScope = determineBindingScope( factory );
			registerBinding( binding );

			return this.container;
		}

		@Override public ScopedBindingBuilder<T> withScope( final BindingScope bindingScope )
		{
			this.bindingScope = bindingScope;

			return this;
		}

		private void registerBinding( final Binding<?> binding )
		{
			this.container.registry.putBinding( binding );
			if ( this.validateOnCreation )
			{
				this.container.validateBinding( binding.clazz );
			}
		}

		private BindingScope determineBindingScope( final Class<?> clazz )
		{
			if ( this.bindingScope != null )
			{
				return this.bindingScope;
			}

			return clazz.getAnnotation( MiniDI.Singleton.class ) != null ?
				BindingScope.SINGLETON :
				BindingScope.TRANSIENT;
		}
	}

	public interface Factory<T>
	{
		T create( );
	}

	static abstract class Dependency
	{
		Class<?> type;
		boolean lazy;

		public Dependency( final Class<?> type )
		{
			this.type = type;
		}
	}

	static class FieldDependency extends Dependency
	{
		Field field;

		public FieldDependency( final Field field )
		{
			super( field.getType( ) );
			this.field = field;
			this.lazy = field.getAnnotation( Lazy.class ) != null;
		}

		public Field getField( )
		{
			return this.field;
		}
	}

	static class ConstructorDependency extends Dependency
	{
		public ConstructorDependency( final Parameter parameter )
		{
			super( parameter.getType( ) );
			this.lazy = parameter.getAnnotation( Lazy.class ) != null;
		}
	}

	private static class DependencyInformation
	{
		static DependencyInformation NONE = new DependencyInformation( );

		List<FieldDependency> fields;
		Constructor<?> constructor;
		List<ConstructorDependency> constructorDependencies;

		public DependencyInformation( final Constructor<?> constructor, final List<Field> fields )
		{
			this.constructor = constructor;
			this.constructorDependencies = Arrays.stream( constructor.getParameters( ) )
				.map( ConstructorDependency::new )
				.collect( Collectors.toList( ) );
			this.fields = fields.stream( )
				.map( FieldDependency::new )
				.collect( Collectors.toList( ) );
		}

		private DependencyInformation( )
		{
			this.constructor = null;
			this.fields = new ArrayList<>( );
			this.constructorDependencies = new ArrayList<>( );
		}

		List<ConstructorDependency> getConstructorDependencies( )
		{
			return this.constructorDependencies;
		}

		List<FieldDependency> getFieldDependencies( )
		{
			return this.fields;
		}

		List<Dependency> getAllDependencies( )
		{
			final List<Dependency> dependencies = new ArrayList<>( );
			dependencies.addAll( getConstructorDependencies( ) );
			dependencies.addAll( getFieldDependencies( ) );

			return dependencies;
		}
	}

	public enum BindingScope
	{
		SINGLETON,
		TRANSIENT
	}

	static abstract class Binding<T>
	{
		Class<T> clazz;
		DependencyInformation dependencyInformation;
		T instance = null;
		BindingScope bindingScope = BindingScope.SINGLETON;

		public Binding( final Class<T> clazz )
		{
			this.clazz = clazz;
		}

		abstract T construct( Object[] constructorDependencies, Object[] fieldDependencies )
			throws java.lang.InstantiationException, IllegalAccessException, InvocationTargetException;

		protected DependencyInformation resolveDependencies( final Class<?> clazz )
		{
			final Constructor<?> constructor = getConstructor( clazz );
			final List<Field> injectionFields = getInjectionFields( clazz );

			return new DependencyInformation( constructor, injectionFields );
		}

		private boolean isInjectable( final AccessibleObject accessibleObject )
		{
			boolean isInjectable = false;
			for ( final Class<? extends Annotation> injectAnnotation : INJECT_ANNOTATIONS )
			{
				isInjectable = accessibleObject.getAnnotation( injectAnnotation ) != null;
				if ( isInjectable )
				{
					break;
				}
			}

			return isInjectable;
		}

		private Constructor<?> getConstructor( final Class<?> clazz )
		{
			Constructor<?> constructor = null;
			for ( final Constructor<?> declaredConstructor : clazz.getDeclaredConstructors( ) )
			{
				if ( isInjectable( declaredConstructor ) )
				{
					constructor = declaredConstructor;
					break;
				}
			}

			if ( constructor == null )
			{
				try
				{
					constructor = clazz.getDeclaredConstructor( );
					final int modifiers = constructor.getModifiers( );
					if ( !Modifier.isPublic( modifiers ) )
					{
						throw new IllegalAccessException( );
					}
				}
				catch ( final NoSuchMethodException | IllegalAccessException e )
				{
					throw new MissingConstructorException( clazz );
				}
			}

			return constructor;
		}

		private List<Field> getInjectionFields( final Class<?> clazz )
		{
			final Field[] fields = clazz.getDeclaredFields( );

			final List<Field> dependencyFields = new ArrayList<>( );
			for ( final Field field : fields )
			{
				final boolean isInjectable = isInjectable( field );

				if ( isInjectable )
				{
					final boolean isLazy = field.getAnnotation( Lazy.class ) != null;
					if ( isLazy && !field.getType( ).isInterface( ) )
					{
						throw new InvalidLazyAnnotation( clazz );
					}

					dependencyFields.add( field );
				}
			}

			return dependencyFields;
		}

		List<ConstructorDependency> getConstructorDependencies( )
		{
			return this.dependencyInformation.getConstructorDependencies( );
		}

		List<FieldDependency> getFieldDependencies( )
		{
			return this.dependencyInformation.getFieldDependencies( );
		}

		protected void injectFieldDependencies( final Object instance, final Object[] dependencies )
			throws IllegalAccessException
		{
			final List<Field> fieldList = this.dependencyInformation.fields.stream( )
				.map( FieldDependency::getField )
				.collect( Collectors.toList( ) );

			for ( int i = 0; i < fieldList.size( ); i++ )
			{
				final Field field = fieldList.get( i );
				final Object dependencyInstance = dependencies[ i ];

				field.setAccessible( true );
				field.set( instance, dependencyInstance );
			}
		}
	}

	static class ClassBinding<T, U extends T> extends Binding<T>
	{
		Class<U> boundClazz;

		public ClassBinding( final Class<T> clazz, final Class<U> boundClazz )
		{
			super( clazz );
			this.boundClazz = boundClazz;
			this.dependencyInformation = resolveDependencies( boundClazz );
		}

		@Override
		public T construct( final Object[] constructorDependencies, final Object[] fieldDependencies )
			throws java.lang.InstantiationException, IllegalAccessException, InvocationTargetException
		{
			final T instance = ( T ) this.dependencyInformation.constructor.newInstance( constructorDependencies );
			injectFieldDependencies( instance, fieldDependencies );

			if ( this.bindingScope == BindingScope.SINGLETON )
			{
				this.instance = instance;
			}

			return instance;
		}
	}

	static class InstanceBinding<T, U extends T> extends Binding<T>
	{
		public InstanceBinding( final Class<T> clazz, final U instance )
		{
			super( clazz );
			this.instance = instance;
			this.dependencyInformation = DependencyInformation.NONE;
		}

		@Override
		T construct( final Object[] constructorDependencies, final Object[] fieldDependencies )
		{
			return this.instance;
		}
	}

	static class FactoryBinding<T> extends Binding<T>
	{
		public FactoryBinding( final Class<T> clazz, final Class<? extends Factory<T>> factoryClass )
		{
			super( clazz );
			this.dependencyInformation = resolveDependencies( factoryClass );
		}

		@Override
		T construct( final Object[] constructorDependencies, final Object[] fieldDependencies )
			throws IllegalAccessException, java.lang.InstantiationException, InvocationTargetException
		{
			final Factory<T> factory =
				( Factory<T> ) this.dependencyInformation.constructor.newInstance( constructorDependencies );
			injectFieldDependencies( factory, fieldDependencies );

			final T instance = factory.create( );
			if ( this.bindingScope == BindingScope.SINGLETON )
			{
				this.instance = instance;
			}

			return instance;
		}
	}

	public static class InstantiationException extends RuntimeException
	{
		public InstantiationException( final Throwable cause )
		{
			super( cause );
		}
	}

	public static class CircularDependencyException extends RuntimeException
	{
		public CircularDependencyException( final Class<?> clazz )
		{
			super( "Circular dependency detected for class " + clazz.getSimpleName( ) );
		}
	}

	public static class MissingBindingException extends RuntimeException
	{
		public <T> MissingBindingException( final Class<T> clazz )
		{
			super( "Missing binding detected for class " + clazz );
		}
	}

	public static class MissingConstructorException extends RuntimeException
	{
		public <T> MissingConstructorException( final Class<T> clazz )
		{
			super( "Missing or inaccessible constructor detected for class.\n" +
				"Please check that the constructor that is used for injection is public.\n" +
				"Class: " + clazz );
		}
	}

	public static class InvalidLazyAnnotation extends RuntimeException
	{
		public InvalidLazyAnnotation( final Class<?> clazz )
		{
			super( "Class contains invalid configuration for lazy initialisation.\n" +
				"Lazy init is only allowed for interfaces.\n" +
				"Please check the configuration for class: " + clazz );
		}
	}

	public static InjectorBuilder create( )
	{
		return new InjectorImpl( );
	}

	private MiniDI( )
	{

	}
}
