package com.tosslab.jandi.app.ui.passcode.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 2. 24..
 */
@RunWith(AndroidJUnit4.class)
public class PassCodeModelTest {

    PassCodeModel model;

    @Before
    public void setUp() throws Exception {
        model = new PassCodeModel();
    }

    @Test
    public void testPutPassCode() throws Exception {
        // When
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        // Then

        assertThat(model.getPassCodeLength(), is(4));
    }

    @Test
    public void testPopPassCode() throws Exception {
        // Given
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        // When
        model.popPassCode();
        // Then
        assertThat(model.getPassCodeLength(), is(3));
    }

    @Test
    public void testGetPassCodeLength() throws Exception {
        // Given
        model.putPassCode(0);
        // When
        model.popPassCode();
        // Then
        assertThat(model.getPassCodeLength(), is(0));
    }

    @Test
    public void testValidatePassCode() throws Exception {
        // Given
        String savedPassCode = "0123";
        // When
        model.putPassCode(0);
        model.putPassCode(1);
        model.putPassCode(2);
        model.putPassCode(3);
        // Then
        assertTrue(model.validatePassCode(savedPassCode));
    }

    @Test
    public void testClearPassCode() throws Exception {
        // Given
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        model.putPassCode(0);
        // When
        model.clearPassCode();
        // Then
        assertThat(model.getPassCodeLength(), is(0));
    }

    @Test
    public void testGetPassCode() throws Exception {
        // When
        model.putPassCode(1);
        model.putPassCode(2);
        model.putPassCode(3);
        model.putPassCode(4);
        // Then
        assertEquals(model.getPassCode(), "1234");
    }

    @Test
    public void testSavePassCode() throws Exception {
        // Given
        model.putPassCode(1);
        model.putPassCode(2);
        model.putPassCode(3);
        model.putPassCode(4);
        // When
        model.savePassCode();
        // Then
        String savedPassCode = JandiPreference.getPassCode(JandiApplication.getContext());
        assertEquals("1234", savedPassCode);
    }

    @Test
    public void testSetAndGetPreviousPassCode() throws Exception {
        // When
        model.setPreviousPassCode("1234");
        // Then
        assertEquals("1234", model.getPreviousPassCode());
    }

}