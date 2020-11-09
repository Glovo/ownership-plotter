package com.glovoapp.ownership.plotting.plantuml;

import static java.util.UUID.randomUUID;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.DiagramDataTransformer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.java.Log;
import net.sourceforge.plantuml.SourceStringReader;

@Log
public final class PlantUMLDiagramDataTransformer implements DiagramDataTransformer<SourceStringReader> {

    private static final String DIAGRAM_CONFIGURATION
        // Owners (teams) are represented by packages
        = "skinparam package {\n"
        + "  backgroundColor LightGray\n"
        + "  borderColor LightGray\n"
        + "}\n"
        + "skinparam storage {\n" // storages are for empty packages (owners with no classes)
        + "  shadowing false\n"
        + "  fontColor LightGray\n"
        + "  backgroundColor LightGray\n"
        + "  borderColor LightGray\n"
        + "}\n\n"
        // Classes are represented by folders
        + "skinparam folder {\n"
        + "  backgroundColor DarkGray\n"
        + "  borderColor DarkGray\n"
        + "}\n"
        + "skinparam agent {\n" // agents are for empty folders (classes with no methods)
        + "  shadowing false\n"
        + "  fontColor DarkGray\n"
        + "  backgroundColor DarkGray\n"
        + "  borderColor DarkGray\n"
        + "}\n\n"
        // Methods are represented by rectangles
        + "skinparam rectangle {\n"
        + "  backgroundColor white\n"
        + "  borderColor black\n"
        + "}\n";

    @Override
    public final SourceStringReader transformToDiagramData(final Collection<ClassOwnership> domainOwnership) {
        final IdContainer idContainer = new IdContainer();
        final StringBuilder diagram = new StringBuilder("@startuml\n").append(DIAGRAM_CONFIGURATION);

        domainOwnership.stream()
                       .flatMap(ownership -> Stream.concat(
                           Stream.of(ownership.getClassOwner())
                                 .filter(Objects::nonNull),
                           ownership.getMethodOwners()
                                    .values()
                                    .stream()
                       ))
                       .distinct()
                       .forEach(owner -> {
                           final String ownerId = idContainer.putAndGetId(owner);
                           final String classesDefinitions = getClassesDefinitions(domainOwnership, idContainer, owner);
                           diagram.append("package ")
                                  .append(owner)
                                  .append(" as ")
                                  .append(ownerId)
                                  .append(" {\n")
                                  .append(
                                      classesDefinitions.isEmpty()
                                           /*
                                            TODO: When there are no classes, we need to render an invisible element,
                                             otherwise the whole package gets messed up.
                                           */
                                          ? "    storage invisible_" + IdContainer.generateRandomId() + '\n'
                                          : classesDefinitions
                                  )
                                  .append("}\n");
                       });

        // Draw class ownerships
        domainOwnership.stream()
                       .filter(ownership -> !ownership.getDependenciesOwnership()
                                                      .isEmpty())
                       .forEach(ownership -> {
                           final String classId = idContainer.getId(ownership.getTheClass())
                                                             .orElseThrow(NullPointerException::new);
                           ownership.getDependencyOwnershipsStream()
                                    .map(Entry::getValue)
                                    .forEach(dependencyOwnership -> {
                                        final String dependencyClassId
                                            = idContainer.getId(dependencyOwnership.getTheClass())
                                                         .orElseThrow(NullPointerException::new);

                                        diagram.append(classId)
                                               .append(" =[#E74C3C,bold]==|> ")
                                               .append(dependencyClassId)
                                               .append('\n');
                                    });
                       });

        // Draw method ownerships
        domainOwnership.forEach(ownership ->
            ownership.getMethodOwners()
                     .entrySet()
                     .stream()
                     .filter(methodOwner ->
                         !methodOwner.getValue()
                                     .equals(ownership.getClassOwner())
                     )
                     .forEach(methodOwner -> {
                         final String methodId = idContainer.getId(methodOwner.getKey())
                                                            .orElseThrow(NullPointerException::new);
                         final String ownerId = idContainer.getId(methodOwner.getValue())
                                                           .orElseThrow(NullPointerException::new);
                         diagram.append(methodId)
                                .append(" -[#DC7633,dashed]--> ")
                                .append(ownerId)
                                .append('\n');
                     })
        );

        diagram.append("@enduml\n");
        final String resultDiagram = diagram.toString();

        log.info("generated diagram:\n" + resultDiagram);
        return new SourceStringReader(resultDiagram);
    }

    private String getClassesDefinitions(final Collection<ClassOwnership> domainOwnership,
                                         final IdContainer idContainer,
                                         final String owner) {
        final StringBuilder diagram = new StringBuilder();
        domainOwnership.stream()
                       .filter(it -> owner.equals(it.getClassOwner()))
                       .forEach(ownedClass -> {
                           final String classId = idContainer.putAndGetId(ownedClass.getTheClass());
                           diagram.append("  folder ")
                                  .append(ownedClass.getTheClass()
                                                    .getSimpleName())
                                  .append(" as ")
                                  .append(classId);

                           final String methodDefinitions = getMethodDefinitions(
                               ownedClass,
                               idContainer
                           );

                           diagram.append(" {\n")
                                  .append(
                                      methodDefinitions.isEmpty()
                                          /*
                                           TODO: When there are no methods, we need to render an invisible element,
                                            otherwise the whole folder gets messed up.
                                           */
                                          ? "    agent invisible_" + IdContainer.generateRandomId() + '\n'
                                          : methodDefinitions
                                  )
                                  .append("  }\n");
                       });
        return diagram.toString();
    }

    private static String getMethodDefinitions(final ClassOwnership ownedClass, final IdContainer idContainer) {
        return ownedClass.getMethodOwners()
                         .keySet()
                         .stream()
                         .map(method -> {
                             final String methodId = idContainer.putAndGetId(method);
                             return "    rectangle " + method.getName() + " as " + methodId + '\n';
                         })
                         .collect(Collectors.joining());
    }

    /**
     * Used to ensure each element of the diagram has a unique ID.
     */
    private static final class IdContainer {

        private final HashMap<Object, String> objectIds = new HashMap<>();

        private String putAndGetId(final Object object) {
            return getId(object).orElseGet(() -> {
                final String newId = generateRandomId();
                objectIds.put(object, newId);
                return newId;
            });
        }

        private Optional<String> getId(final Object object) {
            return Optional.of(object)
                           .map(objectIds::get);
        }

        /**
         * @return unique ID compatible with PlantUML naming convention
         */
        private static String generateRandomId() {
            return randomUUID().toString()
                               .replace("-", "_");
        }

    }

}
