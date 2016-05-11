package com.tosslab.jandi.app.utils.image.loader;

import android.net.Uri;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tonyjs on 16. 5. 9..
 */
public class ThrowIOExceptionStreamLoader<T> implements StreamModelLoader<T> {
    @Override
    public DataFetcher<InputStream> getResourceFetcher(final T model, int width, int height) {
        return new DataFetcher<InputStream>() {
            @Override
            public InputStream loadData(Priority priority) throws Exception {
                throw new IOException("fake");
            }

            @Override
            public void cleanup() {

            }

            @Override
            public String getId() {
                return model.toString();
            }

            @Override
            public void cancel() {

            }
        };
    }
}
