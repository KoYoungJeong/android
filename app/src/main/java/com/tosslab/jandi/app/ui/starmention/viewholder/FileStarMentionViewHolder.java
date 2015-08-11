package com.tosslab.jandi.app.ui.starmention.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

/**
 * Created by tee on 15. 7. 29..
 */
public class FileStarMentionViewHolder extends CommonStarMentionViewHolder {

    private TextView starMentionFileNameView;
    private ImageView starFileTypeView;

    public FileStarMentionViewHolder(View itemView) {
        super(itemView);
        starMentionFileNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
        starFileTypeView = (ImageView) itemView.findViewById(R.id.iv_star_file);
    }

    public TextView getStarMentionFileNameView() {
        return starMentionFileNameView;
    }

    public ImageView getStarFileTypeView() {
        return starFileTypeView;
    }

    @Override
    public String toString() {
        return "FileStarMentionViewHolder{" +
                ", starMentionFileName=" + starMentionFileNameView +
                '}';
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        this.getStarMentionFileNameView().setText(starMentionVO.getFileName());
        this.getStarFileTypeView().setImageResource(starMentionVO.getImageResource());
    }
}
