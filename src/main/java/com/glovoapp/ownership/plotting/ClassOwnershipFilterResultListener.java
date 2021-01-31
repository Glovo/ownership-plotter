package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.shared.TriConsumer;

public interface ClassOwnershipFilterResultListener
    extends TriConsumer<ClassOwnershipFilter, OwnershipContext, Boolean> {

    void accept(final ClassOwnershipFilter thisFilter,
                final OwnershipContext ownershipContext,
                final Boolean result);

}
