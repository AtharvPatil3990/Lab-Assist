package com.android.labassist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

public class NoNetworkException extends IOException {
    @NonNull
    @Override
    public String toString() {
        return "No internet connection available.";
    }

    @Nullable
    @Override
    public String getMessage() {
        return "No internet connection available.";
    }
}
