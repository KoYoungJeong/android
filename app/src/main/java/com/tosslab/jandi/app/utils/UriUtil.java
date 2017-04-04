package com.tosslab.jandi.app.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

/**
 * Created by tonyjs on 15. 12. 8..
 */
public class UriUtil {

    /**
     * http scheme for URIs
     */
    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";
    /**
     * File scheme for URIs
     */
    public static final String LOCAL_FILE_SCHEME = "file";
    /**
     * Content URI scheme for URIs
     */
    public static final String LOCAL_CONTENT_SCHEME = "content";
    /**
     * Asset scheme for URIs
     */
    public static final String LOCAL_ASSET_SCHEME = "asset";
    /**
     * Resource scheme for URIs
     */
    public static final String LOCAL_RESOURCE_SCHEME = "res";
    /**
     * Data scheme for URIs
     */
    public static final String DATA_SCHEME = "data";
    /**
     * URI prefix (including scheme) for contact photos
     */
    private static final String LOCAL_CONTACT_IMAGE_PREFIX =
            Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "display_photo").getPath();

    public static Uri getFileUri(String filePath) {
        Uri uri = Uri.parse(filePath);

        if (!isLocalFileUri(uri)) {
            uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_FILE)
                    .path(filePath)
                    .build();
        }
        return uri;
    }

    public static Uri getContentUri(int imageId) {
        return Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
    }

    public static Uri getContentUriForVideo(int videoId) {
        return Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(videoId));
    }

    /**
     * /**
     * Check if uri represents network resource
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "http" or "https"
     */
    public static boolean isNetworkUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return HTTPS_SCHEME.equals(scheme) || HTTP_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local file
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "file"
     */
    public static boolean isLocalFileUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_FILE_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local content
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "content"
     */
    public static boolean isLocalContentUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_CONTENT_SCHEME.equals(scheme);
    }

    /**
     * Checks if the given URI is a general Contact URI, and not a specific display photo.
     *
     * @param uri the URI to check
     * @return true if the uri is a Contact URI, and is not already specifying a display photo.
     */
    public static boolean isLocalContactUri(Uri uri) {
        return isLocalContentUri(uri)
                && ContactsContract.AUTHORITY.equals(uri.getAuthority())
                && !uri.getPath().startsWith(LOCAL_CONTACT_IMAGE_PREFIX);
    }

    /**
     * Checks if the given URI is for a photo from the device's local media store.
     *
     * @param uri the URI to check
     * @return true if the URI points to a media store photo
     */
    public static boolean isLocalCameraUri(Uri uri) {
        String uriString = uri.toString();
        return uriString.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                || uriString.startsWith(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }

    /**
     * Check if uri represents local asset
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "asset"
     */
    public static boolean isLocalAssetUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_ASSET_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local resource
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to {@link #LOCAL_RESOURCE_SCHEME}
     */
    public static boolean isLocalResourceUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_RESOURCE_SCHEME.equals(scheme);
    }

    /**
     * Check if the uri is a data uri
     */
    public static boolean isDataUri(@Nullable Uri uri) {
        return DATA_SCHEME.equals(getSchemeOrNull(uri));
    }

    /**
     * @param uri uri to extract scheme from, possibly null
     * @return null if uri is null, result of uri.getScheme() otherwise
     */
    @Nullable
    public static String getSchemeOrNull(@Nullable Uri uri) {
        return uri == null ? null : uri.getScheme();
    }

    /**
     * A wrapper around {@link Uri#parse} that returns null if the input is null.
     *
     * @param uriAsString the uri as a string
     * @return the parsed Uri or null if the input was null
     */
    public static Uri parseUriOrNull(@Nullable String uriAsString) {
        return uriAsString != null ? Uri.parse(uriAsString) : null;
    }

}
