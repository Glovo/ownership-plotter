package com.glovoapp.ownership.example2.downstreamcontext;

import com.glovoapp.ownership.example2.OwnerWithContextAnnotation;
import com.glovoapp.ownership.example2.unknowncontext.Unknowclass;
import com.glovoapp.ownership.example2.upstreamcontext.UpstreamContextService;


public class DownstreamContextService {

    private UpstreamContextService dep = new UpstreamContextService();
    private Unknowclass haha = new Unknowclass();

    public void execute() {
        dep.execute();
    }


}
