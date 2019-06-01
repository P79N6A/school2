package com.mnevent.hz.litemolder;

import com.mnevent.hz.Utils.Log;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyand on 2019/5/21.
 */

public class ProductdisplayMolder extends LitePalSupport {
    private int id;

    private ProductlibMolder pathwayMolder ;

    private List<TrackstatusMolder> pathway = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ProductlibMolder getPathwayMolder() {
        return pathwayMolder;
    }

    public void setPathwayMolder(ProductlibMolder pathwayMolder) {
        this.pathwayMolder = pathwayMolder;
    }

    public List<TrackstatusMolder> getPathway() {
        List<TrackstatusMolder> trackstatusMolders = LitePal.findAll(TrackstatusMolder.class,true);
        //LitePal.where("id = ?",String.valueOf(0)).find(TrackstatusMolder.class);
        Log.d("zlc","trackstatusMolders.size()"+trackstatusMolders.size()+""+trackstatusMolders.get(0).getCode());
        //return LitePal.findAll(TrackstatusMolder.class,true);
        return trackstatusMolders;
    }

    public void setPathway(List<TrackstatusMolder> pathway) {
        this.pathway = pathway;
    }
}
