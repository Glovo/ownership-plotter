package com.glovoapp.diagrams;

public interface Identifiable<Id extends Identifier<Id>> {

    Id getId();

}
