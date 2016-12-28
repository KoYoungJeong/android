package com.tosslab.jandi.app.ui.album.imagealbum.dagger;


import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumFragment;

import dagger.Component;

@Component(modules = ImageAlbumFragModule.class)
public interface ImageAlbumFragComponent {
    void inject(ImageAlbumFragment fragment);
}
