package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Utils.generateRandomId;

final class Nothing implements Renderable {

    static final Nothing INSTANCE = new Nothing();

    static Nothing nothing() {
        return INSTANCE;
    }

    @Override
    public final String render() {
        return "agent invisible_" + generateRandomId() + '\n';
    }

}
