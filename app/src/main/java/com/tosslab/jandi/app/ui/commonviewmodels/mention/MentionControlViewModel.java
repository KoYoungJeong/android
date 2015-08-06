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
    private RecyclerView searchMemberListView;
    private EditText editText;
    // for Message List View
    private RecyclerView messageListView;
    // for File comment List View
    private ListView fileCommentListView;
    private KeyboardHeightModel keyboardHeightModel;
    private SearchMemberModel searchMemberModel;
    //MESSAGE OR FILE VIEW TYPE
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

    public MentionControlViewModel(Activity activity, RecyclerView searchMemberListView,
                                   EditText editText, RecyclerView messageListView,
                                   List<Integer> roomIds) {
        this.messageListView = messageListView;
        this.mentionType = MENTION_TYPE_MESSAGE;
        init(activity, searchMemberListView, editText, roomIds, mentionType);
    }

    public MentionControlViewModel(Activity activity, RecyclerView searchMemberListView,
                                   EditText editText, ListView fileCommentListView,
                                   List<Integer> roomIds) {
        this.fileCommentListView = fileCommentListView;
        this.mentionType = MENTION_TYPE_FILE_COMMENT;
        init(activity, searchMemberListView, editText, roomIds, mentionType);
    }

    public boolean isMentionListVisible() {
        return searchMemberListView.getVisibility() == View.VISIBLE;
    }

    private void init(Activity activity, RecyclerView searchMemberListView, EditText editText,
                      List<Integer> roomIds, String mentionType) {

        this.searchMemberListView = searchMemberListView;
        this.editText = editText;
        this.roomIds = roomIds;

        addTextWatcher(editText);

        keyboardHeightModel = KeyboardHeightModel_.getInstance_(activity);
        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        searchMemberListView.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        searchMemberListView.setAdapter(new MentionMemberListAdapter(
                searchMemberModel.getUserSearchByName(null, "", null, mentionType)));

        if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
            keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                if (!isShow) {
                    showListView(false);
                }
            });
        }

        selectedMemberHashMap = new LinkedHashMap<>();

        clipboardListener = new ClipboardListener();
        ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(clipboardListener);

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

    void beforeEditTextChanged(TextView tv, CharSequence s, int start, int count,
                               int after) {
        beforeTextCnt = count;
        beforeText = s.toString();
    }

    void editTextChanged(CharSequence s, TextView tv, int before, int start, int count) {
        // Something Here
        afterTextCnt = count;
        afterText = s.toString();
    }

    void afterEditTextChanged(Editable s, TextView tv) {
        // Something Here

        if (beforeTextCnt > afterTextCnt) {
            removedText = returnRemoveText(beforeText, afterText, tv.getSelectionStart());
            restoreOrDeleteSelectedMentionMemberInfo(1, removedText);
        }

        Editable e = tv.getEditableText();
        int appendCharIndex = tv.getSelectionStart();
        CharSequence cs = e.subSequence(0, appendCharIndex);
        Pattern p = Pattern.compile("(?:(?:^|\\s)([@\\uff20]((?:[^@\\uff20]|[\\!'#%&'\\(\\)*\\+,\\" +
                "\\\\-\\.\\/:;<=>\\?\\[\\]\\^_{|}~\\$][^ ]){0,30})))$");
        Matcher matcher = p.matcher(cs);
        String result = null;

        while (matcher.find()) {
            result = matcher.group(2);
        }

        if (result != null) {

            currentSearchKeywordString = result;

            showSearchMembersInfo(currentSearchKeywordString, getMentionType());

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

    private void showSearchMembersInfo(String searchString, String type) {
        MentionMemberListAdapter mentionMemberListAdapter =
                (MentionMemberListAdapter) searchMemberListView.getAdapter();
        mentionMemberListAdapter.setSearchedMembersList(
                searchMemberModel.getUserSearchByName(getRoomIds(),
                        searchString, selectedMemberHashMap, type));
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
        String remainText = afterText;
        removedText = removedText.replace(afterText.substring(0, currentSelection), "");
        removedText = removedText.replace(afterText.substring(currentSelection, afterText.length()), "");

        return removedText;
    }

    private void restoreOrDeleteSelectedMentionMemberInfo(int type, String rawMemberText) {
        if (rawMemberText != null) {
            String id = "";
            Pattern p = Pattern.compile("(?:@)([^\\u2063].+)(?:\\u2063)(\\d+)(?:\\u2063)");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                id = matcher.group(2);
                if (type == 0) {
                    selectedMemberHashMap.put(new Integer(id),
                            getSelectableMembersInThis().get(new Integer(id)));
                } else if (type == 1) {
                    selectedMemberHashMap.remove(Integer.valueOf(id));
                }
            }
        }
    }

    public LinkedHashMap<Integer, SearchedItemVO> getSelectableMembersInThis() {
        return searchMemberModel.getSelectableMembers();
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

        restoreOrDeleteSelectedMentionMemberInfo(0, "@" + convertedTextBuilder);

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
            Log.e("id", String.valueOf(findId));
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
                selectedMemberHashMap, getSelectableMembersInThis());
    }

    public ResultMentionsVO getMentionInfoObject(String string) {
        return getMentionInfoObject(string,
                selectedMemberHashMap, getSelectableMembersInThis());
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

    public void setTextOnClip(String pasteData) {
        ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
        clipBoard.removePrimaryClipChangedListener(clipboardListener);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", pasteData);
        clipBoard.setPrimaryClip(clip);
        clipBoard.addPrimaryClipChangedListener(clipboardListener);
    }

    public String getMentionType() {
        return mentionType;
    }

    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public void dismissMentionList() {
        showListView(false);
    }

    public interface OnMentionViewShowingListener {
        void onMentionViewShowing(boolean isShowing);
    }

    class ClipboardListener implements
            ClipboardManager.OnPrimaryClipChangedListener {
        public void onPrimaryClipChanged() {
            String et = editText.getText().toString();
            ClipboardManager clipBoard = (ClipboardManager) editText.getContext()
                    .getSystemService(editText.getContext().CLIPBOARD_SERVICE);
            CharSequence pasteData = "";
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText();
            Log.e("editText", editText.getText().toString());
            Log.e("pasteData", pasteData.toString());
            if (et.contains(pasteData.toString())) {
                String convertedMessage = getMentionInfoObject(pasteData.toString()).getMessage();
                Log.e(convertedMessage, convertedMessage);
                setTextOnClip(convertedMessage);
            }
        }
    }
}
