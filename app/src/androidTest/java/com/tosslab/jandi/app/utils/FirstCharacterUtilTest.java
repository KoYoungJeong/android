package com.tosslab.jandi.app.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class FirstCharacterUtilTest {
    @Test
    public void firstCharacter() throws Exception {
        {
            String s = FirstCharacterUtil.firstCharacter("가나다");
            assertThat(s).isEqualTo("ㄱ");

            s = FirstCharacterUtil.firstCharacter("ㅎ어");
            assertThat(s).isEqualTo("ㅎ");
        }

        {
            String s = FirstCharacterUtil.firstCharacter("いんりょうすい");
            assertThat(s).isEqualTo("い");

            s = FirstCharacterUtil.firstCharacter("アリガトウゴザイマス");
            assertThat(s).isEqualTo("あ");

            s = FirstCharacterUtil.firstCharacter("ヸリガトウゴザイマス");
            assertThat(s).isEqualTo("\u3098");
        }

        {
            String s = FirstCharacterUtil.firstCharacter("qasd");
            assertThat(s).isEqualTo("Q");

            s = FirstCharacterUtil.firstCharacter("0123");
            assertThat(s).isEqualTo("#");

            s = FirstCharacterUtil.firstCharacter("\uD83D\uDE00");
            assertThat(s).isEqualTo("#");
        }

        {
            String s = FirstCharacterUtil.firstCharacter("摩");
            assertThat(s).isEqualTo("摩");

        }
    }

}