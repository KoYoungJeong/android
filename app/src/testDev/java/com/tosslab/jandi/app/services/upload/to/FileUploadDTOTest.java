package com.tosslab.jandi.app.services.upload.to;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class FileUploadDTOTest {

    @Test
    public void testJsonSerial() throws Exception {

        FileUploadDTO fileUploadDTO = new FileUploadDTO("path", "name", 1, "comment");

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(fileUploadDTO);

        FileUploadDTO recoveredValue = objectMapper.readValue(value, FileUploadDTO.class);

        assertThat(recoveredValue.getComment(), is(fileUploadDTO.getComment()));
        assertThat(recoveredValue.getEntity(), is(fileUploadDTO.getEntity()));
        assertThat(recoveredValue.getFileName(), is(fileUploadDTO.getFileName()));
        assertThat(recoveredValue.getFilePath(), is(fileUploadDTO.getFilePath()));

    }
}