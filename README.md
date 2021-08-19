# Ownership Plotter

A tool for visualizing bounded context based on annotations present in the code.

It is designed with customization in mind: you can choose which parts of your
application to visualize.

Example diagram from the "perspective" of `TEAM_A`:

![Example diagram](example-diagram.svg)

## Usage

### Prerequisites

The tool depends on
[PlantUML](https://mvnrepository.com/artifact/net.sourceforge.plantuml/plantuml)
for generating diagrams, which in turn requires
[Graphviz](https://graphviz.org/download) to be installed on your machine:

```console
brew install graphviz
```

### Installation

To make Ownership Plotter available locally, `install` it into the local Maven
repository by running the following:

```console
mvn clean install -DskipTests
```

This will put all `.jar` files in your `~/.m2` directory, making the tool
visible for other projects.

### Setup in project

_NOTE:_ Use `PlantUML` version from
[this very project](https://github.com/Glovo/ownership-plotter/blob/master/pom.xml).

To use the tool in your project, add it as Maven or Gradle dependency:

```xml
<dependency>
  <groupId>com.glovoapp</groupId>
  <artifactId>ownership-plotter</artifactId>
  <version>${ownershipPlotter.version}</version>
</dependency>
<dependency>
  <groupId>net.sourceforge.plantuml</groupId>
  <artifactId>plantuml</artifactId>
  <version>${PlantUML.version}</version>
</dependency>
```

```groovy
testImplementation "com.glovoapp:ownership-plotter:${ownershipPlotter.version}"
testImplementation "net.sourceforge.plantuml:plantuml:${PlantUML.version}"
```

### Creating the diagram

The easiest way to create a domain diagram is with a test, for example:

```java
class PlotOwnershipTest {

    @Test
    void shouldPlotOwnership() {
        new ClassOwnershipPlotter(
            new ReflectionsClasspathLoader(),
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    define(YourOwnershipAnnotation.class, YourOwnershipAnnotation::owner)
                )
            ),
            it -> it,
            OwnershipDiagramPipeline.of(
                new PlantUMLIdentifierGenerator(),
                new RelationshipsDiagramDataFactory(),
                new PlantUMLDiagramRenderer(FileFormat.SVG),
                new DiagramToFileDataSink(new File("com-example-domain.svg"))
            )
        ).createClasspathDiagram("com.example");
    }

}
```

You can filter the classes that you wish to include in the diagram by adding
"ownership filters":

```java
DomainOwnershipFilter.simple(
    isOwnedBy("my-wonderful-team").or(
       isADependencyOfAClassThat(isOwnedBy("my-wonderful-team"))
    )
)
```

If the class matches any of the filters, it will be included in the result. In
the example above, all classes owned by "my-wonderful-team" or all dependencies
of classes owned by "my-wonderful-team" will be included.

There are various filters available. You can compose them to generate the exact
diagram you are looking for.

#### Supported diagrams

Two types of diagrams are supported out of the box:

1. Relationship diagram (`RelationshipsDiagramDataFactory`) - relationships
   between teams and classes
1. Features diagram (`FeaturesDiagramDataFactory`) - each team's "features" or
   "domains", similar to a service blueprint

You may create your own type of diagram by implementing the
`OwnershipDiagramFactory` interface.

#### Supported backends

Currently, only PlantUML-generated diagrams are supported via the
`PlantUMLDiagramRenderer` class.

You may add your preferred diagram generation tool by implementing the
`DiagramRenderer` interface.

#### Performance tweaks

##### Parallelized filtering

If your project contains more than a thousand classes, the filtering process may
get a bit slow. To mitigate this, a `parallelized` version of
`DomainOwnershipFilter` can be used:

```java
final int coresCount = Runtime.getRuntime()
                              .availableProcessors();
final DomainOwnershipFilter filter = DomainOwnershipFilter.parallelized(
    it -> true, // your filter here
    newFixedThreadPool(coresCount),
    coresCount
);
```

Any thread pool may be used instead of a fixed one.

The `partitionsCount` dictates how the domain will be split for filtering. Each
partition will be passed to a separate thread. This means that if threads count
is greater than partitions count, some threads in the pool will not be utilized.

##### Caching

If the complexity of used filter is particularly large, you may wish to cache
the filtering results.

```java
ClassOwnershipFilter filter = isOwnedBy("my-wonderful-team").cached();
```

Note this operation will make the filter stateful and may use _lots_ of memory.

##### Debugging

If the performance issues are not fixed by parallelization or caching, you might
want to debug your filters. The helpful `.debugged()` method will create a new
filter that logs potentially important metrics:

```java
ClassOwnershipFilter filter = isOwnedBy("my-wonderful-team").debugged();
```

#### Important note

The `writeDiagramOfClasspathToFile` method uses `reflections` library to scan
the entire classpath. This will effectively load all available classes.

**The tool should not be used outside of tests to avoid decreasing performance
of your application.**
