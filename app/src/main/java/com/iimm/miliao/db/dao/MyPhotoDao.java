package com.iimm.miliao.db.dao;

import android.os.Handler;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.bean.MyPhoto;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.util.ThreadManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 访问MyPhoto的Dao，实际上是一个工具类
 */
public class MyPhotoDao {
    private static MyPhotoDao instance = null;

    public static MyPhotoDao getInstance() {
        if (instance == null) {
            synchronized (MyPhotoDao.class) {
                if (instance == null) {
                    instance = new MyPhotoDao();
                }
            }
        }
        return instance;
    }

    public Dao<MyPhoto, String> dao;

    private MyPhotoDao() {
        try {
            dao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    MyPhoto.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    /**
     * 用户数据更新是，下载用户照片调用此方法。调用此方法更新所有照片（先清除，在插入）
     */
    public void addPhotos(final Handler handler, final String loginUserId, final List<MyPhoto> photos, final OnCompleteListener listener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                // 删除所有的照片
                DeleteBuilder<MyPhoto, String> builder = dao.deleteBuilder();
                try {
                    builder.where().eq("ownerId", loginUserId);
                    dao.delete(builder.prepare());
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                if (photos != null && photos.size() > 0) {
                    for (int i = 0; i < photos.size(); i++) {
                        MyPhoto myPhoto = photos.get(i);
                        if (myPhoto == null) {
                            continue;
                        }
                        myPhoto.setOwnerId(loginUserId);
                        try {
                            dao.createOrUpdate(myPhoto);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (handler != null && listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted();
                        }
                    });
                }
            }
        });
    }

    public void addPhotos(final String loginUserId, final List<MyPhoto> photos) {
        if (photos == null || photos.size() <= 0) {
            return;
        }
        for (int i = 0; i < photos.size(); i++) {
            MyPhoto myPhoto = photos.get(i);
            if (myPhoto == null) {
                continue;
            }
            myPhoto.setOwnerId(loginUserId);
            try {
                dao.createOrUpdate(myPhoto);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void deletePhoto(String photoId) {
        try {
            dao.deleteById(photoId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MyPhoto> getPhotos(String loginUserId) {
        try {
            return dao.queryForEq("ownerId", loginUserId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
