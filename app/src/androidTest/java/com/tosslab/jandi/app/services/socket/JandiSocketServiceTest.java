package com.tosslab.jandi.app.services.socket;

import org.junit.Test;

/**
 * Created by tee on 15. 12. 23..
 */
public class JandiSocketServiceTest {

    @Test
    public void testUpdateEventHistory() throws Exception {
        JandiSocketService jandiSocketService = new JandiSocketService();
        jandiSocketService.updateEventHistory();
    }
}