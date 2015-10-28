package com.tosslab.jandi.app.ui.login.tutorial;

import android.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
@EFragment(R.layout.fragment_intro_tutorial)
public class IntroTutorialFragment extends Fragment {
    public static final int FIRST_PAGE = 0;
    public static final int SECOND_PAGE = 1;
    public static final int LAST_PAGE = 2;

    @FragmentArg
    int pageType;
    @ViewById(R.id.img_tutorial_head)
    ImageView imageViewTutorialHead;
    @ViewById(R.id.txt_tutorial_title)
    TextView textViewTutorialTitle;
    @ViewById(R.id.txt_tutorial_message)
    TextView textViewTutorialMessage;

    @ViewById(R.id.layout_tutorial_top)
    View topLayout;

    @ViewById(R.id.layout_tutorial_bottom)
    View bottomLayout;

    @AfterViews
    void initLayout() {
        int resImageHead;
        int resTitleText;
        int resMessageText;
        switch (pageType) {
            case FIRST_PAGE:
                resImageHead = R.drawable.tutorial_img_01;
                resTitleText = R.string.jandi_tutorial_welcome;
                resMessageText = R.string.jandi_tutorial_welcome_message;
                break;
            case SECOND_PAGE:
                resImageHead = R.drawable.tutorial_img_02;
                resTitleText = R.string.jandi_tutorial_topic;
                resMessageText = R.string.jandi_tutorial_topic_message;
                break;
            case LAST_PAGE:
            default:
                resImageHead = R.drawable.tutorial_img_03;
                resTitleText = R.string.jandi_tutorial_file;
                resMessageText = R.string.jandi_tutorial_file_message;
                break;
        }
        imageViewTutorialHead.setImageResource(resImageHead);
        textViewTutorialTitle.setText(resTitleText);
        textViewTutorialMessage.setText(resMessageText);

    }
}
