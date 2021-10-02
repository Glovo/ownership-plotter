package com.glovoapp.ownership.example2.upstreamcontext;

import com.glovoapp.ownership.example2.OwnerWithContextAnnotation;
import com.glovoapp.ownership.example2.unknowncontext.Unknowclass;

@OwnerWithContextAnnotation(boundedContext = "upstream" )
public class UpstreamContextService {


    public UpstreamValueObject execute() {
        return new UpstreamValueObject();
    }
}
