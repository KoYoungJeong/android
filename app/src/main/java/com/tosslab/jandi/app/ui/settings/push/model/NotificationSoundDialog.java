package com.tosslab.jandi.app.ui.settings.push.model;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

public class NotificationSoundDialog {


    public static void showNotificationSound(Context context, int savedIdx, OnNotificationSelected onNotificationSelected) {

        View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_sound, null);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_item_notification_sound);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        SoundsAdapter adapter = new SoundsAdapter(context, savedIdx);
        recyclerView.setAdapter(adapter);

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri[] soundUris = getSoundIds(context);

        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            ((SoundsAdapter) adapter1).selectedPosition = position;
            adapter.notifyDataSetChanged();

            playNotifcationSound(context, mediaPlayer, position, soundUris);

            if (onNotificationSelected != null) {
                onNotificationSelected.onNotificationSelected(position);
            }
        });

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(rootView)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setOnDismissListener(dialog -> {
                    Observable.just(1)
                            .observeOn(Schedulers.io())
                            .subscribe(integer -> {
                                mediaPlayer.release();
                            }, t -> {
                            });
                })
                .create();
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.height = context.getResources().getDisplayMetrics().heightPixels * 4 / 5;
        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
    }

    private static Uri[] getSoundIds(Context context) {
        String[] fileNames = context.getResources().getStringArray(R.array.jandi_notification_array_file);
        Uri[] soundIds = new Uri[fileNames.length];

        for (int idx = 0, count = fileNames.length; idx < count; idx++) {
            int rawId = context.getResources().getIdentifier(fileNames[idx], "raw", context.getPackageName());
            soundIds[idx] = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawId);
        }

        return soundIds;

    }

    private static void playNotifcationSound(Context context, MediaPlayer mediaPlayer, int position, Uri[] soundIds) {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (streamVolume == 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        }

        Uri notificationUri;
        if (position == 0) {
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            notificationUri = soundIds[position - 1];
        }


        Observable.just(notificationUri)
                .observeOn(Schedulers.io())
                .subscribe(soundId -> {
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(context, soundId);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, t -> {
                });

    }

    public interface OnNotificationSelected {
        void onNotificationSelected(int selectPosition);
    }

    static class SoundsAdapter extends RecyclerView.Adapter<SoundViewHolder> {

        private final Context context;
        private final String[] titles;
        private int selectedPosition;

        private OnRecyclerItemClickListener onRecyclerItemClickListener;

        public SoundsAdapter(Context context, int savedIdx) {
            this.context = context;
            titles = context.getResources().getStringArray(R.array.jandi_notification_array_text);
            this.selectedPosition = savedIdx;
        }

        @Override
        public SoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_notification_sound, parent, false);
            return new SoundViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SoundViewHolder holder, int position) {
            holder.tvTitle.setText(getItem(position));
            holder.ivSelected.setChecked(selectedPosition == position);

            holder.itemView.setOnClickListener(v -> {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener.onItemClick(holder.itemView, SoundsAdapter.this, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        private String getItem(int position) {
            return titles[position];
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
            this.onRecyclerItemClickListener = onRecyclerItemClickListener;
        }
    }

    static class SoundViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private RadioButton ivSelected;

        public SoundViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_item_notification_sound);
            ivSelected = (RadioButton) itemView.findViewById(R.id.iv_item_notification_sound);
        }
    }
}
