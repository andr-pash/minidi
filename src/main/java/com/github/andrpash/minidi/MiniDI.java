package com.github.andrpash.minidi;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MiniDI {

  private static final List<Class<? extends Annotation>> INJECT_ANNOTATIONS = new ArrayList<>();
  private static final List<Class<? extends Annotation>> SINGLETON_ANNOTATIONS = new ArrayList<>();

  static {
    INJECT_ANNOTATIONS.add(MiniDI.Inject.class);
    SINGLETON_ANNOTATIONS.add(MiniDI.Singleton.class);

    if (jsr330supported()) {
      try {
        INJECT_ANNOTATIONS.add((Class<? extends Annotation>) Class.forName("javax.inject.Inject"));
        SINGLETON_ANNOTATIONS.add(
            (Class<? extends Annotation>) Class.forName("javax.inject.Singleton"));
      } catch (final ClassNotFoundException ignored) {

      }
    }
  }

  private static boolean jsr330supported() {
    try {
      Class.forName("javax.inject.Inject");
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  public interface InjectorBuilder {

    <T, U extends T> BindingBuilder<T, U> bind(Class<T> clazz);

    InjectorBuilder injectorPrivate(Class<?>... classes);

    InjectorBuilder dynamic(Class<?>... classes);

    Injector initialize();
  }

  public interface Injector {

    <T> T get(Class<T> clazz);

    <T> T get(InjectionToken<T> injectionToken);

    InjectorBuilder createChild();

    <T, U extends T> BindingBuilder<T, U> bindDynamic(Class<T> clazz);
  }

  static class InjectorImpl implements Injector, InjectorBuilder {

    InjectorImpl parent = null;
    Registry registry = new Registry();

    List<InjectionToken<?>> dynamicBindings = new ArrayList<>();
    List<InjectionToken<?>> injectorPrivates = new ArrayList<>();

    private InjectorImpl(final InjectorImpl parent) {
      this.parent = parent;
    }

    InjectorImpl() {

    }

    @Override
    public <T, U extends T> BindingBuilder<T, U> bind(final Class<T> clazz) {
      return new BindingBuilder<>(clazz, this);
    }

    @Override
    public <T, U extends T> BindingBuilder<T, U> bindDynamic(final Class<T> clazz) {
      return new BindingBuilder<>(clazz, this, true);
    }

    @Override
    public InjectorBuilder injectorPrivate(final Class<?>... classes) {
      this.injectorPrivates.addAll(
          Arrays.stream(classes).map(InjectionToken::new).collect(Collectors.toList())
      );

      return this;
    }

    @Override
    public InjectorBuilder dynamic(final Class<?>... classes) {
      this.dynamicBindings.addAll(
          Arrays.stream(classes).map(InjectionToken::new).collect(Collectors.toList())
      );

      return this;
    }

    @Override
    public Injector initialize() {
      /* provide binding to injector instance */
      this.bind(Injector.class).toInstance(this);

      for (final Binding<?> binding : this.registry.getBindings()) {
        validateBinding(binding.injectionToken);
      }

      return this;
    }

    @Override
    public InjectorBuilder createChild() {
      return new InjectorImpl(this);
    }

    @Override
    synchronized public <T> T get(final Class<T> clazz) {
      return get(new InjectionToken<>(clazz));
    }

    @Override
    public <T> T get(final InjectionToken<T> injectionToken) {
      T instance = resolveInstance(injectionToken);
      if (instance == null) {
        final Binding<T> binding = resolveBinding(injectionToken);
        instance = createInstance(binding);
      }

      return instance;
    }

    private <T> T resolveInstance(final InjectionToken<T> injectionToken) {
      T instance = null;
      if (this.registry.hasInstance(injectionToken)) {
        instance = this.registry.getInstance(injectionToken);
      } else if (this.isChildInjector()) {
        instance = this.parent.resolveInstance(injectionToken);
      }

      return instance;
    }

    private <T> Binding<T> resolveBinding(final InjectionToken<T> injectionToken) {
      return resolveBinding(injectionToken, this);
    }

    private <T> Binding<T> resolveBinding(final InjectionToken<T> injectionToken,
        final InjectorImpl requestingInjector) {
      final boolean isRequestToSelf = this == requestingInjector;
      final boolean accessByChildAllowed = !this.injectorPrivates.contains(injectionToken);

      Binding<T> binding = null;
      if ((isRequestToSelf || accessByChildAllowed) && this.registry.hasBinding(injectionToken)) {
        binding = this.registry.getBinding(injectionToken);
      } else if (this.isChildInjector()) {
        binding = this.parent.resolveBinding(injectionToken, this);
      }

      return binding;
    }

    private boolean isChildInjector() {
      return this.parent != null;
    }

    private <T> T createInstance(final Binding<T> binding) {
      try {
        final List<? extends Dependency> constructorDependencies = binding.getConstructorDependencies();
        final Object[] constructorDependencyInstances = getDependencyInstances(
            constructorDependencies);

        final List<? extends Dependency> fieldDependencies = binding.getFieldDependencies();
        final Object[] fieldDependencyInstances = getDependencyInstances(fieldDependencies);

        return binding.construct(constructorDependencyInstances, fieldDependencyInstances);
      } catch (final Exception e) {
        throw new InstantiationException(e);
      }
    }

    private Object[] getDependencyInstances(final List<? extends Dependency> dependencies) {
      final ArrayList<Object> dependencyInstances = new ArrayList<>();
      for (final Dependency dependency : dependencies) {
        final InjectionToken<?> injectionToken = new InjectionToken<>(dependency.type)
            .withQualifier(dependency.qualifier);
        Object instance = resolveInstance(injectionToken);
        if (instance == null) {
          if (dependency.lazy) {
            instance = createProxy(injectionToken);
          } else {
            instance = get(injectionToken);
          }
        }

        dependencyInstances.add(instance);
      }

      return dependencyInstances.toArray();
    }

    private <T> T createProxy(final InjectionToken<T> injectionToken) {
      return (T) Proxy.newProxyInstance(
          injectionToken.clazz.getClassLoader(),
          new Class<?>[]{injectionToken.clazz},
          new LazyInitProxy<>(injectionToken, this)
      );
    }

    private void validateBinding(final InjectionToken<?> injectionToken) {
      validateBinding(injectionToken, new ArrayList<>());
    }

    private void validateBinding(
        final InjectionToken<?> injectionToken,
        final List<InjectionToken<?>> previousDependencies) {
      if (previousDependencies.contains(injectionToken)) {
        throw new CircularDependencyException(previousDependencies.get(0));
      }
      previousDependencies.add(injectionToken);

      final Binding<?> binding = resolveBinding(injectionToken);
      if (this.dynamicBindings.contains(injectionToken) && binding == null) {
        return;
      }

      if (binding == null) {
        throw new MissingBindingException(injectionToken);
      }

      for (final Dependency dependency : binding.dependencyInformation.getAllDependencies()) {
        /* every subtree has to have it's own list copy to allow for duplicate injections of the same type */
        validateBinding(
            new InjectionToken<>(dependency.type).withQualifier(dependency.qualifier),
            new ArrayList<>(previousDependencies)
        );
      }
    }
  }

  @Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Inject {

  }

  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Lazy {

  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Named {

    String value() default "";
  }

  @Target({ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Qualifier {

  }

  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Singleton {

  }

  static class LazyInitProxy<T> implements InvocationHandler {

    private final InjectionToken<T> injectionToken;
    private final InjectorImpl container;
    private T instance = null;

    public LazyInitProxy(final InjectionToken<T> injectionToken, final InjectorImpl container) {
      this.injectionToken = injectionToken;
      this.container = container;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      /* Note: debugging this actually invokes the toString() method, which triggers the object creation */
      if (this.instance == null) {
        this.instance = this.container.get(this.injectionToken);
      }

      return method.invoke(this.instance, args);
    }
  }

  public static class Registry {

    Map<InjectionToken<?>, Binding<?>> bindingRegistry = new HashMap<>();

    <T> Binding<T> getBinding(final InjectionToken<T> injectionToken) {
      return (Binding<T>) this.bindingRegistry.get(injectionToken);
    }

    <T> void putBinding(final Binding<T> binding) {
      this.bindingRegistry.put(binding.injectionToken, binding);
    }

    boolean hasBinding(final InjectionToken<?> injectionToken) {
      return this.bindingRegistry.containsKey(injectionToken);
    }

    <T> T getInstance(final InjectionToken<T> injectionToken) {
      T instance = null;
      if (this.hasBinding(injectionToken)) {
        instance = this.getBinding(injectionToken).instance;
      }

      return instance;
    }

    boolean hasInstance(final InjectionToken<?> injectionToken) {
      boolean hasInstance = false;
      if (this.hasBinding(injectionToken)) {
        hasInstance = this.getBinding(injectionToken).instance != null;
      }

      return hasInstance;
    }

    Collection<Binding<?>> getBindings() {
      return this.bindingRegistry.values();
    }
  }

  public interface ScopedBindingBuilder<T> {

    InjectorBuilder toClass(final Class<? extends T> clazz);

    InjectorBuilder toProvider(final Class<? extends Provider<T>> clazz);
  }

  public interface ConfiguredBindingBuilder<T, U extends T> {

    ScopedBindingBuilder<T> withScope(final BindingScope bindingScope);

    InjectorBuilder toInstance(U instance);
  }

  public static class BindingBuilder<T, U extends T>
      implements ConfiguredBindingBuilder<T, U>, ScopedBindingBuilder<T> {

    private final Class<T> clazz;
    private BindingScope bindingScope = null;
    private final InjectorImpl container;
    private boolean validateOnCreation = false;

    private BindingBuilder(final Class<T> clazz, final InjectorImpl container) {
      this.clazz = clazz;
      this.container = container;
    }

    private BindingBuilder(final Class<T> clazz, final InjectorImpl container,
        final boolean validateOnCreation) {
      this.validateOnCreation = validateOnCreation;
      this.clazz = clazz;
      this.container = container;
    }

    @Override
    public InjectorBuilder toClass(final Class<? extends T> clazz) {
      final Binding<T> binding = new ClassBinding<>(this.clazz, clazz);
      binding.bindingScope = determineBindingScope(clazz);
      registerBinding(binding);

      return this.container;
    }

    @Override
    public InjectorBuilder toInstance(final U instance) {
      final Binding<T> binding = new InstanceBinding<>(this.clazz, instance);
      registerBinding(binding);

      return this.container;
    }

    @Override
    public InjectorBuilder toProvider(final Class<? extends Provider<T>> provider) {
      final Binding<T> binding = new ProviderBinding<>(this.clazz, provider);
      binding.bindingScope = determineBindingScope(provider);
      registerBinding(binding);

      return this.container;
    }

    @Override
    public ScopedBindingBuilder<T> withScope(final BindingScope bindingScope) {
      this.bindingScope = bindingScope;

      return this;
    }

    private void registerBinding(final Binding<?> binding) {
      this.container.registry.putBinding(binding);
      if (this.validateOnCreation) {
        this.container.validateBinding(binding.injectionToken);
      }
    }

    private BindingScope determineBindingScope(final Class<?> clazz) {
      if (this.bindingScope != null) {
        return this.bindingScope;
      }

      return SINGLETON_ANNOTATIONS.stream()
          .map(clazz::getAnnotation)
          .filter(Objects::nonNull)
          .findAny()
          .map(annotation -> BindingScope.SINGLETON)
          .orElse(BindingScope.TRANSIENT);
    }
  }

  public interface Provider<T> {

    T get();
  }

  static abstract class Dependency {

    Class<?> type;
    String qualifier;
    boolean lazy;

    public Dependency(final Class<?> type) {
      this.type = type;
    }
  }

  static class FieldDependency extends Dependency {

    Field field;

    public FieldDependency(final Field field) {
      super(field.getType());
      this.field = field;
      this.lazy = field.getAnnotation(Lazy.class) != null;
      this.qualifier = determineQualifier(field);
    }

    public Field getField() {
      return this.field;
    }
  }

  static class ConstructorDependency extends Dependency {

    public ConstructorDependency(final Parameter parameter) {
      super(parameter.getType());
      this.lazy = parameter.getAnnotation(Lazy.class) != null;
      this.qualifier = determineQualifier(parameter);
    }
  }

  private static class DependencyInformation {

    static DependencyInformation NONE = new DependencyInformation();

    List<FieldDependency> fields;
    Constructor<?> constructor;
    List<ConstructorDependency> constructorDependencies;

    public DependencyInformation(final Constructor<?> constructor, final List<Field> fields) {
      this.constructor = constructor;
      this.constructorDependencies = Arrays.stream(constructor.getParameters())
          .map(ConstructorDependency::new)
          .collect(Collectors.toList());
      this.fields = fields.stream()
          .map(FieldDependency::new)
          .collect(Collectors.toList());
    }

    private DependencyInformation() {
      this.constructor = null;
      this.fields = new ArrayList<>();
      this.constructorDependencies = new ArrayList<>();
    }

    List<ConstructorDependency> getConstructorDependencies() {
      return this.constructorDependencies;
    }

    List<FieldDependency> getFieldDependencies() {
      return this.fields;
    }

    List<Dependency> getAllDependencies() {
      final List<Dependency> dependencies = new ArrayList<>();
      dependencies.addAll(getConstructorDependencies());
      dependencies.addAll(getFieldDependencies());

      return dependencies;
    }
  }

  public enum BindingScope {
    SINGLETON,
    TRANSIENT
  }

  static class InjectionToken<T> {

    Class<T> clazz;
    String qualifier = null;

    public InjectionToken(final Class<T> clazz) {
      this.clazz = clazz;
    }

    InjectionToken<T> withQualifier(final String qualifier) {
      this.qualifier = qualifier;
      return this;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final InjectionToken<?> that = (InjectionToken<?>) obj;
      return this.clazz.equals(that.clazz) && Objects.equals(this.qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.clazz, this.qualifier);
    }
  }

  static abstract class Binding<T> {

    InjectionToken<T> injectionToken;
    DependencyInformation dependencyInformation;
    T instance = null;
    BindingScope bindingScope = BindingScope.TRANSIENT;

    public Binding(final Class<T> clazz) {
      this.injectionToken = new InjectionToken<>(clazz);
    }

    abstract T construct(Object[] constructorDependencies, Object[] fieldDependencies)
        throws java.lang.InstantiationException, IllegalAccessException, InvocationTargetException;

    protected DependencyInformation resolveDependencies(final Class<?> clazz) {
      final Constructor<?> constructor = getConstructor(clazz);
      final List<Field> injectionFields = getInjectionFields(clazz);

      return new DependencyInformation(constructor, injectionFields);
    }

    private boolean isInjectable(final AccessibleObject accessibleObject) {
      boolean isInjectable = false;
      for (final Class<? extends Annotation> injectAnnotation : INJECT_ANNOTATIONS) {
        isInjectable = accessibleObject.getAnnotation(injectAnnotation) != null;
        if (isInjectable) {
          break;
        }
      }

      return isInjectable;
    }

    private Constructor<?> getConstructor(final Class<?> clazz) {
      Constructor<?> constructor = null;
      for (final Constructor<?> declaredConstructor : clazz.getDeclaredConstructors()) {
        if (isInjectable(declaredConstructor)) {
          constructor = declaredConstructor;
          break;
        }
      }

      if (constructor == null) {
        try {
          constructor = clazz.getDeclaredConstructor();
          final int modifiers = constructor.getModifiers();
          if (!Modifier.isPublic(modifiers)) {
            throw new IllegalAccessException();
          }
        } catch (final NoSuchMethodException | IllegalAccessException e) {
          throw new MissingConstructorException(clazz);
        }
      }

      return constructor;
    }

    private List<Field> getInjectionFields(final Class<?> clazz) {
      final Field[] fields = clazz.getDeclaredFields();

      final List<Field> dependencyFields = new ArrayList<>();
      for (final Field field : fields) {
        final boolean isInjectable = isInjectable(field);

        if (isInjectable) {
          final boolean isLazy = field.getAnnotation(Lazy.class) != null;
          if (isLazy && !field.getType().isInterface()) {
            throw new InvalidLazyAnnotation(clazz);
          }

          dependencyFields.add(field);
        }
      }

      return dependencyFields;
    }

    List<ConstructorDependency> getConstructorDependencies() {
      return this.dependencyInformation.getConstructorDependencies();
    }

    List<FieldDependency> getFieldDependencies() {
      return this.dependencyInformation.getFieldDependencies();
    }

    protected void injectFieldDependencies(final Object instance, final Object[] dependencies)
        throws IllegalAccessException {
      final List<Field> fieldList = this.dependencyInformation.fields.stream()
          .map(FieldDependency::getField)
          .collect(Collectors.toList());

      for (int i = 0; i < fieldList.size(); i++) {
        final Field field = fieldList.get(i);
        final Object dependencyInstance = dependencies[i];

        field.setAccessible(true);
        field.set(instance, dependencyInstance);
      }
    }
  }

  static class ClassBinding<T, U extends T> extends Binding<T> {

    Class<U> boundClazz;

    public ClassBinding(final Class<T> clazz, final Class<U> boundClazz) {
      super(clazz);
      this.boundClazz = boundClazz;
      this.injectionToken.withQualifier(determineQualifier(boundClazz));
      this.dependencyInformation = resolveDependencies(boundClazz);
    }

    @Override
    public T construct(final Object[] constructorDependencies, final Object[] fieldDependencies)
        throws java.lang.InstantiationException, IllegalAccessException, InvocationTargetException {
      final T instance = (T) this.dependencyInformation.constructor.newInstance(
          constructorDependencies);
      injectFieldDependencies(instance, fieldDependencies);

      if (this.bindingScope == BindingScope.SINGLETON) {
        this.instance = instance;
      }

      return instance;
    }
  }

  static class InstanceBinding<T, U extends T> extends Binding<T> {

    public InstanceBinding(final Class<T> clazz, final U instance) {
      super(clazz);
      this.instance = instance;
      this.injectionToken.withQualifier(determineQualifier(instance.getClass()));
      this.dependencyInformation = DependencyInformation.NONE;
    }

    @Override
    T construct(final Object[] constructorDependencies, final Object[] fieldDependencies) {
      return this.instance;
    }
  }

  static class ProviderBinding<T> extends Binding<T> {

    public ProviderBinding(final Class<T> clazz, final Class<? extends Provider<T>> providerClass) {
      super(clazz);
      this.injectionToken.withQualifier(determineQualifier(providerClass));
      this.dependencyInformation = resolveDependencies(providerClass);
    }

    @Override
    T construct(final Object[] constructorDependencies, final Object[] fieldDependencies)
        throws IllegalAccessException, java.lang.InstantiationException, InvocationTargetException {
      final Provider<T> provider =
          (Provider<T>) this.dependencyInformation.constructor.newInstance(constructorDependencies);
      injectFieldDependencies(provider, fieldDependencies);

      final T instance = provider.get();
      if (this.bindingScope == BindingScope.SINGLETON) {
        this.instance = instance;
      }

      return instance;
    }
  }

  public static class InstantiationException extends RuntimeException {

    public InstantiationException(final Throwable cause) {
      super(cause);
    }
  }

  public static class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(final InjectionToken<?> injectionToken) {
      super(
          "Circular dependency detected for class " + injectionToken.clazz.getSimpleName() +
              injectionToken.qualifier != null ? " and qualifier " + injectionToken.qualifier : ""
      );
    }
  }

  public static class MissingBindingException extends RuntimeException {

    public <T> MissingBindingException(final InjectionToken<T> injectionToken) {
      super(
          "Missing binding detected for class " + injectionToken.clazz.getSimpleName() +
              injectionToken.qualifier != null ? " and qualifier " + injectionToken.qualifier : ""
      );
    }
  }

  public static class MissingConstructorException extends RuntimeException {

    public <T> MissingConstructorException(final Class<T> clazz) {
      super("Missing or inaccessible constructor detected for class.\n" +
          "Please check that the constructor that is used for injection is public.\n" +
          "Class: " + clazz);
    }
  }

  public static class InvalidLazyAnnotation extends RuntimeException {

    public InvalidLazyAnnotation(final Class<?> clazz) {
      super("Class contains invalid configuration for lazy initialisation.\n" +
          "Lazy init is only allowed for interfaces.\n" +
          "Please check the configuration for class: " + clazz);
    }
  }

  static String determineQualifier(final AnnotatedElement annotatedElement) {
    Annotation qualifier = null;
    for (final Annotation declaredAnnotation : annotatedElement.getDeclaredAnnotations()) {
      if (declaredAnnotation.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifier = declaredAnnotation;
        break;
      }
    }

    if (qualifier == null) {
      return null;
    }

    final String qualifierString;
    if (qualifier instanceof Named) {
      qualifierString = ((Named) qualifier).value();
    } else {
      qualifierString = qualifier.annotationType().getCanonicalName();
    }

    return qualifierString;
  }

  public static InjectorBuilder create() {
    return new InjectorImpl();
  }

  private MiniDI() {

  }
}
