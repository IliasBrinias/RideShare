package com.unipi.diplomaThesis.rideshare.Interface;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;

public interface OnCompleteUserSave {
    void onComplete(@NonNull Task<Void> task);
}
