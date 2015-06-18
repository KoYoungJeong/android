package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.FileUploadProgressEvent;
import com.tosslab.jandi.app.events.files.FileUploadStartEvent;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EBean
public class FileUploadStateViewModel {

    @ViewById(R.id.rv_message_upload_file)
    RecyclerView rvUploadFile;

    @ViewById(R.id.vg_message_upload_file)
    View vgContentWrapper;

    @ViewById(R.id.loading_message_upload_file)
    WebLoadingBar webLoadingBar;

    @RootContext
    Context context;

    private int entityId;

    @AfterViews
    void initViews() {
        webLoadingBar.setColor(context.getResources().getColor(R.color.jandi_accent_color));
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(FileUploadStartEvent event) {
        if (event.getEntity() != entityId) {
            return;
        }

        rvUploadFile.getAdapter().notifyDataSetChanged();

        webLoadingBar.setVisibility(View.VISIBLE);
        webLoadingBar.setMax(100);
        webLoadingBar.setProgress(0);

    }

    public void onEventMainThread(FileUploadProgressEvent event) {

        if (event.getEntity() != entityId) {
            return;
        }

        rvUploadFile.getAdapter().notifyDataSetChanged();

        webLoadingBar.setMax(100);
        webLoadingBar.setProgress(event.getProgressPercent());

    }

    public void onEventMainThread(FileUploadFinishEvent event) {

        FileUploadDTO fileUploadDTO = event.getFileUploadDTO();
        if (fileUploadDTO.getEntity() != entityId) {
            return;
        }

        FileUploadInfoAdapter adapter = (FileUploadInfoAdapter) rvUploadFile.getAdapter();
        if (fileUploadDTO.getUploadState() == FileUploadDTO.UploadState.SUCCESS) {
            adapter.remove(fileUploadDTO);
        }

        if (adapter.getItemCount() > 0) {
            adapter.notifyDataSetChanged();
        } else {
            vgContentWrapper.setVisibility(View.GONE);
        }

        webLoadingBar.setMax(100);
        webLoadingBar.setProgress(100);
        webLoadingBar.setVisibility(View.GONE);

    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void initDownloadState() {
        FileUploadManager instance = FileUploadManager.getInstance(context);
        List<FileUploadDTO> uploadInfos = instance.getUploadInfos(entityId);

        if (uploadInfos.size() <= 0) {
            vgContentWrapper.setVisibility(View.GONE);
            return;
        }

        vgContentWrapper.setVisibility(View.VISIBLE);

        rvUploadFile.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rvUploadFile.setAdapter(new FileUploadInfoAdapter(context, uploadInfos));
    }

    private static class FileUploadInfoAdapter extends RecyclerView.Adapter<FileUploadViewHolder> {
        private final Context context;
        private final List<FileUploadDTO> uploadInfos;

        public FileUploadInfoAdapter(Context context, List<FileUploadDTO> uploadInfos) {
            this.context = context;
            this.uploadInfos = uploadInfos;
        }


        @Override
        public FileUploadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_file_upload_state, parent, false);

            FileUploadViewHolder holder = new FileUploadViewHolder(view);

            holder.ivPhoto = (ImageView) view.findViewById(R.id.iv_item_message_file_upload_state_photo);
            holder.ivState = (ImageView) view.findViewById(R.id.iv_item_message_file_upload_state_state);

            return holder;
        }

        @Override
        public void onBindViewHolder(FileUploadViewHolder holder, int position) {
            FileUploadDTO item = uploadInfos.get(position);
            Glide.with(context)
                    .load(item.getFilePath())
                    .asBitmap()
                    .centerCrop()
                    .into(holder.ivPhoto);

            switch (item.getUploadState()) {
                case PROGRESS:
                    holder.ivState.setVisibility(View.GONE);
                    holder.ivState.setOnClickListener(null);
                    break;
                default:
                case IDLE:
                    holder.ivState.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                    holder.ivState.setVisibility(View.VISIBLE);
                    holder.ivState.setOnClickListener(null);
                    break;
                case SUCCESS:
                    holder.ivState.setVisibility(View.GONE);
                    holder.ivState.setOnClickListener(null);
                    break;
                case FAIL:
                    holder.ivState.setImageResource(R.drawable.jandi_upload_error);
                    holder.ivState.setVisibility(View.VISIBLE);
                    holder.ivState.setOnClickListener(view -> {
                        FileUploadManager.getInstance(context).retryAsFailed(item);
                        FileUploadInfoAdapter.this.notifyDataSetChanged();
                    });
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return uploadInfos.size();
        }

        public void remove(FileUploadDTO fileUploadDTO) {
            uploadInfos.remove(fileUploadDTO);
        }
    }

    private static class FileUploadViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageView ivState;

        public FileUploadViewHolder(View itemView) {
            super(itemView);
        }
    }
}
