package com.glovoapp.ownership.plotting.plantuml;


import static lombok.AccessLevel.PACKAGE;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
final class Relationship implements Renderable {

    private final Identifiable left;
    private final Identifiable right;
    private final Arrow arrow;

    @Override
    public final String render() {
        return left.getId() + ' ' + arrow.render() + ' ' + right.getId() + '\n';
    }

}
