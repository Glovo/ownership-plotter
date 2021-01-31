package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.shared.Sets.union;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.glovoapp.diagrams.Component;
import com.glovoapp.diagrams.Component.SimpleComponent;
import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.Diagram.SimpleDiagram;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.diagrams.Relationship.SimpleRelationship;
import com.glovoapp.ownership.ClassOwnership;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class RelationshipsDiagramDataFactory implements OwnershipDiagramFactory<ClassRelationship> {

    @Override
    public final <Id extends Identifier<Id>> Diagram<Id, ClassRelationship> createOwnershipDiagram(
        final Set<ClassOwnership> domainOwnership,
        final IdentifierGenerator<Id> idGenerator
    ) {
        final Map<ClassOwnership, Component<Id>> ownershipsToClassComponents
            = getOwnershipsToClassComponents(domainOwnership, idGenerator);

        final Map<String, Component<Id>> ownerNamesToOwnerComponents
            = getOwnerNamesToOwnerComponents(domainOwnership, idGenerator, ownershipsToClassComponents);

        final Map<ClassOwnership, Component<Id>> ownershipsToComponentsWithNoOwners
            = getOwnershipsToComponentsWithNoOwners(ownershipsToClassComponents);

        return new SimpleDiagram<>(
            Stream.concat(
                ownerNamesToOwnerComponents.values()
                                           .stream(),
                ownershipsToComponentsWithNoOwners.values()
                                                  .stream()
            )
                  .collect(toSet()),
            union(
                getCompositionRelationships(ownershipsToClassComponents),
                getMethodRelationships(ownershipsToClassComponents, ownerNamesToOwnerComponents)
            )
        );
    }

    private static <Id extends Identifier<Id>> Set<SimpleRelationship<Id, ClassRelationship>> getMethodRelationships(
        final Map<ClassOwnership, Component<Id>> ownershipsToClassComponents,
        final Map<String, Component<Id>> ownerNamesToOwnerComponents
    ) {
        return ownershipsToClassComponents.entrySet()
                                          .stream()
                                          .flatMap(ownershipToClassComponent ->
                                              ownershipToClassComponent.getKey()
                                                                       .getMethodOwners()
                                                                       .values()
                                                                       .stream()
                                                                       .distinct()
                                                                       .filter(methodOwner ->
                                                                           !Objects.equals(
                                                                               ownershipToClassComponent.getKey()
                                                                                                        .getClassOwner(),
                                                                               methodOwner
                                                                           )
                                                                       )
                                                                       .map(ownerNamesToOwnerComponents::get)
                                                                       .filter(Objects::nonNull)
                                                                       .map(methodOwnerComponent ->
                                                                           new SimpleRelationship<>(
                                                                               ClassRelationship.USES,
                                                                               ownershipToClassComponent.getValue(),
                                                                               methodOwnerComponent
                                                                           )
                                                                       )
                                          )
                                          .collect(toSet());
    }

    private <Id extends Identifier<Id>> Set<SimpleRelationship<Id, ClassRelationship>> getCompositionRelationships(
        Map<ClassOwnership, Component<Id>> ownershipsToClassComponents) {
        return ownershipsToClassComponents.entrySet()
                                          .stream()
                                          .flatMap(
                                              ownershipToComponent ->
                                                  ownershipToComponent.getKey()
                                                                      .getDependencyOwnershipsStream()
                                                                      .map(
                                                                          Entry::getValue)
                                                                      .map(
                                                                          ownershipsToClassComponents::get)
                                                                      .filter(
                                                                          Objects::nonNull)
                                                                      .map(
                                                                          dependencyComponent ->
                                                                              new SimpleRelationship<>(
                                                                                  ClassRelationship.COMPOSES,
                                                                                  ownershipToComponent.getValue(),
                                                                                  dependencyComponent
                                                                              )
                                                                      )
                                          )
                                          .collect(toSet());
    }

    private static <Id extends Identifier<Id>> Map<ClassOwnership, Component<Id>> getOwnershipsToComponentsWithNoOwners(
        final Map<ClassOwnership, Component<Id>> ownershipsToClassComponents
    ) {
        return ownershipsToClassComponents.entrySet()
                                          .stream()
                                          .filter(ownershipToComponent ->
                                              ownershipToComponent.getKey()
                                                                  .getClassOwner() == null
                                          )
                                          .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private static <Id extends Identifier<Id>> Map<String, Component<Id>> getOwnerNamesToOwnerComponents(
        final Set<ClassOwnership> domainOwnership,
        final IdentifierGenerator<Id> idGenerator,
        final Map<ClassOwnership, Component<Id>> ownershipsToClassComponents
    ) {
        return domainOwnership.stream()
                              .flatMap(classOwnership ->
                                  Stream.concat(
                                      Optional.of(classOwnership)
                                              .map(ClassOwnership::getClassOwner)
                                              .map(Stream::of)
                                              .orElseGet(Stream::empty),
                                      classOwnership.getMethodOwners()
                                                    .values()
                                                    .stream()
                                  )
                              )
                              .distinct()
                              .map(ownerName ->
                                  new SimpleComponent<>(
                                      idGenerator.generate(),
                                      ownerName,
                                      ownershipsToClassComponents.entrySet()
                                                                 .stream()
                                                                 .filter(ownershipToComponent ->
                                                                     Objects.equals(
                                                                         ownerName,
                                                                         ownershipToComponent.getKey()
                                                                                             .getClassOwner()
                                                                     )
                                                                 )
                                                                 .map(Entry::getValue)
                                                                 .collect(toSet())
                                  )
                              )
                              .collect(toMap(
                                  SimpleComponent::getName,
                                  identity()
                              ));
    }

    private static <Id extends Identifier<Id>> Map<ClassOwnership, Component<Id>> getOwnershipsToClassComponents(
        final Set<ClassOwnership> domainOwnership,
        final IdentifierGenerator<Id> idGenerator
    ) {
        return domainOwnership.stream()
                              .filter(classOwnership -> !classOwnership.getTheClass()
                                                                       .getSimpleName()
                                                                       .isEmpty())
                              .collect(
                                  toMap(
                                      identity(),
                                      classOwnership -> new SimpleComponent<>(
                                          idGenerator.generate(),
                                          classOwnership.getTheClass()
                                                        .getSimpleName(),
                                          emptySet()
                                      )
                                  )
                              );
    }

}
