package com.glovoapp.ownership.plotting.plantuml;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.DiagramDataTransformer;
import java.util.Collection;
import java.util.Map.Entry;
import lombok.extern.java.Log;
import net.sourceforge.plantuml.SourceStringReader;

@Log
public final class PlantUMLDiagramDataTransformer implements DiagramDataTransformer<SourceStringReader> {

    @Override
    public final SourceStringReader transformToDiagramData(final Collection<ClassOwnership> domainOwnership) {
        final StringBuilder diagram = new StringBuilder().append("@startuml")
                                                         .append('\n');

        // TODO: draw me like one of your french girls
        domainOwnership.forEach(ownership -> {
            diagram.append("class ")
                   .append(ownership.getTheClass()
                                    .getCanonicalName());

            if (!ownership.getMethodOwners()
                          .isEmpty()) {
                diagram.append(" {\n");
                ownership.getMethodOwners()
                         .forEach((method, owner) ->
                             diagram.append("  +")
                                    .append(method.getName())
                                    .append("() owned by ")
                                    .append(owner)
                                    .append('\n')
                         );
                diagram.append("}");
            }

            diagram.append('\n')
                   .append("note top of ")
                   .append(ownership.getTheClass()
                                    .getCanonicalName())
                   .append(" : ")
                   .append(ownership.getClassOwner())
                   .append('\n');
        });

        domainOwnership.forEach(ownership ->
            ownership.getDependencyOwnershipsStream()
                     .map(Entry::getValue)
                     .forEach(dependencyOwnership ->
                         diagram.append(ownership.getTheClass()
                                                 .getCanonicalName())
                                .append(" --* ")
                                .append(dependencyOwnership.getTheClass()
                                                           .getCanonicalName())
                                .append('\n')
                     )
        );

        diagram.append("@enduml\n");
        final String resultDiagram = diagram.toString();

        log.info("generated diagram:\n" + resultDiagram);
        return new SourceStringReader(resultDiagram);
    }

}
