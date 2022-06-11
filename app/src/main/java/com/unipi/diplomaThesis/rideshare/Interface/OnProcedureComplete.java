package com.unipi.diplomaThesis.rideshare.Interface;


import com.google.android.gms.tasks.Task;

public interface OnProcedureComplete {
    void isComplete(Task<Void> task, boolean complete);
}
