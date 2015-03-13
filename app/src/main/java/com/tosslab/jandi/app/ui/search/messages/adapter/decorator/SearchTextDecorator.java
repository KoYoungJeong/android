package com.tosslab.jandi.app.ui.search.messages.adapter.decorator;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public abstract class SearchTextDecorator implements TextDecorator {

    private final TextDecorator textDecorator;

    public SearchTextDecorator(TextDecorator textDecorator) {

        this.textDecorator = textDecorator;
    }

    public abstract void appendText();

    @Override
    public void next() {

        if (textDecorator != null) {
            textDecorator.next();
        }
    }

    private static enum TextType {
        Writer, ContentType, ContentOwner, FileName, Content
    }

    public static enum ContentType {
        Text, File, Shared, Unshared, Comment
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        final Context context;
        SearchResultType searchResultType;
        TextType textType;
        String text;
        private ContentType contentType;

        private Builder(Context context) {
            this.context = context;
        }

        public Builder searchResultType(SearchResultType searchResultType) {
            this.searchResultType = searchResultType;
            return this;
        }

        public Builder writer() {
            textType = TextType.Writer;
            return this;
        }

        public Builder contentType(String contentType, String status) {

            if (TextUtils.equals(contentType, "file")) {
                if (TextUtils.equals(contentType, "shared")) {
                    this.contentType = ContentType.Shared;
                } else if (TextUtils.equals(contentType, "unsahred")) {
                    this.contentType = ContentType.Unshared;
                } else {
                    this.contentType = ContentType.File;
                }
            } else if (TextUtils.equals(contentType, "text")) {
                this.contentType = ContentType.Text;
            } else if (TextUtils.equals(contentType, "comment")) {
                this.contentType = ContentType.Comment;
            }

            textType = TextType.ContentType;
            return this;
        }

        public Builder contentOwner() {
            textType = TextType.ContentOwner;
            return this;
        }

        public Builder fileName() {
            textType = TextType.FileName;
            return this;
        }

        public Builder content() {
            textType = TextType.Content;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public SearchTextDecorator build(SearchTextDecorator searchTextDecorator) {

            SearchTextDecorator decorator;

            int textColor;

            switch (searchResultType) {
                case Previous:
                case Next:
                    textColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color_sub);
                    break;
                default:
                case Main:
                    textColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color);
                    break;
            }

            switch (textType) {
                case Writer:
                    BoldTextDecorator writerBoldTextDecorator = new BoldTextDecorator(searchTextDecorator);
                    writerBoldTextDecorator.setContext(context);
                    writerBoldTextDecorator.setText(text);
                    writerBoldTextDecorator.setTextColor(textColor);
                    decorator = writerBoldTextDecorator;
                    break;
                case ContentType:
                    ImageDecorator imageDecorator = new ImageDecorator(searchTextDecorator);
                    imageDecorator.setContentType(contentType);
                    imageDecorator.setContext(context);

                    decorator = imageDecorator;
                    break;
                case ContentOwner:
                    BoldTextDecorator ownerBoldTextDecorator = new BoldTextDecorator(searchTextDecorator);
                    ownerBoldTextDecorator.setContext(context);
                    ownerBoldTextDecorator.setText(text);
                    ownerBoldTextDecorator.setTextColor(textColor);
                    decorator = ownerBoldTextDecorator;
                    break;
                case FileName:
                    BoldTextDecorator fileNameBoldTextDecorator = new BoldTextDecorator(searchTextDecorator);
                    fileNameBoldTextDecorator.setContext(context);
                    fileNameBoldTextDecorator.setText(text);
                    fileNameBoldTextDecorator.setTextColor(textColor);
                    decorator = fileNameBoldTextDecorator;
                    break;
                default:
                case Content:
                    NormalTextDecorator contentTextDecorator = new NormalTextDecorator(searchTextDecorator);
                    contentTextDecorator.setContext(context);
                    contentTextDecorator.setText(text);
                    contentTextDecorator.setTextColor(textColor);
                    decorator = contentTextDecorator;
                    break;
            }

            return decorator;
        }

    }
}