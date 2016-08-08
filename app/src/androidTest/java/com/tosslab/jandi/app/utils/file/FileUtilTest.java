package com.tosslab.jandi.app.utils.file;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class FileUtilTest {

    @Test
    public void testFileSizeCalculation() throws Exception {
        String fileSizeText = FileUtil.formatFileSize(1023);
        assertThat(fileSizeText).isEqualTo("1023 Bytes");

        fileSizeText = FileUtil.formatFileSize(1025);
        assertThat(fileSizeText).isEqualTo("1 KB");

        fileSizeText = FileUtil.formatFileSize(1023 * 1024);
        assertThat(fileSizeText).isEqualTo("1023 KB");

        fileSizeText = FileUtil.formatFileSize(1244 * 1024);
        assertThat(fileSizeText).isEqualTo("1.2 MB");

        fileSizeText = FileUtil.formatFileSize(1023 * 1024 * 1024);
        assertThat(fileSizeText).isEqualTo("1023.0 MB");

        fileSizeText = FileUtil.formatFileSize(1025 * 1024 * 1024);
        assertThat(fileSizeText).isEqualTo("1.0 GB");

        fileSizeText = FileUtil.formatFileSize(1244 * 1024 * 1024);
        assertThat(fileSizeText).isEqualTo("1.2 GB");

        fileSizeText = FileUtil.formatFileSize(10250L * 1024L * 1024L * 1024L);
        assertThat(fileSizeText).isEqualTo("10250.0 GB");
    }
}