package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Color.black;
import static com.glovoapp.ownership.plotting.plantuml.Color.darkGrey;
import static com.glovoapp.ownership.plotting.plantuml.Color.lightGrey;
import static com.glovoapp.ownership.plotting.plantuml.Utils.DEFAULT_INDENTATION_STRING;
import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = PRIVATE)
public final class DiagramConfiguration implements Renderable {

    @Builder.Default
    private final Color ownerFontColor = black();
    @Builder.Default
    private final Color ownerBackgroundColor = lightGrey();

    @Builder.Default
    private final Color classFontColor = black();
    @Builder.Default
    private final Color classBackgroundColor = darkGrey();

    public static DiagramConfiguration defaultDiagramConfiguration() {
        return DiagramConfiguration.builder()
                                   .build();
    }

    @Override
    public final String render() {
        return "/' diagram configuration '/\n"
            + "skinparam package {\n"
            + DEFAULT_INDENTATION_STRING + "fontColor " + ownerFontColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "backgroundColor " + ownerBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "borderColor " + ownerBackgroundColor.toHexString() + '\n'
            + "}\n"
            + "skinparam storage {\n" // storages are for empty packages (owners with no classes)
            + DEFAULT_INDENTATION_STRING + "shadowing false\n"
            + DEFAULT_INDENTATION_STRING + "fontColor " + ownerBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "backgroundColor " + ownerBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "borderColor " + ownerBackgroundColor.toHexString() + '\n'
            + "}\n\n"
            // Classes are represented by folders
            + "skinparam folder {\n"
            + DEFAULT_INDENTATION_STRING + "fontColor " + classFontColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "backgroundColor " + classBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "borderColor " + classBackgroundColor.toHexString() + '\n'
            + "}\n"
            + "skinparam agent {\n" // agents are for empty folders (classes with no methods)
            + DEFAULT_INDENTATION_STRING + "shadowing false\n"
            + DEFAULT_INDENTATION_STRING + "fontColor " + classBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "backgroundColor " + classBackgroundColor.toHexString() + '\n'
            + DEFAULT_INDENTATION_STRING + "borderColor " + classBackgroundColor.toHexString() + '\n'
            + "}\n\n";
    }

}
