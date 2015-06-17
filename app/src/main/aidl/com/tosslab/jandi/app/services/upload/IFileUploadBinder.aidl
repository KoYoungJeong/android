// IFileUploadBinder.aidl
package com.tosslab.jandi.app.services.upload;

// Declare any non-default types here with import statements
interface IFileUploadBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String getUploadFiles(int entityId);

    void start();

}
