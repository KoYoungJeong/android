package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

/**
 * Created by tee on 16. 4. 16..
 */
public class TypeUtil {

    // Flag 타입은 int 범위 상 총 32개 까지만 가능함.
    // 그럴리는 없어야겠지만 32개가 넘어간다면 좀 더 복잡한 비트 마스크 연산이 필요.
    // -> 상수와 메서드 내용물만 바뀌므로 고민할 필요는 없음.
    public static final int TYPE_EMPTY = 1;
    public static final int TYPE_VIEW_NORMAL_MESSAGE = 1 << 1;
    public static final int TYPE_VIEW_STICKER_MESSAGE = 1 << 2;
    public static final int TYPE_VIEW_IMAGE_MESSAGE = 1 << 3;
    public static final int TYPE_VIEW_FILE_MESSAGE = 1 << 4;
    public static final int TYPE_VIEW_STICKER_COMMENT_FOR_FILE = 1 << 5;
    public static final int TYPE_VIEW_MESSAGE_COMMENT_FOR_FILE = 1 << 6;
    public static final int TYPE_VIEW_DUMMY_NORMAL_MESSAGE = 1 << 7;
    public static final int TYPE_VIEW_DUMMY_STICKER = 1 << 8;
    public static final int TYPE_VIEW_EVENT_MESSAGE = 1 << 9;
    public static final int TYPE_VIEW_JANDI_BOT_MESSAGE = 1 << 10;
    public static final int TYPE_VIEW_INTEGRATION_BOT_MESSAGE = 1 << 11;
    public static final int TYPE_VIEW_POLL = 1 << 12;
    public static final int TYPE_VIEW_STICKER_COMMENT_FOR_POLL = 1 << 13;
    public static final int TYPE_VIEW_MESSAGE_COMMENT_FOR_POLL = 1 << 14;
    public static final int TYPE_VIEW_LIMIT_MESSAGE = 1 << 15;

    public static final int TYPE_OPTION_PURE = 1 << 20;
    public static final int TYPE_OPTION_HAS_ONLY_BADGE = 1 << 21;
    public static final int TYPE_OPTION_HAS_BOTTOM_MARGIN = 1 << 22;
    public static final int TYPE_OPTION_HAS_COMMENT_CONTENT_INFO = 1 << 23;
    public static final int TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL = 1 << 24;
    public static final int TYPE_OPTION_HAS_COMMENT_VIEW_ALL = 1 << 25;
    public static final int TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE = 1 << 26;
    public static final int TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER = 1 << 27;
    public static final int TYPE_OPTION_HAS_FLAT_TOP = 1 << 28;
    public static final int TYPE_OPTION_HAS_TOP_MARGIN = 1 << 29;

    /* 인자 viewType에 flagType의 요소가 포함되어 있는지 검사
     * ex) isElementType(viewType, TYPE_VIEW_EVENT_MESSAGE) -> event 타입의 메세지
     */
    public static boolean hasTypeElement(int viewType, int flagType) {
        if ((viewType & flagType) > 0) {
            return true;
        }
        return false;
    }

    /* viewType에 flagType을 추가 */
    public static int addType(int viewType, int flagType) {
        return viewType | flagType;
    }
}
