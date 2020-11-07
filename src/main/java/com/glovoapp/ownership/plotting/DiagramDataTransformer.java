package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.ClassOwnership;
import java.util.Collection;

public interface DiagramDataTransformer<ClassOwnershipDiagramData> {

    ClassOwnershipDiagramData transformToDiagramData(final Collection<ClassOwnership> domainOwnership);

}
