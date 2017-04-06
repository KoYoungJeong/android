package com.tosslab.jandi.app.ui.album.videoalbum.dagger;


import com.tosslab.jandi.app.ui.album.videoalbum.VideoAlbumFragment;

import dagger.Component;

@Component(modules = VideoAlbumFragModule.class)
public interface VideoAlbumFragComponent {
    void inject(VideoAlbumFragment fragment);
}
