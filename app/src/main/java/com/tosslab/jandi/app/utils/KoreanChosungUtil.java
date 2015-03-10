package com.tosslab.jandi.app.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class KoreanChosungUtil {

    private static final char HANGUL_BEGIN_UNICODE = 44032; // 가
    private static final char HANGUL_END_UNICODE = 55203; // 힣

    /**
     * @param word : target word
     * @return : target word 의 초성 index
     */
    private static int getChoSungOfWord(char word) {
        int a, c;

        c = word - 0xAC00;
        a = c / (21 * 28); // 초성
        return a;
    }

    /**
     * @param word : target word
     * @return : target word가 초성인지 판단 /true : 초성 o false : 초성x
     */
    private static int isChoSung(char word) {
        char[] samp = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ',
                'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
        for (int i = 0; i < samp.length; i++) {
            if (word == samp[i])
                return i;
        }
        return -1;
    }

    /**
     * @param unicode
     * @return
     * @설명 : 한글인지 체크
     */
    public static boolean isHangul(char unicode) {
        return HANGUL_BEGIN_UNICODE <= unicode && unicode <= HANGUL_END_UNICODE;
    }

    /**
     * @param unicode
     * @return
     * @설명 : 영어인지 체크
     */
    public static boolean isEnglish(char c) {
        return (0x61 <= c && c <= 0x7A) || (0x41 <= c && c <= 0x5A);
    }

    /**
     * @param letter
     * @return
     * @ㅎ설명 : 소문자로 바꿔준다.
     */
    public static char ChangeToSmallLetter(char letter) {
        return Character.toLowerCase(letter);
    }

    /**
     * @param word       : 검색 대상 단어
     * @param searchWord : 검색어
     * @return : 검색어에 일치하는 검색 대상인지 여부 / true : 일치 false : 불일치
     * @설명 : 검색어와 검색 대상을 비교하여 검색어(초성검색 포함)에 검색 대상인지 여부 판단
     */
    public static boolean isMatchSearchWord(String word, String searchWord) {

        int searchWordSize = searchWord.length();    // 검색어의 길이
        int wordSize = word.length();                // 검색대상 어의 길이
        int wordMatchIndex = 0;                    // 검색대상어의 index
        List<Integer> wordMatchIndexArray = new ArrayList<Integer>();

        // 검색 대상어의 길이만큼 돌면서 검색어의 첫글자와 일치하는 인덱스의 값들을 wordMatchIndexArray 넣는다.
        for (wordMatchIndex = 0; wordMatchIndex < wordSize; wordMatchIndex++) {
            // 검색어의 첫글자가 초성인지를 체크
            int tempIsChoSung = isChoSung(searchWord.charAt(0));
            if (tempIsChoSung >= 0) {
                // 검색대상어의 초성과 비교한다.
                if (tempIsChoSung == getChoSungOfWord(word.charAt(wordMatchIndex))) {
                    // 일치하면 Match Index에 넣는다.
                    wordMatchIndexArray.add(wordMatchIndex);
                }
            } else {
                // 검색어가 초성이 아니면 영문인지 여부 체크
                if (isEnglish(searchWord.charAt(0))) {
                    // 영어이면 소문자로 변환후 검색
                    if (ChangeToSmallLetter(searchWord.charAt(0)) == ChangeToSmallLetter(word
                            .charAt(wordMatchIndex))) {
                        // 일치하면 Match Index에 넣는다.
                        wordMatchIndexArray.add(wordMatchIndex);
                    }
                } else {
                    if (searchWord.charAt(0) == word.charAt(wordMatchIndex)) {
                        // 일치하면 Match Index에 넣는다.
                        wordMatchIndexArray.add(wordMatchIndex);
                    }
                }
            }
        }

        // Match Index로 검색한다.
        int wordMatchIndexArraySize = wordMatchIndexArray.size();
        // 일치하는 첫글자의 index들을 시작으로 해서 뒤에 글자들이 일치하는지 체크
        for (int k = 0; k < wordMatchIndexArraySize; k++) {
            // Match Index를 가져온다.
            int currentIndex = wordMatchIndexArray.get(k);
            // Match Index이후에 남은 단어가 검색어보다 적으면 패스한다.
            if (searchWordSize > wordSize - currentIndex) {
                continue;
            }
            // 검색어와 모두 일치하는지 체크
            int matchWordNum = 0;
            for (int t = 0; t < searchWordSize; t++) {
                // 초성인지 여부
                int tempIsChoSung = isChoSung(searchWord.charAt(t));
                if (tempIsChoSung >= 0) {
                    // 초성의 경우
                    if (tempIsChoSung == getChoSungOfWord(word.charAt(currentIndex))) {
                        matchWordNum++;
                    } else {
                        break;
                    }
                } else {
                    // 검색어가 초성이 아니면 영문인지 여부 체크
                    if (isEnglish(searchWord.charAt(t))) {
                        // 영어이면 소문자로 변환후 검색
                        if (ChangeToSmallLetter(searchWord.charAt(t)) == ChangeToSmallLetter(word
                                .charAt(currentIndex))) {
                            // 일치하면 Match Index에 넣는다.
                            matchWordNum++;
                        }
                    } else {
                        if (searchWord.charAt(t) == word.charAt(currentIndex)) {
                            // 일치하면 Match Index에 넣는다.
                            matchWordNum++;
                        }
                    }
                }
                currentIndex++;
            }
            // 일치한 워드수와 검색어 워드수가 같을경우 True 리턴
            if (matchWordNum == searchWordSize)
                return true;
        }
        return false;
    }

    public static String getInitSound(String keyword) {

        int keywordLength = keyword.length();
        Character[] chars = new Character[keywordLength];

        for (int idx = 0; idx < keywordLength; idx++) {
            chars[idx] = keyword.charAt(idx);
        }

        Iterator<String> characters = Observable.from(chars)
                .map(character -> {
                    char charValue = character.charValue();
                    if (isHangul(charValue)) {
                        return String.valueOf(getChoSungOfWord(charValue));
                    } else {
                        return String.valueOf(charValue);
                    }
                }).toBlocking()
                .getIterator();

        StringBuffer buffer = new StringBuffer();

        while (characters.hasNext()) {
            buffer.append(characters.next());
        }

        return buffer.toString();
    }

    public static String replaceChosung(String keyword, String replacement) {
        int keywordLength = keyword.length();
        Character[] chars = new Character[keywordLength];

        for (int idx = 0; idx < keywordLength; idx++) {
            chars[idx] = keyword.charAt(idx);
        }

        Iterator<String> characters = Observable.from(chars)
                .map(character -> {
                    char charValue = character.charValue();
                    if (isChoSung(charValue) >= 0) {
                        return replacement;
                    } else {
                        return String.valueOf(charValue);
                    }
                }).toBlocking()
                .getIterator();

        StringBuffer buffer = new StringBuffer();

        while (characters.hasNext()) {
            buffer.append(characters.next());
        }

        return buffer.toString();
    }

    public static boolean hasHangul(String keyword) {

        int keywordLength = keyword.length();
        Character[] chars = new Character[keywordLength];

        for (int idx = 0; idx < keywordLength; idx++) {
            chars[idx] = keyword.charAt(idx);
        }

        Boolean hasHangul = Observable.from(chars)
                .filter(character -> isHangul(character.charValue()))
                .map(character -> true)
                .toBlocking()
                .firstOrDefault(false);

        return hasHangul.booleanValue();
    }
}
