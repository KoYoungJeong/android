package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.filter.IconFilterUtil;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EViewGroup(R.layout.item_searched_file)
public class SearchedFileItemView extends RelativeLayout {
    @ViewById(R.id.txt_searched_file_name)
    TextView textViewSearchedFileName;
    @ViewById(R.id.txt_searched_file_owner_name)
    TextView textViewSearchedFileOwnerName;
    @ViewById(R.id.txt_searched_file_type)
    TextView textViewSearchedFileType;
    @ViewById(R.id.txt_searched_file_date)
    TextView textViewSearchedFileDate;
    @ViewById(R.id.iv_searched_file_type)
    SimpleDraweeView imageViewSearchedFileType;
    @ViewById(R.id.img_searched_file_name_line_through)
    View imageViewLineThrough;

    @ViewById(R.id.txt_searched_file_type_comment)
    TextView commentTextView;
    @ViewById(R.id.img_searched_file_type_comment)
    ImageView commentImageView;

    Context mContext;

    public SearchedFileItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.FileMessage searchedFile) {
        ResMessages.FileContent content = searchedFile.content;

        String searchedFileName = content.title;
        textViewSearchedFileName.setText(searchedFileName);

        FormattedEntity entity = EntityManager.getInstance().getEntityById(searchedFile.writerId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY) {
            String searchedFileOwnerName = entity.getName();
            textViewSearchedFileOwnerName.setText(searchedFileOwnerName);
        } else {
            textViewSearchedFileOwnerName.setText("");
        }

        textViewSearchedFileType.setText(content.ext);

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.createTime);
        textViewSearchedFileDate.setText(searchedFileDate);

        String icon = content.icon;
        MimeTypeUtil.FilterType filterType = IconFilterUtil.getMimeType(icon);
        GenericDraweeHierarchy hierarchy = imageViewSearchedFileType.getHierarchy();
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
        if (filterType == MimeTypeUtil.FilterType.Image) {
            if (ImageUtil.hasImageUrl(content)) {
                hierarchy.setPlaceholderImage(R.drawable.file_icon_img);
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                imageViewSearchedFileType.setHierarchy(hierarchy);

                // 썸네일
                String thumbnailUrl =
                        ImageUtil.getThumbnailUrlOrOriginal(content, ImageUtil.Thumbnails.SMALL);

                ViewGroup.LayoutParams params = imageViewSearchedFileType.getLayoutParams();
                ResizeOptions resizeOptions = new ResizeOptions(params.width, params.height);

                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(thumbnailUrl))
                        .setResizeOptions(resizeOptions)
                        .setAutoRotateEnabled(true)
                        .build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(imageRequest)
                        .setOldController(imageViewSearchedFileType.getController())
                        .build();

                imageViewSearchedFileType.setController(controller);
            } else {
                imageViewSearchedFileType.setHierarchy(hierarchy);
                imageViewSearchedFileType.setImageURI(UriFactory.getResourceUri(R.drawable.file_icon_img));
            }
        } else {
            // 파일 타입에 해당하는 아이콘 연결
            imageViewSearchedFileType.setHierarchy(hierarchy);
            int mimeTypeResource = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, icon);
            imageViewSearchedFileType.setImageURI(UriFactory.getResourceUri(mimeTypeResource));
        }

        commentTextView.setText(String.valueOf(searchedFile.commentCount));

        if (searchedFile.commentCount > 0) {
            commentImageView.setVisibility(View.VISIBLE);
            commentTextView.setVisibility(View.VISIBLE);
        } else {
            commentImageView.setVisibility(View.INVISIBLE);
            commentTextView.setVisibility(View.INVISIBLE);
        }

        if (entity != null && entity.getUser() != null && TextUtils.equals(entity.getUser().status, "enabled")) {
            textViewSearchedFileOwnerName.setTextColor(getResources().getColor(R.color.jandi_file_search_item_owner_text));
            imageViewLineThrough.setVisibility(View.GONE);
        } else {
            textViewSearchedFileOwnerName.setTextColor(getResources().getColor(R.color.deactivate_text_color));
            imageViewLineThrough.setVisibility(View.VISIBLE);
        }
    }
}
