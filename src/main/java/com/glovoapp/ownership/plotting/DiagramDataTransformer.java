package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.ClassOwnership;
import java.util.Set;

public interface DiagramDataTransformer<ClassOwnershipDiagramData> {

    ClassOwnershipDiagramData transformToDiagramData(final Set<ClassOwnership> domainOwnership);

}
