package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Arrow.randomHiddenArrow;
import static com.glovoapp.ownership.plotting.plantuml.Nothing.nothing;
import static com.glovoapp.ownership.plotting.plantuml.Utils.RANDOM;
import static com.glovoapp.ownership.plotting.plantuml.Utils.generateRandomId;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class Owner implements Renderable, Identifiable {

    private final static ConcurrentHashMap<String, String> OWNERS_TO_IDS = new ConcurrentHashMap<>();

    @Getter
    private final String id;

    @Getter
    private final String name;
    private final Set<Component> components;

    Owner(final String name, final Set<Component> components) {
        this(OWNERS_TO_IDS.computeIfAbsent(name, it -> generateRandomId()), name, components);
    }

    @Override
    public final String render() {
        return "package " + name + " as " + id + " {\n"
            + (
            components.isEmpty()
                ? nothing().renderWithDefaultIndent()
                : (
                    Stream.concat(
                        components.stream(),

                        // Randomly distribute components inside owner
                        components.stream()
                                  .flatMap(component -> components.stream()
                                                                  .filter(ignore -> RANDOM.nextBoolean())
                                                                  .map(anotherComponent ->
                                                                      new Relationship(
                                                                          component,
                                                                          anotherComponent,
                                                                          randomHiddenArrow()
                                                                      )
                                                                  ))
                    )
                          .map(Renderable::renderWithDefaultIndent)
                          .collect(joining())
                )
        ) + "}\n";
    }

}
