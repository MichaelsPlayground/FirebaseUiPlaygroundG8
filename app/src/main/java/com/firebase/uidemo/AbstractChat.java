package com.firebase.uidemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Common interface for chat messages, helps share code between RTDB and Firestore examples.
 */

/**
 * original code taken from the FirebaseUi sample app
 * https://github.com/firebase/FirebaseUI-Android/tree/master/app/src/main/java/com/firebase/uidemo/database
 */
public abstract class AbstractChat {

    @Nullable
    public abstract String getName();

    public abstract void setName(@Nullable String name);

    @Nullable
    public abstract String getMessage();

    public abstract void setMessage(@Nullable String message);

    @NonNull
    public abstract String getUid();

    public abstract void setUid(@NonNull String uid);

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract int hashCode();

}