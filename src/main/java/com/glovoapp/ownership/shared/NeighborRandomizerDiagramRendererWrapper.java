package com.glovoapp.ownership.shared;

import com.glovoapp.diagrams.*;
import com.glovoapp.diagrams.Relationship.SimpleRelationship;
import com.glovoapp.ownership.plotting.ClassRelationship;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import static com.glovoapp.ownership.shared.Sets.union;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

@RequiredArgsConstructor
public final class NeighborRandomizerDiagramRendererWrapper<Id extends Identifier<Id>, RelationshipType>
        implements DiagramRenderer<Id, RelationshipType> {

    private final static Random RANDOM = new Random();

    private final int randomNeighborGenerationPercentage;
    private final DiagramRenderer<Id, RelationshipType> delegate;
    private final Supplier<RelationshipType> neighborRelationshipSupplier;

    public static <Id extends Identifier<Id>> NeighborRandomizerDiagramRendererWrapper<Id, ClassRelationship> wrapClassDiagram(
            final DiagramRenderer<Id, ClassRelationship> delegate
    ) {
        return wrapClassDiagram(25, delegate);
    }

    public static <Id extends Identifier<Id>> NeighborRandomizerDiagramRendererWrapper<Id, ClassRelationship> wrapClassDiagram(
            final int randomNeighborGenerationPercentage,
            final DiagramRenderer<Id, ClassRelationship> delegate
    ) {
        return new NeighborRandomizerDiagramRendererWrapper<>(
                randomNeighborGenerationPercentage,
                delegate,
                () -> ClassRelationship.VISUAL
        );
    }

    @Override
    public final InputStream renderDiagram(final Diagram<Id, RelationshipType> diagram) {
        return delegate.renderDiagram(
                new Diagram.SimpleDiagram<>(
                        diagram.getTopLevelComponents(),
                        union(
                                diagram.getRelationships(),
                                createRandomNeighborRelationships(diagram.getTopLevelComponents())
                        )
                )
        );
    }

    private Set<Relationship<Id, RelationshipType>> createRandomNeighborRelationships(
            final Set<Component<Id>> components
    ) {
        return components.stream()
                .flatMap(component ->
                        concat(
                                components.stream()
                                        .filter(otherComponent -> component != otherComponent)
                                        .filter(otherComponent -> shouldGenerateRelationship())
                                        .map(otherComponent ->
                                                new SimpleRelationship<>(
                                                        neighborRelationshipSupplier.get(),
                                                        component,
                                                        otherComponent
                                                )
                                        ),
                                createRandomNeighborRelationships(component.getNestedComponents()).stream()
                        )
                )
                .collect(toSet());
    }

    private boolean shouldGenerateRelationship() {
        return RANDOM.nextInt(100) <= randomNeighborGenerationPercentage;
    }

}
