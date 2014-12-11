package com.tosslab.jandi.app.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PasswordCheckerTest {
    @Test
    public void testHasCapitalLetterShouldReturnTrue() {
        String passwordContaingCapitalLetter = "aBcd";
        assertThat(
                PasswordChecker.hasCapitalLetter(passwordContaingCapitalLetter),
                is(true)
        );
    }

    @Test
    public void testHasCapitalLetterShouldReturnFalse() {
        String passwordContaingCapitalLetter = "abcd123$%^";
        assertThat(
                PasswordChecker.hasCapitalLetter(passwordContaingCapitalLetter),
                is(false)
        );
    }

    @Test
    public void testHasDigitShouldReturnTrue() {
        String passwordContaingCapitalLetter = "aBcd1";
        assertThat(
                PasswordChecker.hasDigit(passwordContaingCapitalLetter),
                is(true)
        );
    }

    @Test
    public void testHasDigitShouldReturnFalse() {
        String passwordContaingCapitalLetter = "abcd$%^";
        assertThat(
                PasswordChecker.hasDigit(passwordContaingCapitalLetter),
                is(false)
        );
    }

    @Test
    public void testHasSpecialCharacterShouldReturnTrue() {
        String passwordContaingCapitalLetter = "abcd123|;";
        assertThat(
                PasswordChecker.hasSpecialCharacter(passwordContaingCapitalLetter),
                is(true)
        );
    }

    @Test
    public void testHasSpecialCharacterShouldReturnTrue2() {
        String passwordContaingCapitalLetter = "abcd[123";
        assertThat(
                PasswordChecker.hasSpecialCharacter(passwordContaingCapitalLetter),
                is(true)
        );
    }

    @Test
    public void testHasSpecialCharacterShouldReturnTrue3() {
        String passwordContaingCapitalLetter = "abcd12]3";
        assertThat(
                PasswordChecker.hasSpecialCharacter(passwordContaingCapitalLetter),
                is(true)
        );
    }

    @Test
    public void testHasSpecialCharacterShouldReturnFalse() {
        String passwordContaingCapitalLetter = "1234";
        assertThat(
                PasswordChecker.hasSpecialCharacter(passwordContaingCapitalLetter),
                is(false)
        );
    }

    @Test
    public void testTooShortPasswordShouldBeCheckedAsTooShortResult() {
        String tooShortPassword = "aB#$;1";
        assertThat("Too short password has to be returned with TOO_SHORT",
                PasswordChecker.checkStrength(tooShortPassword),
                is(PasswordChecker.TOO_SHORT)
        );
    }

    @Test
    public void testWeakPasswordShouldBeCheckedAsWeakResult() {
        String weakPassword = "123456789";
        assertThat("Weak password has to be returned with WEAK",
                PasswordChecker.checkStrength(weakPassword),
                is(PasswordChecker.WEAK)
        );
    }

    @Test
    public void testWeakPasswordShouldBeCheckedAsWeakResult2() {
        String weakPassword = "!@#$&*(}\":]";
        assertThat("Weak password has to be returned with WEAK",
                PasswordChecker.checkStrength(weakPassword),
                is(PasswordChecker.WEAK)
        );
    }

    @Test
    public void testAveragePasswordShouldBeCheckedAsResultAverage() {
        String safePassword = "abcdefGHij";
        assertThat("Average password has to be returned with AVERAGE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.AVERAGE)
        );
    }

    @Test
    public void testAveragePasswordShouldBeCheckedAsResultAverage2() {
        String safePassword = "abcdef&*()_";
        assertThat("Average password has to be returned with AVERAGE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.AVERAGE)
        );
    }

    @Test
    public void testAveragePasswordShouldBeCheckedAsResultAverage3() {
        String safePassword = "123456&*()_";
        assertThat("Average password has to be returned with AVERAGE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.AVERAGE)
        );
    }

    @Test
    public void testSafePasswordShouldBeCheckedAsResultSafe() {
        String safePassword = "abcdefGHij2";
        assertThat("Safe password has to be returned with SAFE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.SAFE)
        );
    }

    @Test
    public void testSafePasswordShouldBeCheckedAsResultSafe2() {
        String safePassword = "1aX2bY3cZ";
        assertThat("Safe password has to be returned with SAFE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.SAFE)
        );
    }

    @Test
    public void testSafePasswordShouldBeCheckedAsResultSafe3() {
        String safePassword = "{?<123ABC";
        assertThat("Safe password has to be returned with SAFE",
                PasswordChecker.checkStrength(safePassword),
                is(PasswordChecker.SAFE)
        );
    }

    @Test
    public void testStrongPasswordShouldBeCheckedAsResultStrong() {
        String strongPassword = "abCD12|\\";
        assertThat("Strong password has to be returned with STRONG",
                PasswordChecker.checkStrength(strongPassword),
                is(PasswordChecker.STRONG)
        );
    }

    @Test
    public void testStrongPasswordShouldBeCheckedAsResultStrong2() {
        String strongPassword = "Rkd789&*(";
        assertThat("Strong password has to be returned with STRONG",
                PasswordChecker.checkStrength(strongPassword),
                is(PasswordChecker.STRONG)
        );
    }

    @Test
    public void testStrongPasswordShouldBeCheckedAsResultStrong3() {
        String strongPassword = ",.[]Zz12";
        assertThat("Strong password has to be returned with STRONG",
                PasswordChecker.checkStrength(strongPassword),
                is(PasswordChecker.STRONG)
        );
    }
}
