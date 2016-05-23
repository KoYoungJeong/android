package com.tosslab.jandi.app.services.socket;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class JandiSocketServiceModelTest {

    JandiSocketServiceModel jandiSocketServiceModel;

    @Before
    public void setUp() throws Exception {
        jandiSocketServiceModel = new JandiSocketServiceModel(JandiApplication.getContext(),
                () -> new AccountApi(RetrofitBuilder.getInstance()),
                () -> new MessageApi(RetrofitBuilder.getInstance()),
                () -> new LoginApi(RetrofitBuilder.getInstance()),
                () -> new EventsApi(RetrofitBuilder.getInstance()));
    }


    @Test
    public void testValidVersion() throws Exception {
        {
            HasVersion hasVersion = new HasVersion();
            hasVersion.setVersion(1);

            boolean validVersion = jandiSocketServiceModel.validVersion(hasVersion);
            assertThat(validVersion, is(true));

            hasVersion.setVersion(2);

            validVersion = jandiSocketServiceModel.validVersion(hasVersion);
            assertThat(validVersion, is(false));
        }
        {
            NoVersion noVersion = new NoVersion();
            noVersion.setVersion(1);

            boolean validVersion = jandiSocketServiceModel.validVersion(noVersion);
            assertThat(validVersion, is(false));

        }

        {
            SubVersion subVersion = new SubVersion();
            subVersion.setVersion(1);

            boolean validVersion = jandiSocketServiceModel.validVersion(subVersion);
            assertThat(validVersion, is(true));
            subVersion.setVersion(2);
            validVersion = jandiSocketServiceModel.validVersion(subVersion);
            assertThat(validVersion, is(false));
        }
    }

    @Version(1)
    static class HasVersion {
        private int version;

        public void setVersion(int version) {
            this.version = version;
        }
    }

    static class NoVersion {
        private int version;

        public void setVersion(int version) {
            this.version = version;
        }
    }

    @Version(1)
    static class SubVersion extends NoVersion {
    }
}