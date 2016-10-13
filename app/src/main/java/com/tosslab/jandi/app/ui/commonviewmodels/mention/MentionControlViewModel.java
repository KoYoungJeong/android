package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.MentionMemberListAdapter;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel_;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

public class MentionControlViewModel {
    public static final Pattern MENTION_PATTERN = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)(?:\\u2063)");
    public static final Pattern MENTION_PATTERN_FOR_SEARCH = Pattern.compile("(?:(?:^|\\s)([@\\uff20]((?:[^@\\uff20]){0,30})))$");
    public static final Pattern MENTION_PATTERN_FOR_GOOGLE_KEYBOARD = Pattern.compile("@([^@]+)(?:\\u2063)(\\d+)$");

    public static final String MENTION_TYPE_MESSAGE = "mention_type_message";
    public static final String MENTION_TYPE_FILE_COMMENT = "mention_type_file_comment";
    protected String currentSearchKeywordString;
    private SearchMemberModel searchMemberModel;

    //message or file view type
    private String mentionType = MENTION_TYPE_MESSAGE;

    //for textControl
    private int beforeTextCnt = 0;

    private int afterTextCnt = 0;
    private String beforeText = "";
    private String afterText = "";
    private String removedText = "";
    private ClipboardManager clipBoard;
    private ClipboardListener clipboardListener;
    private TextWatcher textWatcher;
    private AutoCompleteTextView etMessage;
    private MentionMemberListAdapter mentionMemberListAdapter;
    private OnMentionShowingListener onMentionShowingListener;

    private MentionControlViewModel(Activity activity,
                                    EditText editText,
                                    long teamId,
                                    List<Long> roomIds,
                                    String mentionType, Runnable callback) {

        this.clipBoard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        this.mentionType = mentionType;

        init(activity, editText, teamId, roomIds, callback);

    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      List<Long> roomIds,
                                                      String mentionType) {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        return new MentionControlViewModel(activity,
                editText,
                teamId,
                roomIds,
                mentionType, null);
    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      List<Long> roomIds,
                                                      String mentionType, Runnable callback) {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        return new MentionControlViewModel(activity,
                editText,
                teamId,
                roomIds,
                mentionType, callback);
    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      long teamId,
                                                      List<Long> roomIds,
                                                      String mentionType) {
        return new MentionControlViewModel(activity,
                editText,
                teamId,
                roomIds,
                mentionType, null);
    }

    // 현재까지의 editText에서 멘션 가공된 message와 mention object 리스트를 얻어오는 메서드
    public static ResultMentionsVO getMentionInfoObject(String message,
                                                        Map<Long, SearchedItemVO> selectableMembers) {

        if (TextUtils.isEmpty(message)) {
            return new ResultMentionsVO("", new ArrayList<>());
        }

        StringBuilder builder = new StringBuilder(message);
        String findId;
//        Pattern p = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)(?:\\u2063)");
        Matcher matcher = MENTION_PATTERN.matcher(message);

        List<SearchedItemVO> orderedSearchedMember = new ArrayList<>();

        List<Pair<Integer, Integer>> replaceIndex = new ArrayList<>();

        while (matcher.find()) {
            findId = matcher.group(2);
            try {
                long id = Long.parseLong(findId);
                if (selectableMembers.containsKey(id)) {
                    orderedSearchedMember.add(selectableMembers.get(id));
                } else {
                    replaceIndex.add(new Pair<>(matcher.start(2) - 1, matcher.end(2) + 1));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (int idx = replaceIndex.size() - 1; idx >= 0; idx--) {
            Pair<Integer, Integer> replace = replaceIndex.get(idx);
            if (replace != null && replace.first >= 0 && replace.second >= 0) {
                builder.replace(replace.first, replace.second, "");
            }
        }

        List<MentionObject> mentions = new ArrayList<>();

        StringBuilder memberInfoStringSB;

        for (SearchedItemVO searchedItemVO : orderedSearchedMember) {

            memberInfoStringSB = new StringBuilder();

            if (searchedItemVO == null) {
                continue;
            }

            String name = searchedItemVO.getName();
            String id = String.valueOf(searchedItemVO.getId());
            String type = searchedItemVO.getType();

            memberInfoStringSB
                    .append(name)
                    .append("\u2063")
                    .append(id)
                    .append("\u2063");

            int startIndexOfMemberString = builder.indexOf(memberInfoStringSB.toString());

            if (startIndexOfMemberString >= 0) {

                builder.replace(startIndexOfMemberString,
                        startIndexOfMemberString + memberInfoStringSB.length(), name);
                int offset = startIndexOfMemberString - 1;
                int length = name.length() + 1;
                MentionObject mentionInfo = new MentionObject(searchedItemVO.getId(), type, offset, length);
                mentions.add(mentionInfo);
            }

        }

        return new ResultMentionsVO(builder.toString(), mentions);

    }

    private void init(Activity activity, EditText editText, long teamId, List<Long> roomIds, Runnable callback) {

        this.etMessage = (AutoCompleteTextView) editText;

        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        refreshSelectableMembers(teamId, roomIds);

        List<SearchedItemVO> users = searchMemberModel.getUserSearchByName("");
        mentionMemberListAdapter = new MentionMemberListAdapter(activity, users);

        if (callback != null) {
            Completable.fromAction(() -> {
                etMessage.setAdapter(mentionMemberListAdapter);
                etMessage.setDropDownBackgroundResource(R.drawable.mention_popup);
                etMessage.setThreshold(1);
                addTextWatcher(editText);
            }).subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(callback::run);
        } else {
            etMessage.setAdapter(mentionMemberListAdapter);
            etMessage.setDropDownBackgroundResource(R.drawable.mention_popup);
            etMessage.setThreshold(1);
            addTextWatcher(editText);
        }

    }

    public void setOnMentionShowingListener(OnMentionShowingListener onMentionShowingListener) {
        this.onMentionShowingListener = onMentionShowingListener;
    }

    public void refreshMembers(List<Long> roomIds) {
        refreshMembers(TeamInfoLoader.getInstance().getTeamId(), roomIds);
    }

    public void refreshMembers(long teamId, List<Long> roomIds) {
        refreshSelectableMembers(teamId, roomIds);

        if (etMessage != null) {

            Editable e = etMessage.getEditableText();
            int appendCharIndex = etMessage.getSelectionStart();
            CharSequence cs = e.subSequence(0, appendCharIndex);

            String mentionedName = getMentionSearchName(cs);

            if (!TextUtils.isEmpty(mentionedName)) {
                showSearchMembersInfo(mentionedName);
            } else {
                showSearchMembersInfo("");
            }
        }
    }

    private void addTextWatcher(EditText editText) {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeEditTextChanged(editText, s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editTextChanged(s, editText, before, start, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                afterEditTextChanged(s, editText);
            }
        };

        editText.addTextChangedListener(textWatcher);
    }

    void beforeEditTextChanged(TextView tv, CharSequence s, int start, int count, int after) {
        beforeTextCnt = count;
        beforeText = s.toString();
    }

    void editTextChanged(CharSequence s, TextView tv, int before, int start, int count) {
        afterTextCnt = count;
        afterText = s.toString();
    }

    void afterEditTextChanged(Editable s, TextView tv) {

        // this is something removed case
        if (beforeTextCnt > afterTextCnt) {

            // 특정 폰( EX. NEXUS 5 )에서는 별도의 처리가 필요하다.
            // 기본적으로 멘션된 이름은 삭제시 블록이 한번에 지워지지만
            // 특정 폰에서는 블록에서 삭제 시도할 때 블록안의 내용을 하나씩 지워나간다.
            // 따라서 지워진 글자의 앞부분이 멘션 블록이라고 판단되면 블록 전체를 날려버리고
            // REMOVED TEXT를 멘션 블록으로 치환하는 코드를 삽입하였다.
            String tempString = findMentionedMemberForGoogleKeyboard(
                    afterText.substring(0, tv.getSelectionStart()));
            if (tempString != null) {

                etMessage.removeTextChangedListener(textWatcher);
                int starIndex = tv.getSelectionStart() - tempString.length();
                int endIndex = tv.getSelectionStart();
                etMessage.setText(s.delete(starIndex, endIndex));
                tempString += "\u2063";
                removedText = tempString;
                etMessage.addTextChangedListener(textWatcher);
                etMessage.setSelection(starIndex);

            } else {

                removedText = returnRemoveText(beforeText, afterText, tv.getSelectionStart());

            }

        }

        Editable e = tv.getEditableText();
        int appendCharIndex = tv.getSelectionStart();
        CharSequence cs = e.subSequence(0, appendCharIndex);

        String mentionedName = getMentionSearchName(cs);

        if (mentionedName != null) {

            currentSearchKeywordString = mentionedName;
            showSearchMembersInfo(currentSearchKeywordString);

        } else {

            removeAllMemberList();

        }

    }

    // @ 이후로 부터 검색에 필요한 이름을 얻어오는 메서드
    private String getMentionSearchName(CharSequence cs) {
        Matcher matcher = MENTION_PATTERN_FOR_SEARCH.matcher(cs);
        String result = null;
        while (matcher.find()) {
            result = matcher.group(2);
        }
        return result;
    }

    //for only google keyboard issue
    private String findMentionedMemberForGoogleKeyboard(String rawMemberText) {
        if (rawMemberText != null) {
            Matcher matcher = MENTION_PATTERN_FOR_GOOGLE_KEYBOARD.matcher(rawMemberText);
            while (matcher.find()) {
                String find = matcher.group(0);
                return find;
            }
        }
        return null;
    }

    // 실제 화면에 멘션 가능한 사람들을 보여주는 메서드
    private void showSearchMembersInfo(String searchString) {
        mentionMemberListAdapter.setSearchedMembersList(
                searchMemberModel.getUserSearchByName(searchString));

        boolean hasMembers = mentionMemberListAdapter.getCount() > 0;
        if (hasMembers) {
            setMentionListPopupWidth();
        }

        if (onMentionShowingListener != null) {
            onMentionShowingListener.onMentionShowing(hasMembers);
        }
        mentionMemberListAdapter.notifyDataSetChanged();
    }

    private void setMentionListPopupWidth() {
        int widthPixels = etMessage.getResources().getDisplayMetrics().widthPixels;
        int popupWidth = (widthPixels / 2 - etMessage.getLeft()) * 2;
        etMessage.setDropDownWidth(popupWidth);

        if (etMessage.isPopupShowing()) {
            etMessage.showDropDown();
        }
    }

    // 검색 대상 리스트를 모두 삭제하는 메서드
    private void removeAllMemberList() {
        mentionMemberListAdapter.clear();
        if (onMentionShowingListener != null) {
            onMentionShowingListener.onMentionShowing(false);
        }
    }

    // 멘션 가능한 멤버 리스트 뷰의 view단을 컨트롤 하는 메서드
    private void showListView(boolean isShow) {

        if (isShow && !etMessage.isPopupShowing()) {
            etMessage.showDropDown();
            if (onMentionShowingListener != null) {
                onMentionShowingListener.onMentionShowing(true);
            }
        } else if (!isShow && etMessage.isPopupShowing()) {
            etMessage.dismissDropDown();
            if (onMentionShowingListener != null) {
                onMentionShowingListener.onMentionShowing(false);
            }
        }

    }

    // editText로 부터 삭제된 스트링을 얻어오는 메서드
    private String returnRemoveText(String beforeText, String afterText, int currentSelection) {
        if (beforeTextCnt <= afterTextCnt) {
            return null;
        }

        String removedText = beforeText;
        removedText = removedText.substring(currentSelection, removedText.length());
        removedText = removedText.replace(afterText.substring(currentSelection, afterText.length()), "");

        return removedText;
    }

    // 현재 토픽 또는 파일에서 멘션 가능한 모든 멤버들을 얻어오는 메서드
    public Map<Long, SearchedItemVO> getAllSelectableMembers() {
        return searchMemberModel.getAllSelectableMembers();
    }

    // 토픽 또는 파일의 정보 갱신으로 갱신된 멘션가능한 멤버들을 얻어오는 메서드
    public void refreshSelectableMembers(long teamId, List<Long> roomIds) {
        searchMemberModel.refreshSelectableMembers(teamId, roomIds, mentionType, null);
    }

    // 멘션된 멤버들을 etMessage 뷰 단에서 하일라이트 처리하는 로직
    public void mentionedMemberHighlightInEditText(SearchedItemVO searchedItemVO) {

        int selectionIndex = etMessage.getSelectionStart();
        int startIndex = selectionIndex - currentSearchKeywordString.length();

        SpannableStringBuilder ssb = new SpannableStringBuilder(etMessage.getEditableText());

        MentionMessageSpannable spannable = new MentionMessageSpannable(
                searchedItemVO.getName(),
                etMessage.getTextSize(),
                0xFFfefefe,
                0xFF01a4e7
        );

        float maginDp = 50;
        float marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                maginDp, etMessage.getContext().getResources().getDisplayMetrics());
        spannable.setViewMaxWidthSize(etMessage.getWidth() - (int) marginPx);

        StringBuilder convertedTextBuilder = new StringBuilder();

        convertedTextBuilder
                .append(searchedItemVO.getName())
                .append("\u2063")
                .append(searchedItemVO.getId())
                .append("\u2063 ");

        ssb.replace(startIndex, selectionIndex, convertedTextBuilder);

        ssb.setSpan(spannable, startIndex - 1, startIndex + convertedTextBuilder.length() - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        etMessage.setText(ssb, TextView.BufferType.SPANNABLE);
        etMessage.setSelection(startIndex + convertedTextBuilder.length());

    }

    public void setUpMention(String comment) {
        if (TextUtils.isEmpty(comment)) {
            return;
        }
        StringBuilder builder = new StringBuilder(comment);
        Matcher matcher = MENTION_PATTERN.matcher(comment);

        List<Pair<Integer, Integer>> replaceIndex = new ArrayList<>();

        Map<Long, SearchedItemVO> selectableMembers = getAllSelectableMembers();

        String findId;
        // 해제할 멘션 정보.
        while (matcher.find()) {
            findId = matcher.group(2);
            try {
                long id = Long.parseLong(findId);
                if (!selectableMembers.containsKey(id)) {
                    replaceIndex.add(new Pair<>(matcher.start(2) - 1, matcher.end(2) + 1));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // 멘션 정보 수정
        for (int idx = replaceIndex.size() - 1; idx >= 0; idx--) {
            Pair<Integer, Integer> replace = replaceIndex.get(idx);
            if (replace != null && replace.first >= 0 && replace.second >= 0) {
                builder.replace(replace.first, replace.second, "");
            }
        }

        // 멘션 정보 반영
        matcher = MENTION_PATTERN.matcher(builder.toString());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(builder.toString());
        while (matcher.find()) {

            int nameStart = matcher.start(1);
            int nameEnd = matcher.end(1);

            MentionMessageSpannable spannable = new MentionMessageSpannable(
                    builder.substring(nameStart, nameEnd),
                    etMessage.getTextSize(),
                    0xFFfefefe,
                    0xFF01a4e7
            );
            float maginDp = 50;
            float marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    maginDp, etMessage.getContext().getResources().getDisplayMetrics());
            spannable.setViewMaxWidthSize(etMessage.getMeasuredWidth() - (int) marginPx);


            spannableStringBuilder.setSpan(spannable, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        etMessage.setText(spannableStringBuilder);
        etMessage.setSelection(etMessage.length());
    }

    public ResultMentionsVO getMentionInfoObject() {
        return getMentionInfoObject(etMessage.getText().toString().trim(),
                getAllSelectableMembers());
    }

    // use to get converted message for clipboard
    public ResultMentionsVO getMentionInfoObject(String message) {
        return getMentionInfoObject(message,
                getAllSelectableMembers());
    }

    public boolean hasMentionMember() {
        return searchMemberModel.getAllSelectableMembers().size() > 0;
    }

    public void setTextOnClip(String pasteData) {
        ClipboardManager clipBoard = (ClipboardManager) etMessage.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipBoard.removePrimaryClipChangedListener(clipboardListener);
        android.content.ClipData clip =
                android.content.ClipData.newPlainText("Copied Text", pasteData);
        clipBoard.setPrimaryClip(clip);
        clipBoard.addPrimaryClipChangedListener(clipboardListener);
    }

    public void removeClipboardListener() {
        if (clipboardListener != null) {
            clipBoard.removePrimaryClipChangedListener(clipboardListener);
        }
    }

    public void registClipboardListener() {
        removeClipboardListener();
        clipboardListener = new ClipboardListener();
        clipBoard.addPrimaryClipChangedListener(clipboardListener);
    }

    public void onConfigurationChanged() {
        setMentionListPopupWidth();
    }

    public void reset() {
        etMessage.removeTextChangedListener(textWatcher);
        removeClipboardListener();
        showListView(false);
    }

    public interface OnMentionShowingListener {
        void onMentionShowing(boolean isShowing);
    }

    // 가공되지 않은 스트링이 클립보드에 복사되면 안되므로 별도의 처리 진행
    class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {

        @Override
        public void onPrimaryClipChanged() {
            if (etMessage == null)
                return;

            // if U cut the string in the etMessage, etMessage already removed all string.
            String et;
            boolean isCut = false;
            if (afterText.length() == 0 && beforeText.length() > 0) {
                et = beforeText;
                isCut = true;
            } else {
                Editable text = etMessage.getText();
                if (!TextUtils.isEmpty(text)) {
                    et = text.toString();
                } else {
                    et = "";
                }
            }
            ClipboardManager clipBoard = (ClipboardManager) etMessage.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            CharSequence pasteData;
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText();
            if (!TextUtils.isEmpty(pasteData) && et.contains(pasteData.toString())) {
                String convertedMessage;
                if (isCut) {
                    convertedMessage =
                            getMentionInfoObject(et).getMessage();
                } else {
                    convertedMessage =
                            getMentionInfoObject(et).getMessage();
                }
                Log.e(convertedMessage, convertedMessage);
                setTextOnClip(convertedMessage);
            }

        }
    }

}
