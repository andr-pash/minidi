# MiniDI
A minimalist approach to DI for Java apps and libraries. 

## Motivation
Letting a DI solution take care of object instantiation can benefit most projects.
But sometimes it is not feasible to get out the big guns (Spring, Guice,...) or even pico container.
To fill this gap MiniDI was created. A single file implementation with zero dependencies that if need be can just be included as source in your project.

## Caveats
There is no code generation involved, which means the implementation relies solely on reflection.
This comes at a performance cost compared to other libraries.
 
## Usage
Get it from maven central or just include the source in your project.

```
<dependency>

</dependency>
```

### Basic example
Imagine a use case where you have a booking service, that requires a payment service implementation to charge for an incoming booking.

```
class BookingService 
{
    @MiniDI.Inject
    private PaymentService paymentService;

    public void createBooking( )
    {
        ...
        this.paymentService.charge( bookingFee );
    }

}
```

We can now create an injector, where we tell MiniDI how to fulfill the dependencies.

In this case we want to use the concrete `CreditCardPaymentService` implementation when a `PaymentServcie` is requested.
```
final MiniDI.Injector injector = MiniDI.create( )
    .bind( PaymentService.class ).toClass( CreditCardPaymentService.class )
    .bind( BookingService.class ).toClass( BookingService.class )
    .initialize( );
```

Now we can request a `BookingService` instance from our injector.
```
injector.get( BookingService.class ).createBooking( );
```


For an overview of the complete feature set and concepts please take a look at the [documentation](https://andr-pash.github.io/MiniDI).

## Run the project
```
git clone https://...
cd 
mvn test
```

## Contribute
Currently closed for contribution