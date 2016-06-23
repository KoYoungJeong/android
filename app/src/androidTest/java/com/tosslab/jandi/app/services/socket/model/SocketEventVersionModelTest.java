package com.tosslab.jandi.app.services.socket.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class SocketEventVersionModelTest {
    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {

    }

    @Test
    public void testValidVersion() throws Exception {
        {
            HasVersion hasVersion = new HasVersion();
            hasVersion.setVersion(1);

            boolean validVersion = SocketEventVersionModel.validVersion(hasVersion);
            assertThat(validVersion).isTrue();

            hasVersion.setVersion(2);

            validVersion = SocketEventVersionModel.validVersion(hasVersion);
            assertThat(validVersion).isFalse();
        }
        {
            NoVersion noVersion = new NoVersion();
            noVersion.setVersion(1);

            boolean validVersion = SocketEventVersionModel.validVersion(noVersion);
            assertThat(validVersion).isFalse();

        }

        {
            SubVersion subVersion = new SubVersion();
            subVersion.setVersion(1);

            boolean validVersion = SocketEventVersionModel.validVersion(subVersion);
            assertThat(validVersion).isTrue();
            subVersion.setVersion(2);
            validVersion = SocketEventVersionModel.validVersion(subVersion);
            assertThat(validVersion).isFalse();
        }

        {
            boolean valid = SocketEventVersionModel.validVersion(new HistoryVersion(1));
            assertThat(valid).isTrue();
            valid = SocketEventVersionModel.validVersion(new HistoryVersion(2));
            assertThat(valid).isFalse();
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
    static class HistoryVersion implements EventHistoryInfo {


        private int version;

        HistoryVersion(int version) {this.version = version;}

        @Override
        public long getTs() {
            return 0;
        }

        @Override
        public String getEvent() {
            return null;
        }

        @Override
        public int getVersion() {
            return version;
        }

        @Override
        public long getTeamId() {
            return 0;
        }
    }

    @Version(1)
    static class SubVersion extends NoVersion {
    }

}