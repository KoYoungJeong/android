package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Iterator;
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

    private RecyclerView lvSearchMember;
    private EditText etMessage;

    // for Message List View
    private RecyclerView lvMessage;

    // for File comment List View
    private ListView lvFileComment;

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
    private List<Integer> roomIds;

    // restore mentioned members using hashmap for time complexity
    private LinkedHashMap<Integer, SearchedItemVO> selectedMemberHashMap;
    private ClipboardListener clipboardListener;
    private TextWatcher textWatcher;

    // 멘션 선택된 멤버 리스트의 사본
    // 클립 보드에서 CUT(잘라내기) 시 해당 정보를 한꺼번에 잃기 때문에 사본을 저장할 필요성 있음.
    private LinkedHashMap<Integer, SearchedItemVO> cloneSelectedMemberHashMap;
    private ClipboardManager clipBoard;

    public MentionControlViewModel(Activity activity,
                                   EditText editText,
                                   RecyclerView lvSearchMember, View lvMessage,
                                   List<Integer> roomIds,
                                   String mentionType) {

        this.clipBoard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        this.mentionType = mentionType;
        if (mentionType.equals(MENTION_TYPE_MESSAGE)) {
            this.lvMessage = (RecyclerView) lvMessage;
        } else if (mentionType.equals(MENTION_TYPE_FILE_COMMENT)) {
            this.lvFileComment = (ListView) lvMessage;
        }

        init(activity, lvSearchMember, editText, roomIds);

    }

    public static MentionControlViewModel newInstance(Activity activity,
                                                      EditText editText,
                                                      RecyclerView lvSearchMember, View lvMessage,
                                                      List<Integer> roomIds,
                                                      String mentionType) {
        return new MentionControlViewModel(activity,
                editText,
                lvSearchMember, lvMessage,
                roomIds,
                mentionType);
    }

    private void init(Activity activity,
                      RecyclerView lvSearchMember,
                      EditText editText,
                      List<Integer> roomIds) {

        this.lvSearchMember = lvSearchMember;
        this.etMessage = editText;
        this.roomIds = roomIds;

        addTextWatcher(editText);

        keyboardHeightModel = KeyboardHeightModel_.getInstance_(activity);
        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        refreshSelectableMembers(roomIds);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        lvSearchMember.setLayoutManager(layoutManager);
        lvSearchMember.setAdapter(new MentionMemberListAdapter(
                searchMemberModel.getUserSearchByName("", null)));

        if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
            keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                if (!isShow) {
                    showListView(false);
                }
            });
        }

        selectedMemberHashMap = new LinkedHashMap<>();

    }

    public void refreshMembers(List<Integer> roomIds) {
        refreshSelectableMembers(roomIds);
        if (lvSearchMember != null
                && lvSearchMember.getAdapter() != null
                && etMessage != null) {

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
        editText.addTextChangedListener(
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
                }
        );
    }

    void beforeEditTextChanged(TextView tv, CharSequence s, int start, int count, int after) {
        beforeTextCnt = count;
        cloneSelectedMemberHashMap = (LinkedHashMap<Integer, SearchedItemVO>) selectedMemberHashMap.clone();
        beforeText = s.toString();
    }

    void editTextChanged(CharSequence s, TextView tv, int before, int start, int count) {
        afterTextCnt = count;
        afterText = s.toString();
        if (s.length() == 0) {
            selectedMemberHashMap.clear();
        }
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

            removeSelectedMentionMemberInfo(removedText);

        }

        Editable e = tv.getEditableText();
        int appendCharIndex = tv.getSelectionStart();
        CharSequence cs = e.subSequence(0, appendCharIndex);

        String mentionedName = getMentionSearchName(cs);

        if (mentionedName != null) {

            currentSearchKeywordString = mentionedName;
            showSearchMembersInfo(currentSearchKeywordString);
            if (getMembersListByAdapter().size() > 0) {
                showListView(true);
            } else {
                showListView(false);
            }

        } else {

            removeAllMemberList();
            showListView(false);

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
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) lvSearchMember.getAdapter();
        mentionMemberListAdapter.setSearchedMembersList(
                searchMemberModel.getUserSearchByName(searchString, selectedMemberHashMap));
    }

    // 검색 대상 리스트를 모두 삭제하는 메서드
    private void removeAllMemberList() {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) lvSearchMember.getAdapter();
        mentionMemberListAdapter.clearMembersList();
    }

    // 멘션 가능한 멤버 리스트 뷰의 view단을 컨트롤 하는 메서드
    private void showListView(boolean isShow) {

        if (isShow) {
            lvSearchMember.setVisibility(View.VISIBLE);
            if (lvMessage != null) {
                lvMessage.setVisibility(View.INVISIBLE);
            } else if (lvFileComment != null) {
                lvFileComment.setVisibility(View.INVISIBLE);
            }
        } else {
            lvSearchMember.setVisibility(View.INVISIBLE);
            if (lvMessage != null) {
                lvMessage.setVisibility(View.VISIBLE);
            } else if (lvFileComment != null) {
                lvFileComment.setVisibility(View.VISIBLE);
            }
        }

    }

    public List<SearchedItemVO> getMembersListByAdapter() {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) lvSearchMember.getAdapter();
        return mentionMemberListAdapter.getSearchedMembersList();
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

    // 멘션된 멤버를 리스트에 저장하는 메서드
    private void restoreSelectedMentionMemberInfo(String rawMemberText) {
        if (rawMemberText != null) {
            String id = "";
            Pattern p = Pattern.compile("(?:@)([^@]+)(?:\\u2063)(\\d+)(?:\\u2063)");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                id = matcher.group(2);
                selectedMemberHashMap.put(new Integer(id),
                        getAllSelectableMembers().get(new Integer(id)));
            }
        }
    }

    // 멘션된 멤버를 리스트에서 제거하는 메서드
    private void removeSelectedMentionMemberInfo(String rawMemberText) {
        if (rawMemberText != null) {
            String id = "";
            Pattern p = Pattern.compile("(?:@)([^@]+)(?:\\u2063)(\\d+)(?:\\u2063)");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                id = matcher.group(2);
                selectedMemberHashMap.remove(Integer.valueOf(id));
            }
        }
    }

    // 현재 토픽 또는 파일에서 멘션 가능한 모든 멤버들을 얻어오는 메서드
    public LinkedHashMap<Integer, SearchedItemVO> getAllSelectableMembers() {
        return searchMemberModel.getAllSelectableMembers();
    }

    // 토픽 또는 파일의 정보 갱신으로 갱신된 멘션가능한 멤버들을 얻어오는 메서드
    public void refreshSelectableMembers(List<Integer> roomIds) {
        searchMemberModel.refreshSelectableMembers(roomIds, mentionType);
    }

    // 멘션된 멤버들을 etMessage 뷰 단에서 하일라이트 처리하는 로직
    public void mentionedMemberHighlightInEditText(SearchedItemVO searchedItemVO) {

        int selectionIndex = etMessage.getSelectionStart();
        int startIndex = selectionIndex - currentSearchKeywordString.length();

        SpannableStringBuilder ssb = new SpannableStringBuilder(etMessage.getEditableText());

        MentionMessageSpannable spannable = new MentionMessageSpannable(
                etMessage.getContext(),
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

        restoreSelectedMentionMemberInfo("@" + convertedTextBuilder);

        ssb.replace(startIndex, selectionIndex, convertedTextBuilder);

        ssb.setSpan(spannable, startIndex - 1, startIndex + convertedTextBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        etMessage.setText(ssb, TextView.BufferType.SPANNABLE);
        etMessage.setSelection(startIndex + convertedTextBuilder.length());

    }

    // 현재까지의 editText에서 멘션 가공된 message와 mention object 리스트를 얻어오는 메서드
    private ResultMentionsVO getMentionInfoObject(String message,
                                                  LinkedHashMap<Integer, SearchedItemVO> selectedMembers,
                                                  LinkedHashMap<Integer, SearchedItemVO> selectableMembers) {

        if (selectedMembers.size() == 0) {
            return new ResultMentionsVO(message, new ArrayList<MentionObject>());
        }

        StringBuilder builder = new StringBuilder(message);
        String findId = "";
        Pattern p = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)(?:\\u2063)");
        Matcher matcher = p.matcher(message);

        LinkedHashMap<Integer, SearchedItemVO> orderedSearchedMember = new LinkedHashMap<>();

        while (matcher.find()) {
            findId = matcher.group(2);
            try {
                int id = Integer.parseInt(findId);
                if (selectableMembers.containsKey(id)) {
                    orderedSearchedMember.put(id, selectableMembers.get(id));
                } else {
                    builder.replace(matcher.start(2) - 1, matcher.end(2) + 1, "");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        Iterator iterator = orderedSearchedMember.keySet().iterator();

        List<MentionObject> mentions = new ArrayList<>();

        StringBuilder memberInfoStringSB;

        while (iterator.hasNext()) {

            int key = (Integer) iterator.next();
            memberInfoStringSB = new StringBuilder();
            SearchedItemVO searchedItemVO = orderedSearchedMember.get(Integer.valueOf(key));

            if (searchedItemVO == null) {
                continue;
            }

            String name = searchedItemVO.getName();
            String id = String.valueOf(key);
            String type = searchedItemVO.getType();

            memberInfoStringSB
                    .append(name)
                    .append("\u2063")
                    .append(id)
                    .append("\u2063");

            int startIndexOfMemberString = builder.indexOf(memberInfoStringSB.toString());

            builder.replace(startIndexOfMemberString,
                    startIndexOfMemberString + memberInfoStringSB.length(), name);

            int offset = startIndexOfMemberString - 1;
            int length = name.length() + 1;
            MentionObject mentionInfo = new MentionObject(key, type, offset, length);
            mentions.add(mentionInfo);

        }

        return new ResultMentionsVO(builder.toString(), mentions);

    }

    public ResultMentionsVO getMentionInfoObject() {
        return getMentionInfoObject(etMessage.getText().toString().trim(),
                selectedMemberHashMap, getAllSelectableMembers());
    }

    // use to get converted message for clipboard
    public ResultMentionsVO getMentionInfoObject(
            String string, LinkedHashMap<Integer, SearchedItemVO> selectedMemberHashMap) {
        return getMentionInfoObject(string,
                selectedMemberHashMap, getAllSelectableMembers());
    }

    public boolean hasMentionMember() {
        if (selectedMemberHashMap.size() > 0) {
            return true;
        }
        return false;
    }

    public void clear() {
        selectedMemberHashMap.clear();
    }

    public boolean isMentionListVisible() {
        return lvSearchMember.getVisibility() == View.VISIBLE;
    }

    public void dismissMentionList() {
        showListView(false);
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
                            getMentionInfoObject(et, cloneSelectedMemberHashMap).getMessage();
                } else {
                    convertedMessage =
                            getMentionInfoObject(et, selectedMemberHashMap).getMessage();
                }
                Log.e(convertedMessage, convertedMessage);
                setTextOnClip(convertedMessage);
            }

        }
    }

}
