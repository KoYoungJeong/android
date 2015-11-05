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

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.MentionMemberListAdapter;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel_;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel_;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tee on 15. 7. 21..
 */

public class MentionControlViewModel {

    public static final String MENTION_TYPE_MESSAGE = "mention_type_message";
    public static final String MENTION_TYPE_FILE_COMMENT = "mention_type_file_comment";
    private AutoCompleteTextView etMessage;
    private KeyboardHeightModel keyboardHeightModel;
    private SearchMemberModel searchMemberModel;

    //message or file view type
    private String mentionType = MENTION_TYPE_MESSAGE;

    //for textControl
    private int beforeTextCnt = 0;
    private int afterTextCnt = 0;
    private String beforeText = "";
    private String afterText = "";
    private String removedText = "";
    private String currentSearchKeywordString;

    private ClipboardListener clipboardListener;
    private TextWatcher textWatcher;

    private ClipboardManager clipBoard;
    private MentionMemberListAdapter mentionMemberListAdapter;

    private MentionControlViewModel(Activity activity,
                                    EditText editText,
                                    int teamId,
                                    List<Integer> roomIds,
                                    String mentionType) {

        this.clipBoard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        this.mentionType = mentionType;

        init(activity, editText, teamId, roomIds);

    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      List<Integer> roomIds,
                                                      String mentionType) {
        int teamId = EntityManager.getInstance().getTeamId();
        return new MentionControlViewModel(activity,
                editText,
                teamId,
                roomIds,
                mentionType);
    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      int teamId,
                                                      List<Integer> roomIds,
                                                      String mentionType) {
        return new MentionControlViewModel(activity,
                editText,
                teamId,
                roomIds,
                mentionType);
    }

    private void init(Activity activity,
                      EditText editText,
                      int teamId,
                      List<Integer> roomIds) {

        this.etMessage = (AutoCompleteTextView) editText;

        addTextWatcher(editText);

        keyboardHeightModel = KeyboardHeightModel_.getInstance_(activity);
        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        refreshSelectableMembers(teamId, roomIds);

        List<SearchedItemVO> users = searchMemberModel.getUserSearchByName("");
        mentionMemberListAdapter = new MentionMemberListAdapter(activity, users);

        etMessage.setAdapter(mentionMemberListAdapter);
        etMessage.setDropDownBackgroundResource(R.drawable.mention_popup);
        etMessage.setThreshold(1);

        if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
            keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                if (!isShow) {
                    showListView(false);
                }
            });
        }

    }

    public void refreshMembers(List<Integer> roomIds) {
        refreshMembers(EntityManager.getInstance().getTeamId(), roomIds);
    }

    public void refreshMembers(int teamId, List<Integer> roomIds) {
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
    public String getMentionSearchName(CharSequence cs) {
        Pattern p = Pattern.compile("(?:(?:^|\\s)([@\\uff20]((?:[^@\\uff20]){0,30})))$");
        Matcher matcher = p.matcher(cs);
        String result = null;
        while (matcher.find()) {
            result = matcher.group(2);
        }
        return result;
    }

    //for only google keyboard issue
    private String findMentionedMemberForGoogleKeyboard(String rawMemberText) {
        if (rawMemberText != null) {
            Pattern p = Pattern.compile("@([^@]+)(?:\\u2063)(\\d+)$");
            Matcher matcher = p.matcher(rawMemberText);
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

        if (mentionMemberListAdapter.getCount() > 0) {
            setMetionListPopupWidth();
        }

        mentionMemberListAdapter.notifyDataSetChanged();
    }

    private void setMetionListPopupWidth() {
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
    }

    // 멘션 가능한 멤버 리스트 뷰의 view단을 컨트롤 하는 메서드
    private void showListView(boolean isShow) {

        if (isShow && !etMessage.isPopupShowing()) {
            etMessage.showDropDown();
        } else if (!isShow && etMessage.isPopupShowing()) {
            etMessage.dismissDropDown();
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
    public LinkedHashMap<Integer, SearchedItemVO> getAllSelectableMembers() {
        return searchMemberModel.getAllSelectableMembers();
    }

    // 토픽 또는 파일의 정보 갱신으로 갱신된 멘션가능한 멤버들을 얻어오는 메서드
    public void refreshSelectableMembers(int teamId, List<Integer> roomIds) {
        searchMemberModel.refreshSelectableMembers(teamId, roomIds, mentionType);
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
                .append("\u2063");

        ssb.replace(startIndex, selectionIndex, convertedTextBuilder);

        ssb.setSpan(spannable, startIndex - 1, startIndex + convertedTextBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        etMessage.setText(ssb, TextView.BufferType.SPANNABLE);
        etMessage.setSelection(startIndex + convertedTextBuilder.length());

    }

    public void setUpMention(String comment) {
        if (TextUtils.isEmpty(comment)) {
            return;
        }
        StringBuilder builder = new StringBuilder(comment);
        Pattern p = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)(?:\\u2063)");
        Matcher matcher = p.matcher(comment);

        List<Pair<Integer, Integer>> replaceIndex = new ArrayList<>();

        LinkedHashMap<Integer, SearchedItemVO> selectableMembers = getAllSelectableMembers();

        String findId = "";
        // 해제할 멘션 정보.
        while (matcher.find()) {
            findId = matcher.group(2);
            try {
                int id = Integer.parseInt(findId);
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
        matcher = p.matcher(builder.toString());
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

    // 현재까지의 editText에서 멘션 가공된 message와 mention object 리스트를 얻어오는 메서드
    private ResultMentionsVO getMentionInfoObject(String message,
                                                  LinkedHashMap<Integer, SearchedItemVO> selectableMembers) {

        StringBuilder builder = new StringBuilder(message);
        String findId = "";
        Pattern p = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)(?:\\u2063)");
        Matcher matcher = p.matcher(message);

        List<SearchedItemVO> orderedSearchedMember = new ArrayList<>();

        List<Pair<Integer, Integer>> replaceIndex = new ArrayList<>();

        while (matcher.find()) {
            findId = matcher.group(2);
            try {
                int id = Integer.parseInt(findId);
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
        return true;
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
        setMetionListPopupWidth();
    }

    public void reset() {
        etMessage.removeTextChangedListener(textWatcher);
        if (keyboardHeightModel != null) {
            keyboardHeightModel.setOnKeyboardShowListener(null);
        }
        removeClipboardListener();
        showListView(false);
    }

    // 가공되지 않은 스트링이 클립보드에 복사되면 안되므로 별도의 처리 진행
    class ClipboardListener implements
            ClipboardManager.OnPrimaryClipChangedListener {
        public void onPrimaryClipChanged() {

            if (etMessage == null)
                return;

            // if U cut the string in the etMessage, etMessage already removed all string.
            String et = null;
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
            CharSequence pasteData = "";
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText();
            if (et.contains(pasteData.toString())) {
                String convertedMessage = null;
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
