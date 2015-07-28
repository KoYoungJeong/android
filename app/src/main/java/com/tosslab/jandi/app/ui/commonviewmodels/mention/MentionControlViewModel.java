package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.app.Activity;
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

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ReqMention;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.adapter.MentionListAdapter;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel_;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.util.MensionSpannable;
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

    RecyclerView searchMemberListView;

    EditText editText;

    KeyboardHeightModel keyboardHeightModel;

    SearchMemberModel searchMemberModel;

    // for Message List View
    RecyclerView messageListView;

    // for File comment List View
    ListView fileCommentListView;

    private int beforeTextCnt = 0;
    private int afterTextCnt = 0;
    private String beforeText = "";
    private String afterText = "";
    private String removedText = "";
    private List<Integer> roomIds;
    private String currentSearchText;
    private LinkedHashMap<Integer, SearchedItemVO> selectedMemberHashMap;
    private List<ReqMention> resultMentions;
    private String type = MENTION_TYPE_MESSAGE;

    // finally generated mention info.

    public static final MentionControlViewModel getInstance() {
        return new MentionControlViewModel();
    }

    private void messageTextInit(View view) {
        editText = (EditText) view.findViewById(R.id.et_message);
        addTextWatcher();
    }

    private void addTextWatcher() {
        editText.addTextChangedListener(
                new TextWatcher() {
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

    public void init(View view, Activity activity, String type, List<Integer> roomIds) {
        this.type = type;
        this.roomIds = roomIds;

        searchMemberListView = (RecyclerView) view.findViewById(R.id.rv_list_search_members);

        messageTextInit(view);

        keyboardHeightModel = KeyboardHeightModel_.getInstance_(activity);

        searchMemberModel = SearchMemberModel_.getInstance_(activity);

        if (type.equals(MENTION_TYPE_MESSAGE)) {
            messageListView = (RecyclerView) view.findViewById(R.id.list_messages);
        } else if (type.equals(MENTION_TYPE_FILE_COMMENT)) {
            fileCommentListView = (ListView) view.findViewById(R.id.lv_file_detail_comments);
        }

        searchMemberListView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        searchMemberListView.setAdapter(new MentionListAdapter(searchMemberModel.getUserSearchByName(null, "", null, type)));

        if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
            keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                if (!isShow) {
                    showListView(false);
                }
            });
        }

        selectedMemberHashMap = new LinkedHashMap<>();
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
            int startIndex = cs.length() - (result.length() + 1);
            int endIndex = appendCharIndex;
            setCurrentSearchText(result);
            showSearchMembersInfo(result, type);

            if (getMembersList().size() > 0) {
                showListView(true);
            } else {
                showListView(false);
            }

            currentSearchText = result;

        } else {
            removeAllMemberList();
            showListView(false);
        }

    }

    public String getCurrentSearchText() {
        return currentSearchText;
    }

    public void setCurrentSearchText(String currentSearchText) {
        this.currentSearchText = currentSearchText;
    }

    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public void showSearchMembersInfo(String searchString, String type) {
        MentionListAdapter mentionListAdapter = (MentionListAdapter) searchMemberListView.getAdapter();
        mentionListAdapter.setSearchedMembersList(
                searchMemberModel.getUserSearchByName(getRoomIds(), searchString, selectedMemberHashMap, type));
    }

    public void removeAllMemberList() {
        MentionListAdapter mentionListAdapter = (MentionListAdapter) searchMemberListView.getAdapter();
        mentionListAdapter.clearMembersList();
    }

    public void showListView(boolean isShow) {

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

    }

    public List<SearchedItemVO> getMembersList() {
        MentionListAdapter mentionListAdapter = (MentionListAdapter) searchMemberListView.getAdapter();
        return mentionListAdapter.getSearchedMembersList();
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
            Pattern p = Pattern.compile("(?:@)(.+)(?:\\!\\^\\$)(\\d+)(?:\\!\\^\\$)");
            Matcher matcher = p.matcher(rawMemberText);
            while (matcher.find()) {
                id = matcher.group(2);
                if (type == 0) {
                    LinkedHashMap<Integer, SearchedItemVO> searchedItemLinkedHashMap =
                            searchMemberModel.getSearchedItemlinkedHashMap();
                    Iterator i = searchedItemLinkedHashMap.keySet().iterator();
                    selectedMemberHashMap.put(new Integer(id),
                            searchedItemLinkedHashMap.get(new Integer(id)));
                } else if (type == 1) {
                    selectedMemberHashMap.remove(Integer.valueOf(id));
                }
            }
        }
    }

    public void convertMentionedMemberText(SearchedItemVO searchedItemVO, String currentSearchText) {
        int selectionIndex = editText.getSelectionStart();
        int startIndex = selectionIndex - currentSearchText.length();
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getEditableText());
        MensionSpannable spannable = new MensionSpannable(editText.getContext(), searchedItemVO.getName());
        String convertedText = searchedItemVO.getName() + "!^$" + searchedItemVO.getId() + "!^$";
        restoreOrDeleteSelectedMentionMemberInfo(0, "@" + convertedText);
        ssb.replace(startIndex, selectionIndex, convertedText);
        ssb.setSpan(spannable, startIndex - 1, startIndex + convertedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setText(ssb, TextView.BufferType.SPANNABLE);
        editText.setSelection(startIndex + convertedText.length());
    }

    public String getConvertedMessage() {

        String message = editText.getText().toString();
        StringBuilder builder = new StringBuilder(message);

        Iterator iterator = selectedMemberHashMap.keySet().iterator();

        if (iterator.hasNext()) {
            resultMentions = new ArrayList<>();
        }

        while (iterator.hasNext()) {
            int key = (Integer) iterator.next();
            StringBuilder memberInfoStringSB = new StringBuilder();
            SearchedItemVO searchedItemVO = selectedMemberHashMap.get(Integer.valueOf(key));
            String name = searchedItemVO.getName();
            String id = String.valueOf(key);
            String type = searchedItemVO.getType();
            memberInfoStringSB.append(name);
            memberInfoStringSB.append("!^$");
            memberInfoStringSB.append(id);
            memberInfoStringSB.append("!^$");
            int startIndexOfMemberString = builder.indexOf(memberInfoStringSB.toString());
            builder.replace(startIndexOfMemberString,
                    startIndexOfMemberString + memberInfoStringSB.length(), name);

            int offset = startIndexOfMemberString - 1;
            int length = name.length() + 1;

            Log.e(name + "offset", offset + "");
            Log.e(name + "length", length + "");

            ReqMention mentionInfo = new ReqMention(key, type, offset, length);

            resultMentions.add(mentionInfo);
        }

        return builder.toString();
    }

    public boolean hasMentionMember() {
        if (selectedMemberHashMap.size() > 0) {
            return true;
        }
        return false;
    }

    public List<ReqMention> getResultMentions() {
        List<ReqMention> result = new ArrayList<>();
        result.addAll(resultMentions);
        return result;
    }

    public void clear() {
        selectedMemberHashMap.clear();
        resultMentions.clear();
        addTextWatcher();
    }
}
