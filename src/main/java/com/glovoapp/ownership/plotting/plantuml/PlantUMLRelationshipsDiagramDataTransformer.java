package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Arrow.Attribute.BOLD;
import static com.glovoapp.ownership.plotting.plantuml.Utils.RANDOM;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.DiagramDataTransformer;
import com.glovoapp.ownership.plotting.plantuml.Arrow.HeadStyle;
import com.glovoapp.ownership.plotting.plantuml.Arrow.LineStyle;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
public final class PlantUMLRelationshipsDiagramDataTransformer implements DiagramDataTransformer<SourceStringReader> {

    private final DiagramConfiguration diagramConfiguration;

    /**
     * @param diagramConfiguration global diagram settings
     */
    public PlantUMLRelationshipsDiagramDataTransformer(final DiagramConfiguration diagramConfiguration) {
        this.diagramConfiguration = diagramConfiguration;
    }

    @Override
    public final SourceStringReader transformToDiagramData(final Set<ClassOwnership> domainOwnership) {
        final StringBuilder diagram = new StringBuilder("@startuml\n").append(diagramConfiguration.render());

        final Set<Component> components = generateComponentsFromDomain(domainOwnership);
        final Set<Owner> owners = createOwnersFromComponents(components);
        final Set<Component> componentsWithNoOwners = getComponentsWithNoOwners(components);

        Stream.concat(owners.stream(), componentsWithNoOwners.stream())
              /* TODO: this casting is only to fix BootstrapMethodError, remove casting in the future.
               *       This line is throwing:
               *           Invalid receiver type
               *             interface com.glovoapp.ownership.plotting.plantuml.Identifiable;
               *           not a subtype of implementation type
               *             interface com.glovoapp.ownership.plotting.plantuml.Renderable
               *       Which is obviously wrong and Java should understand that, but it doesnt.
               *       Therefore we are explicitly casting so it can stop complaining.
               *       See full stack trace in commit message.
               */
              .map(it -> (Renderable) it)
              .map(Renderable::render)
              .forEach(diagram::append);

        Stream.concat(getAllDependencyRelationships(components), getAllMethodRelationships(components, owners))
              .map(Relationship::render)
              .forEach(diagram::append);

        diagram.append("@enduml\n");
        final String resultDiagram = diagram.toString();

        log.info("generated diagram with {} lines", countLines(resultDiagram));
        return new SourceStringReader(resultDiagram);
    }

    private Stream<Relationship> getAllMethodRelationships(final Set<Component> components, final Set<Owner> owners) {
        return components.stream()
                         .flatMap(component ->
                             component.getClassOwnership()
                                      .getMethodOwners()
                                      .values()
                                      .stream()
                                      .distinct()
                                      .map(uniqueOwnerName ->
                                          owners.stream()
                                                .filter(owner ->
                                                    !Objects.equals(
                                                        owner.getName(),
                                                        component.getClassOwnership()
                                                                 .getClassOwner()
                                                    )
                                                )
                                                .filter(owner -> Objects.equals(uniqueOwnerName, owner.getName()))
                                                .findAny()
                                      )
                                      .filter(Optional::isPresent)
                                      .map(Optional::get)
                                      .map(owner ->
                                          new Relationship(
                                              component,
                                              owner,
                                              Arrow.builder()
                                                   .lineStyle(LineStyle.DOTTED)
                                                   .length(1 + RANDOM.nextInt(3))
                                                   .randomColor()
                                                   .build()
                                          )
                                      )
                         );
    }

    private Stream<Relationship> getAllDependencyRelationships(Set<Component> components) {
        return components.stream()
                         .flatMap(component ->
                             component.getClassOwnership()
                                      .getDependencyOwnershipsStream()
                                      .map(Entry::getValue)
                                      .flatMap(dependencyOwnership ->
                                          components.stream()
                                                    .filter(anotherComponent ->
                                                        anotherComponent.getClassOwnership()
                                                                        .equals(
                                                                            dependencyOwnership)
                                                    )
                                      )
                                      .map(dependencyComponent ->
                                          new Relationship(
                                              component,
                                              dependencyComponent,
                                              Arrow.builder()
                                                   .attributes(singletonList(BOLD))
                                                   .headStyle(HeadStyle.FULL)
                                                   .length(1 + RANDOM.nextInt(3))
                                                   .randomColor()
                                                   .build()
                                          )
                                      )
                         );
    }

    private Set<Component> getComponentsWithNoOwners(final Set<Component> components) {
        return components.stream()
                         .filter(component -> component.getClassOwnership()
                                                       .getClassOwner() == null)
                         .collect(toSet());
    }

    private Set<Owner> createOwnersFromComponents(final Set<Component> components) {
        return components.stream()
                         .filter(component -> component.getClassOwnership()
                                                       .getClassOwner() != null)
                         .collect(toMap(
                             component -> component.getClassOwnership()
                                                   .getClassOwner(),
                             Collections::singleton,
                             Utils::merge
                         ))
                         .entrySet()
                         .stream()
                         .map(entry -> new Owner(entry.getKey(), entry.getValue()))
                         .collect(toSet());
    }

    private Set<Component> generateComponentsFromDomain(final Set<ClassOwnership> domainOwnership) {
        return domainOwnership.stream()
                              .map(Component::new)
                              .collect(toSet());
    }

    @SneakyThrows
    private static long countLines(final String input) {
        return Optional.ofNullable(input)
                       .map(it -> it.chars()
                                    .filter(character -> '\n' == character)
                                    .count() + 1)
                       .orElse(0L);
    }

}
