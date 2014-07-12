package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.CdpItem;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * MainCdpFragment -> MainActivity : Navigation Panel 에서 선택한 CDP 의 메시지 list 획득 목적
 */
public class SelectCdpItemEvent {
    public CdpItem cdpItem;

    public SelectCdpItemEvent(CdpItem cdpItem) {
        this.cdpItem = cdpItem;
    }
}
