package com.iimm.miliao.db.dao;

import android.support.annotation.WorkerThread;

public interface OnCompleteListener2 {// User to FriendDao addAttentionUsers addRooms

    @WorkerThread
    void onLoading(int progressRate, int sum);

    @WorkerThread
    void onCompleted();

}
