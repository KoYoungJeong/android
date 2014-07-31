package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * MainLeftFragment -> MainActivity : Navigation Panel 에서 선택한 CDP 의 메시지 list 획득 목적
 */
public class SelectCdpItemEvent {
//    public CdpItem cdpItem;
    public int cdpType;
    public int cdpId;
    public boolean doGetMessageAfterThisWork;

//    public SelectCdpItemEvent(CdpItem cdpItem) {
//        this.cdpItem = cdpItem;
//    }
    public SelectCdpItemEvent(int cdpType, int cdpId) {
        this(cdpType, cdpId, true);
    }

    public SelectCdpItemEvent(int cdpType, int cdpId, boolean doGetMessageAfterThisWork) {
        this.cdpType = cdpType;
        this.cdpId = cdpId;
        this.doGetMessageAfterThisWork = doGetMessageAfterThisWork;
    }
}
