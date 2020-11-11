package com.glovoapp.ownership.plotting.plantuml;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.DiagramDataTransformer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
public final class PlantUMLDiagramDataTransformer implements DiagramDataTransformer<SourceStringReader> {

    private static final Random RANDOM = new Random();
    private static final String DIAGRAM_CONFIGURATION
        // Owners (teams) are represented by packages
        = "\n/' colors configuration '/\n"
        + "skinparam package {\n"
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

    private static String randomArrowColor() {
        final List<Integer> cmyList = asList(77, 0, RANDOM.nextInt(77));
        Collections.shuffle(cmyList);
        return cmykToRgb(cmyList.get(0), cmyList.get(1), cmyList.get(2), 14);
    }

    private static String cmykToRgb(final int c, final int m, final int y, final int k) {
        int r = (int) (255 * (1.0 - c / 100.0) * (1.0 - k / 100.0));
        int g = (int) (255 * (1.0 - m / 100.0) * (1.0 - k / 100.0));
        int b = (int) (255 * (1.0 - y / 100.0) * (1.0 - k / 100.0));

        return "#" + hexPart(r) + hexPart(g) + hexPart(b);
    }

    private static String hexPart(final int value) {
        final String asHex = Integer.toHexString(value)
                                    .toUpperCase();
        return asHex.length() != 2 ? '0' + asHex : asHex;
    }

    private static String randomRepeat(final int minCount, final int maxCount, final String toRepeat) {
        final int repeatTimes = RANDOM.nextInt(maxCount - minCount) + minCount;
        return IntStream.range(0, repeatTimes)
                        .mapToObj(it -> toRepeat)
                        .collect(Collectors.joining());
    }

    @Override
    public final SourceStringReader transformToDiagramData(final Object ownerPerspective,
                                                           final Collection<ClassOwnership> domainOwnership) {
        final String desiredOwner = ownerPerspective == null ? null : ownerPerspective.toString();
        final IdContainer idContainer = new IdContainer();
        final StringBuilder diagram = new StringBuilder("@startuml\n").append(DIAGRAM_CONFIGURATION);
        final List<String> drawLater = new ArrayList<>();

        diagram.append("\n/' domain definitions '/\n");
        domainOwnership.stream()
                       .filter(ownership ->
                           desiredOwner == null
                               || desiredOwner.equals(ownership.getClassOwner())
                               || ownership.getMethodOwners()
                                           .containsValue(desiredOwner)
                               || ownership.getDependencyOwnershipsStream()
                                           .map(Entry::getValue)
                                           .map(ClassOwnership::getClassOwner)
                                           .anyMatch(desiredOwner::equals)
                               || domainOwnership.stream()
                                                 .filter(it -> desiredOwner.equals(it.getClassOwner()))
                                                 .flatMap(ClassOwnership::getDependencyOwnershipsStream)
                                                 .map(Entry::getValue)
                                                 .map(ClassOwnership::getClassOwner)
                                                 .anyMatch(it -> Objects.equals(it, ownership.getClassOwner()))
                       )
                       .flatMap(ownership -> Stream.concat(
                           Stream.of(ownership.getClassOwner())
                                 .filter(Objects::nonNull),
                           ownership.getMethodOwners()
                                    .values()
                                    .stream()
                       ))
                       .distinct()
                       .forEach(owner -> {
                           final String classesDefinitions = getClassesDefinitions(
                               desiredOwner, domainOwnership, idContainer, owner, drawLater
                           );

                           if (
                               // Ignore owners that have no relation to desired owner
                               desiredOwner != null
                                   && classesDefinitions.isEmpty()
                                   && domainOwnership.stream()
                                                     .filter(it -> desiredOwner.equals(it.getClassOwner()))
                                                     .map(ClassOwnership::getMethodOwners)
                                                     .map(Map::values)
                                                     .flatMap(Collection::stream)
                                                     .noneMatch(owner::equals)

                           ) {
                               log.info("ignoring because no relation to desired owner {}", owner);
                               return;
                           }

                           final String ownerId = idContainer.putAndGetId(owner);
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

        diagram.append("\n/' classes with no ownership that have methods with ownership '/\n");
        domainOwnership.stream()
                       .filter(ownership -> ownership.getClassOwner() == null)
                       .filter(ownership -> !ownership.getMethodOwners()
                                                      .isEmpty())
                       .filter(ownership ->
                           desiredOwner == null
                               || ownership.getMethodOwners()
                                           .values()
                                           .stream()
                                           .anyMatch(desiredOwner::equals)
                       )
                       .map(ownership ->
                           getClassesDefinitions(desiredOwner, domainOwnership, idContainer, null, drawLater)
                       )
                       .forEach(diagram::append);

        diagram.append("\n/' class ownerships '/\n");
        domainOwnership.stream()
                       .filter(ownership -> !ownership.getDependenciesOwnership()
                                                      .isEmpty())
                       .forEach(ownership -> {
                           final String classId = idContainer.getId(ownership.getTheClass())
                                                             .orElse(null);
                           if (classId == null) {
                               log.info("ignoring because of missing ID {}", ownership.getTheClass());
                               return;
                           }
                           ownership.getDependencyOwnershipsStream()
                                    .map(Entry::getValue)
                                    .forEach(dependencyOwnership -> {
                                        final String dependencyClassId
                                            = idContainer.getId(dependencyOwnership.getTheClass())
                                                         .orElse(null);
                                        if (dependencyClassId == null) {
                                            log.info("ignoring {}", dependencyOwnership.getTheClass());
                                            return;
                                        }

                                        diagram.append(classId)
                                               .append(" =[")
                                               .append(randomArrowColor())
                                               .append(",bold]=")
                                               .append(randomRepeat(1, 5, "="))
                                               .append("|> ")
                                               .append(dependencyClassId)
                                               .append('\n');
                                    });
                       });

        diagram.append("\n/' method ownerships '/\n");
        domainOwnership.forEach(ownership -> {
                // This will limit arrows for methods to one per class / owner
                final HashSet<Pair<String, String>> methodClassIdToOwnerIdPairs = new HashSet<>();
                ownership.getMethodOwners()
                         .entrySet()
                         .stream()
                         .filter(methodOwner ->
                             !methodOwner.getValue()
                                         .equals(ownership.getClassOwner())
                         )
                         .forEach(methodOwner -> {
                             final String methodClassId = idContainer.getId(ownership.getTheClass())
                                                                     .orElse(null);
                             if (methodClassId == null) {
                                 log.info("ignoring {}", ownership.getTheClass());
                                 return;
                             }
                             final String ownerId = idContainer.getId(methodOwner.getValue())
                                                               .orElse(null);
                             if (ownerId == null) {
                                 log.info("ignoring {}", methodOwner.getValue());
                                 return;
                             }
                             final Pair<String, String> pair = new Pair<>(methodClassId, ownerId);
                             if (methodClassIdToOwnerIdPairs.contains(pair)) {
                                 return;
                             } else {
                                 methodClassIdToOwnerIdPairs.add(pair);
                             }
                             diagram.append(methodClassId)
                                    .append(" -[")
                                    .append(randomArrowColor())
                                    .append(",dashed]-")
                                    .append(randomRepeat(1, 5, "-"))
                                    .append("> ")
                                    .append(ownerId)
                                    .append('\n');
                         });
            }
        );

        diagram.append("\n/' drawLater lines '/\n");
        drawLater.forEach(diagram::append);
        diagram.append("@enduml\n");
        final String resultDiagram = diagram.toString();

        log.info("generated diagram:\n{}", resultDiagram);
        return new SourceStringReader(resultDiagram);
    }

    private String getClassesDefinitions(final String desiredOwner,
                                         final Collection<ClassOwnership> domainOwnership,
                                         final IdContainer idContainer,
                                         final String owner,
                                         final List<String> drawLater) {
        final StringBuilder diagram = new StringBuilder();
        final List<String> classIds = new ArrayList<>();
        domainOwnership.stream()
                       .filter(it -> Objects.equals(owner, it.getClassOwner()))
                       .forEach(ownedClass -> {
                           if (
                               // don't draw classes that have no relationships with other classes
                               // or are not dependencies of other classes
                               desiredOwner != null
                                   && desiredOwner.equals(ownedClass.getClassOwner())
                                   && ownedClass.getMethodOwners()
                                                .isEmpty()
                                   && ownedClass.getDependencyOwnershipsStream()
                                                .map(Entry::getValue)
                                                .map(ClassOwnership::getClassOwner)
                                                .allMatch(ownedClass.getClassOwner()::equals)
                                   && domainOwnership.stream()
                                                     .flatMap(ClassOwnership::getDependencyOwnershipsStream)
                                                     .map(Entry::getValue)
                                                     .noneMatch(it -> ownedClass.getTheClass()
                                                                                .equals(it.getTheClass()))
                           ) {
                               log.info("ignoring {} because it is irrelevant", ownedClass.getTheClass());
                               return;
                           }

                           if (
                               desiredOwner == null
                                   || desiredOwner.equals(ownedClass.getClassOwner())
                                   || ownedClass.getMethodOwners()
                                                .containsValue(desiredOwner)
                           ) {
                               final String classId = idContainer.putAndGetId(ownedClass.getTheClass());
                               classIds.add(classId);
                               diagram.append("  folder ")
                                      .append(ownedClass.getTheClass()
                                                        .getSimpleName())
                                      .append(" as ")
                                      .append(classId);

                               diagram.append(" {\n")
                                      .append("    agent invisible_")
                                      .append(IdContainer.generateRandomId())
                                      .append('\n')
                                      .append("  }\n");
                           }
                       });
        classIds.forEach(classId ->
            classIds.stream()
                    .filter(anotherClassId -> !classId.equals(anotherClassId))
                    .forEach(anotherClassId -> {
                        if (RANDOM.nextBoolean()) {
                            drawLater.add(
                                classId + " -[hidden]" + randomRepeat(0, 3, "-") + "> " + anotherClassId
                                    + '\n'
                            );
                        }
                    })
        );
        return diagram.toString();
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static final class Pair<L, R> {

        private final L left;
        private final R right;
    }

    /**
     * Used to ensure each element of the diagram has a unique ID.
     */
    private static final class IdContainer {

        private final HashMap<Object, String> objectIds = new HashMap<>();

        /**
         * @return unique ID compatible with PlantUML naming convention
         */
        private static String generateRandomId() {
            return randomUUID().toString()
                               .replace("-", "_");
        }

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

    }

}
