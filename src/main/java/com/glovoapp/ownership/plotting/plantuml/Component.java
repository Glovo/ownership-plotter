package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Nothing.nothing;
import static com.glovoapp.ownership.plotting.plantuml.Utils.DEFAULT_INDENTATION;
import static com.glovoapp.ownership.plotting.plantuml.Utils.generateRandomId;
import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.ClassOwnership;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(exclude = "classOwnership")
@RequiredArgsConstructor(access = PRIVATE)
final class Component implements Renderable, Identifiable {

    private final static ConcurrentHashMap<Class<?>, String> CLASSES_TO_IDS = new ConcurrentHashMap<>();

    @Getter
    private final String id;
    private final Class<?> theClass;

    @Getter
    private final ClassOwnership classOwnership;

    Component(final ClassOwnership classOwnership) {
        this(CLASSES_TO_IDS.computeIfAbsent(classOwnership.getTheClass(), it -> generateRandomId()), classOwnership);
    }

    private Component(final String id, final ClassOwnership classOwnership) {
        this(id, classOwnership.getTheClass(), classOwnership);
    }

    @Override
    public final String render() {
        return "folder " + theClass.getSimpleName() + " as " + id + " {\n"
            + nothing().renderIndented(DEFAULT_INDENTATION)
            + "}\n";
    }

}
