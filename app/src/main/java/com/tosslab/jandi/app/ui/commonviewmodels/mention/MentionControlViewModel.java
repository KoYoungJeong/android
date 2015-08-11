package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.MentionMemberListAdapter;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel_;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.util.MentionSpannable;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel_;

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
    public static final int MENTION_MEMBER_DELETE = 0x01;
    public static final int MENTION_MEMBER_INSERT = 0x02;

    private RecyclerView searchMemberListView;
    private EditText editText;

    // for Message List View
    private RecyclerView messageListView;

    // for File comment List View
    private ListView fileCommentListView;
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
    private OnMentionViewShowingListener onMentionViewShowingListener;

    // 클립 보드에서 CUT(잘라내기) 시 해당 정보를 한꺼번에 잃기 때문에 저장할 필요성이 있음.
    private LinkedHashMap<Integer, SearchedItemVO> cloneSelectedMemberHashMap;

    public MentionControlViewModel(Activity activity, RecyclerView searchMemberListView,
                                   EditText editText, RecyclerView messageListView,
                                   List<Integer> roomIds) {
        this.messageListView = messageListView;
        this.mentionType = MENTION_TYPE_MESSAGE;
        init(activity, searchMemberListView, editText, roomIds);
    }

    public MentionControlViewModel(Activity activity, RecyclerView searchMemberListView,
                                   EditText editText, ListView fileCommentListView,
                                   List<Integer> roomIds) {
        this.fileCommentListView = fileCommentListView;
        this.mentionType = MENTION_TYPE_FILE_COMMENT;
        init(activity, searchMemberListView, editText, roomIds);
    }

    private void init(Activity activity, RecyclerView searchMemberListView, EditText editText,
                      List<Integer> roomIds) {

        this.searchMemberListView = searchMemberListView;
        this.editText = editText;
        this.roomIds = roomIds;

        addTextWatcher(editText);

        keyboardHeightModel = KeyboardHeightModel_.getInstance_(activity);
        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        refreshSelectableMembers(roomIds);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        searchMemberListView.setLayoutManager(layoutManager);
        searchMemberListView.setAdapter(new MentionMemberListAdapter(
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

    public void setOnMentionViewShowingListener(OnMentionViewShowingListener listener) {
        onMentionViewShowingListener = listener;
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

                editText.removeTextChangedListener(textWatcher);
                int starIndex = tv.getSelectionStart() - tempString.length();
                int endIndex = tv.getSelectionStart();
                editText.setText(s.delete(starIndex, endIndex));
                tempString += "\u2063";
                removedText = tempString;
                editText.addTextChangedListener(textWatcher);
                editText.setSelection(starIndex);

            } else {

                removedText = returnRemoveText(beforeText, afterText, tv.getSelectionStart());

            }

            restoreOrDeleteSelectedMentionMemberInfo(MENTION_MEMBER_DELETE, removedText);

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
            Pattern p = Pattern.compile("(?:@)([^\\u2063]+)(?:\\u2063)(\\d+)$");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                String find = matcher.group(0);
                return find;
            }
        }
        return null;
    }

    private void showSearchMembersInfo(String searchString) {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) searchMemberListView.getAdapter();
        mentionMemberListAdapter.setSearchedMembersList(
                searchMemberModel.getUserSearchByName(searchString, selectedMemberHashMap));
    }

    private void removeAllMemberList() {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) searchMemberListView.getAdapter();
        mentionMemberListAdapter.clearMembersList();
    }

    private void showListView(boolean isShow) {

        if (isShow) {
            searchMemberListView.setVisibility(View.VISIBLE);
            if (messageListView != null) {
                messageListView.setVisibility(View.INVISIBLE);
            } else if (fileCommentListView != null) {
                fileCommentListView.setVisibility(View.INVISIBLE);
            }
        } else {
            searchMemberListView.setVisibility(View.INVISIBLE);
            if (messageListView != null) {
                messageListView.setVisibility(View.VISIBLE);
            } else if (fileCommentListView != null) {
                fileCommentListView.setVisibility(View.VISIBLE);
            }
        }

        if (onMentionViewShowingListener != null) {
            onMentionViewShowingListener.onMentionViewShowing(isShow);
        }

    }

    public List<SearchedItemVO> getMembersListByAdapter() {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) searchMemberListView.getAdapter();
        return mentionMemberListAdapter.getSearchedMembersList();
    }

    private String returnRemoveText(String beforeText, String afterText, int currentSelection) {
        if (beforeTextCnt <= afterTextCnt) {
            return null;
        }

        String removedText = beforeText;
        removedText = removedText.replace(afterText.substring(0, currentSelection), "");
        removedText = removedText.replace(afterText.substring(currentSelection, afterText.length()), "");

        return removedText;
    }

    private void restoreOrDeleteSelectedMentionMemberInfo(int request, String rawMemberText) {
        if (rawMemberText != null) {
            String id = "";
            Pattern p = Pattern.compile("(?:@)([^\\u2063].+)(?:\\u2063)(\\d+)(?:\\u2063)");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                id = matcher.group(2);
                if (request == MENTION_MEMBER_INSERT) {
                    selectedMemberHashMap.put(new Integer(id),
                            getAllSelectableMembers().get(new Integer(id)));
                } else if (request == MENTION_MEMBER_DELETE) {
                    selectedMemberHashMap.remove(Integer.valueOf(id));
                }
            }
        }
    }

    public LinkedHashMap<Integer, SearchedItemVO> getAllSelectableMembers() {
        return searchMemberModel.getAllSelectableMembers();
    }

    public void refreshSelectableMembers(List<Integer> roomIds) {
        searchMemberModel.refreshSelectableMembers(roomIds, mentionType);
    }

    public void mentionedMemberHighlightInEditText(SearchedItemVO searchedItemVO) {

        int selectionIndex = editText.getSelectionStart();
        int startIndex = selectionIndex - currentSearchKeywordString.length();

        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getEditableText());

        MentionSpannable spannable = new MentionSpannable(editText.getContext(), searchedItemVO.getName());

        StringBuilder convertedTextBuilder = new StringBuilder();

        convertedTextBuilder
                .append(searchedItemVO.getName())
                .append("\u2063")
                .append(searchedItemVO.getId())
                .append("\u2063");

        restoreOrDeleteSelectedMentionMemberInfo(MENTION_MEMBER_INSERT, "@" + convertedTextBuilder);

        ssb.replace(startIndex, selectionIndex, convertedTextBuilder);

        ssb.setSpan(spannable, startIndex - 1, startIndex + convertedTextBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        editText.setText(ssb, TextView.BufferType.SPANNABLE);
        editText.setSelection(startIndex + convertedTextBuilder.length());

    }

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
            if (selectedMembers.get(new Integer(findId)) != null) {
                orderedSearchedMember.put(new Integer(findId),
                        selectableMembers.get(new Integer(findId)));
            }
        }

        Iterator iterator = orderedSearchedMember.keySet().iterator();

        List<MentionObject> mentions = new ArrayList<>();

        while (iterator.hasNext()) {

            int key = (Integer) iterator.next();
            StringBuilder memberInfoStringSB = new StringBuilder();
            SearchedItemVO searchedItemVO = orderedSearchedMember.get(Integer.valueOf(key));

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
        return getMentionInfoObject(editText.getText().toString().trim(),
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
        return searchMemberListView.getVisibility() == View.VISIBLE;
    }

    public void dismissMentionList() {
        showListView(false);
    }

    public void setTextOnClip(String pasteData) {
        ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
        clipBoard.removePrimaryClipChangedListener(clipboardListener);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", pasteData);
        clipBoard.setPrimaryClip(clip);
        clipBoard.addPrimaryClipChangedListener(clipboardListener);
    }

    public void removeClipboardListener() {
        ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
        clipBoard.removePrimaryClipChangedListener(clipboardListener);
    }

    public void registClipboardListener() {
        clipboardListener = new ClipboardListener();
        ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(clipboardListener);
    }

    // for control announcement view
    public interface OnMentionViewShowingListener {
        void onMentionViewShowing(boolean isShowing);
    }

    class ClipboardListener implements
            ClipboardManager.OnPrimaryClipChangedListener {
        public void onPrimaryClipChanged() {

            // if U cut the string in the editText, editText already removed all string.
            String et = null;
            boolean isCut = false;
            if (afterText.length() == 0 && beforeText.length() > 0) {
                et = beforeText;
                isCut = true;
            } else {
                et = editText.getText().toString();
            }
            Log.e("et", et);
            ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                    .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
            CharSequence pasteData = "";
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText();
            if (et.contains(pasteData.toString())) {
                String convertedMessage = null;
                if (isCut) {
                    convertedMessage = getMentionInfoObject(et, cloneSelectedMemberHashMap).getMessage();
                } else {
                    convertedMessage = getMentionInfoObject(et, selectedMemberHashMap).getMessage();
                }
                Log.e(convertedMessage, convertedMessage);
                setTextOnClip(convertedMessage);

            }

        }
    }

}
