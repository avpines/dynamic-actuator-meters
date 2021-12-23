# Dynamic Actuator Meters

Enables creating meters with dynamic tag values, will register the new meters behind the scenes as
needed.

## Supported Meters

Currently, **Counters**, **Timers**, **Gauges**, and **DistributionSummaries** are supported.

## Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.avpines</groupId>
    <artifactId>dynamic-actuator-meters</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
```

## Example usage

Create a new dynamic counter with tag keys "t1" and "t2":

```java
DynamicCounter dc = DynamicCounter.builder(registry, "my.meter.name") 
    .tagKeys("t1", "t2")     
    .build();
```

We can create counters with these tag keys and each has its own dynamic value:

```java
// Create a counter with the tags ("t1", "t1-v1") and ("t2", "t2-v1")
Counter c1 = dc.getOrCreate("v1", "v2");
c1.increment(100);

// Create a counter with the tags ("t1", "t1-v2") and ("t2", "t2-v2")
dc.getOrCreate("v1", "v2").increment(100);
```

If we need to define more characteristics to the counter, e.g., add a base unit, we can do it via
a customizer.

```java
DynamicCounter dc = DynamicCounter.builder(registry, "my.meter.name") 
    .tagKeys("t1", "t2")     
    .customizer(b -> b.baseUnit("boatloads").description("my description"))
    .build();
```

Each counter created will have the base unit and description defined. This allows you to control
the meter just as you would have if you have created it directly.

### Gauges

Some meters, such as gauges, are a bit more complex and require more parameters when built. This
is also supported. For example, if we want to create a dynamic gauge the operates on a list and 
measures all the strings in the list with length between 1 to 10:

```java
DynamicGauge<List<String>> dg = DynamicGauge
    .builder(smr, name, new OfType<List<String>>() {})
    .tagKeys("min", "max").build();

dg.getOrCreate(
    list, 
    l -> l.stream().filter(s -> s.length() >= 1 && s.length() <= 10, 
    "min-1", "max-10");
```

You can use `get` instead of `getOrCreate` to get the gauge only if it exists. This way you don't
have to give the object and function each time:

```java
DynamicGauge<Set<Double>> dg = DynamicGauge
    .builder(smr, "hello", new OfType<Set<Double>>() {})
    .tagKey("t1")
    .build();

// create a gauge
Gauge g = dg.getOrCreate(set, s -> s.stream().filter(d -> d > 10.0).count(), "hello");

Optional<Gauge> another1 = dg.get("hello"); // exists
Optional<Gauge> another2 = dg.get("world"); // empty, gauge was not previosuly created.
```

To levarage Gauge's special `Supplier<Number>` implementation (see 
`Gauge#builder(String name, Supplier<Number> f)`), you can use the `SupplierDynamicGauge`:

```java
AtomicInteger ai = new AtomicInteger();

SupplierDynamicGauge dg = DynamicGauge.builder(smr, "my.metric").tagKeys("hello").build();
dg.getOrCreate(ai::get, "add-1"); 

ai.set(10);

List<Meter> meters = smr.getMeters();
assertThat(meters)
    .filteredOn(Conditions.onTags(Tag.of("hello", "add-1"))).hasSize(1)
    .map(m -> ((Gauge) m).value()).containsExactly(10.0);
```