package com.glovoapp.ownership.example2.downstreamcontext;

import com.glovoapp.ownership.example2.unknowncontext.UnknownClass;
import com.glovoapp.ownership.example2.upstreamcontext.UpstreamContextService;


public class DownstreamContextService {

    private UpstreamContextService dep = new UpstreamContextService();
    private UnknownClass unknown = new UnknownClass();

    public void execute() {
        dep.execute();
    }


}
