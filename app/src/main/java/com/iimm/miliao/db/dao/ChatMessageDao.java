package com.iimm.miliao.db.dao;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.MsgRoamTask;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.SQLiteHelper;
import com.iimm.miliao.db.SQLiteRawUtil;
import com.iimm.miliao.db.UnlimitDaoManager;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DES;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.log.LogUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.iimm.miliao.db.InternationalizationHelper.getString;

public class ChatMessageDao {
    private String TAG = "ChatMessageDao";
    private static ChatMessageDao instance = null;
    private SQLiteHelper mHelper;
    private Map<String, Dao<ChatMessage, Integer>> mDaoMap;

    private ChatMessageDao() {
        mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
        mDaoMap = new HashMap<String, Dao<ChatMessage, Integer>>();
    }

    public static ChatMessageDao getInstance() {
        if (instance == null) {
            synchronized (ChatMessageDao.class) {
                if (instance == null) {
                    instance = new ChatMessageDao();
                }
            }
        }
        return instance;
    }

    /**
     * 根据不同的消息类型，返回相应的重发次数
     */
    public static int fillReCount(int type) {
        int recount = 0;
        if (type < 100) {// 重发两次
            recount = 5;
        }
        return recount;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    private Dao<ChatMessage, Integer> getDao(String ownerId, String friendId) {
        if (TextUtils.isEmpty(ownerId) || TextUtils.isEmpty(friendId)) {
            return null;
        }
        String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
        if (mDaoMap.containsKey(tableName)) {
            return mDaoMap.get(tableName);
        }
        Dao<ChatMessage, Integer> dao = null;
        try {
            DatabaseTableConfig<ChatMessage> config = DatabaseTableConfigUtil.fromClass(mHelper.getConnectionSource(), ChatMessage.class);
            config.setTableName(tableName);
            SQLiteRawUtil.createTableIfNotExist(mHelper.getWritableDatabase(), tableName, SQLiteRawUtil.getCreateChatMessageTableSql(tableName));
            dao = UnlimitDaoManager.createDao(mHelper.getConnectionSource(), config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dao != null)
            mDaoMap.put(tableName, dao);
        return dao;
    }

    /**
     * 保存一条新的聊天记录
     */
    public boolean saveNewSingleChatMessage(String ownerId, String friendId, ChatMessage message) {
        LogUtils.e(TAG, "saveNewSingleChatMessage 开始存消息");
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            LogUtils.e(TAG, "isNotChatVisibility");
            return false;
        }
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            LogUtils.e(TAG, "dao == nul");
            return false;
        }
        try {
            // 去除重复消息
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            LogUtils.e(TAG, message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                LogUtils.e(TAG, "重复消息");
                return false;// 重复消息
            }
            if (message != null && message.getType() == Constants.TYPE_TEXT) {
                if (TextUtils.isEmpty(message.getContent())) {
                    return false;// 空消息
                }
            }
            // 为群组消息且发送方为我的好友且备注了，修改fromUserName
            // 提示消息只需要更新Content字段
            if (message.getType() != Constants.TYPE_READ
                    && message.getType() != Constants.TYPE_TIP
                    && message.isGroup()) {
                String groupNameForGroupOwner = RoomMemberDao.getInstance().getRoomRemarkName(friendId, message.getFromUserId());
                if (!TextUtils.isEmpty(groupNameForGroupOwner)) {
                    message.setFromUserName(groupNameForGroupOwner);
                } else {
                    Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), message.getFromUserId());
                    if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                        message.setFromUserName(friend.getRemarkName());
                    }
                }
            }

            // 保存该条消息
            dao.create(message);
            // 更新朋友表最后一次消息事件
            if (message.getType() != Constants.TYPE_READ) {// 已读消息不更新
                if (message.isGroup()) {// 群组
                    if (message.getType() == Constants.TYPE_TIP || TextUtils.isEmpty(message.getFromUserName())) {// 群组控制消息 || FromUserName为空
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, message.getContent(), message.getType(), message.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, message.getFromUserName() + " : " + message.getContent(), message.getType(), message.getTimeSend());
                    }
                } else {
                    String str;
                    if (message.getType() == Constants.TYPE_TEXT && message.getIsReadDel()) {
                        str = MyApplication.getContext().getString(R.string.tip_click_to_read);
                    } else {
                        str = message.getContent();
                    }
                    FriendDao.getInstance().updateFriendContent(ownerId, friendId, str, message.getType(), message.getTimeSend());
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            // Todo 问题描述：在群组内发消息其他人能收到，自己却收不到其他人的消息，且再次进入该群组聊天界面之前发的消息也消失了
            // Todo 问题分析：断点发现抛出了SQLException 基本都是本地不存在该消息表，但是对应的dao却还存在，导致了这个问题
            // Todo 问题解决：之前想从源头解决该问题，但该问题极难复现且不好分析，现抛出该异常时检查本地是否存在该消息表，如不存在重建该张消息表
            if (e != null && e.getCause() != null) {
                Log.e(TAG, e.getCause().getMessage());
            }
            String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
            if (!SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
                LogUtils.e(TAG, tableName + "不存在，重新创建");
                SQLiteRawUtil.createTableIfNotExist(mHelper.getWritableDatabase(), tableName, SQLiteRawUtil.getCreateChatMessageTableSql(tableName));
                saveNewSingleChatMessage(ownerId, friendId, message);// 将之前存失败的消息在存一遍
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveRoamingChatMessage(String ownerId, String friendId, ChatMessage message) {
        decryptDES(message);
        handlerRoamingSpecialMessage(message);

        LogUtils.e(TAG, "saveRoamingChatMessage 开始存消息");
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            Log.e(TAG, "isNotChatVisibility");
            return false;
        }
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            LogUtils.e(TAG, "saveRoamingChatMessage dao == nul");
            return false;
        }
        try {
            // 重复消息去除
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            Log.e(TAG, message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                LogUtils.e(TAG, "重复消息");
                return false;
            }
            if (message != null && message.getType() == Constants.TYPE_TEXT) {
                if (TextUtils.isEmpty(message.getContent())) {
                    return false;// 空消息
                }
            }
            // 保存这次的消息
            dao.create(message);
            LogUtils.e(TAG, "更新朋友表部分字段");
            // 同步更新content 漫游不更新content
            /*if (message.getType() != Constants.TYPE_READ) {// 已读消息不更新
                if (message.isGroup()) {// 群组
                    if (message.getType() == Constants.TYPE_TIP || TextUtils.isEmpty(message.getFromUserName())) {// 群组控制消息 || FromUserName为空
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, message.getContent(), message.getType(), message.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, message.getFromUserName() + " : " + message.getContent(), message.getType(), message.getTimeSend());
                    }
                } else {
                    String str;
                    if (message.getType() == Constants.TYPE_TEXT && message.getIsReadDel() == 1) {
                        str = "点击查看 T";
                    } else {
                        str = message.getContent();
                    }
                    FriendDao.getInstance().updateFriendContent(ownerId, friendId, str, message.getType(), message.getTimeSend());
                }
            }*/
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存 新的朋友 回话
     */
    public boolean saveNewSingleAnswerMessage(String ownerId, String friendId, ChatMessage message) {
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            return false;
        }

        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            // 重复消息去除
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                return false;// 重复消息
            }
            FriendDao.getInstance().updateFriendContent(ownerId, friendId,
                    message.getContent(), message.getType(), message.getTimeSend());
            // 保存这次的消息
            dao.create(message);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除某条消息
     */
    public boolean deleteSingleChatMessage(String ownerId, String friendId, ChatMessage message) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                dao.delete(chatMessages);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSingleChatMessage(String ownerId, String friendId, String packet) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", packet);
            if (chatMessages != null && chatMessages.size() > 0) {
                dao.delete(chatMessages);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除过期消息
    public boolean deleteOutTimeChatMessage(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages;
        try {
            builder.where().ne("deleteTime", -1)
                    .and().ne("deleteTime", 0)
                    .and().lt("deleteTime", TimeUtils.time_current_time());// deleteTime不等于 -1 || 0（-1、0为永久保存）并且deleteTime小于当前时间 需要删除
            messages = dao.query(builder.prepare());
            if (messages != null && messages.size() > 0) {
                dao.delete(messages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateExpiredStatus(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages;
        try {
            builder.where().ne("isExpired", 1)
                    .and().ne("deleteTime", -1)
                    .and().ne("deleteTime", 0)
                    .and().lt("deleteTime", TimeUtils.time_current_time());// deleteTime不等于 -1 || 0（-1、0为永久保存）并且deleteTime小于当前时间 需要删除
            messages = dao.query(builder.prepare());
            if (messages != null && messages.size() > 0) {
                Object[] msgIds = new Object[messages.size()];
                for (int i = 0; i < messages.size(); i++) {
                    msgIds[i] = messages.get(i).getPacketId();
                }
                UpdateBuilder<ChatMessage, Integer> builder2 = dao.updateBuilder();
                builder2.updateColumnValue("isExpired", 1);
                builder2.where().in("packetId", msgIds);
                dao.update(builder2.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * 更新已读状态
     *
     * @param ownerId
     * @param friendId
     * @param state
     */
    public void updateMessageRead(String ownerId, String friendId, String packetId, boolean state) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "更新已读失败:" + packetId);
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("sendRead", state);
            if (state) { // 改变这条已读的是时候 加一个消息容错
                builder.updateColumnValue("messageState", Constants.MESSAGE_SEND_SUCCESS);
            }
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "更新已读失败:" + packetId);
            e.printStackTrace();
        }
    }

    public void updateMessageRead(String ownerId, String friendId, ChatMessage chat) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            // builder.updateColumnValue("sendRead", true);
            builder.updateColumnValue("readPersons", chat.getReadPersons());
            builder.updateColumnValue("readTime", chat.getReadTime());
            builder.where().eq("packetId", chat.getPacketId());

            dao.update(builder.prepare());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户查看了阅后即焚消息
     */
    public boolean updateReadMessage(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null && message.getIsReadDel()) {
//                builder.updateColumnValue("content", MyApplication.getInstance().getString(R.string.tip_burn_message));
//                builder.updateColumnValue("type", Constants.TYPE_TIP);
//                builder.where().eq("packetId", packetId);
//                dao.update(builder.prepare());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 文本消息的阅后即焚剩余查看时间
     */
    public void updateMessageReadTime(String ownerId, String friendId, String packetId, long time) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("readTime", time);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本消息的阅后即焚剩余查看时间
     */
    public void updateMessageReadTime(String ownerId, String friendId, String packetId, long readTime, long deleteTime) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("readTime", readTime);
            builder.updateColumnValue("deleteTime", deleteTime);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新戳一戳状态
     */
    public void updateMessageShakeState(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", true);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户撤回了某条消息
     */
    public void updateMessageBack(String ownerId, String friendId, String packetId, String name) {
        // 更新message数据库
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null) {
                builder.updateColumnValue("content", name + " " + InternationalizationHelper.getString("JX_OtherWithdraw"));
                builder.updateColumnValue("type", Constants.TYPE_TIP);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * 红包、转账的领取状态
     * */
    public boolean updateChatMessageReceiptStatus(String ownerId, String friendId, String packetId) {
        try {
            Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
            UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null) {
                builder.updateColumnValue("fileSize", 2);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新消息发送状态OK
     */
    public void updateMessageSendState(String ownerId, String friendId, int msg_id, int messageState) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "updateMessageSendState Failed");
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("messageState", messageState);
            builder.updateColumnValue("timeReceive", TimeUtils.time_current_time());
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "updateMessageSendState SQLException");
            e.printStackTrace();
        }
    }

    /**
     * 更新消息上传进度OK
     */
    public void updateMessageUploadSchedule(String ownerId, String friendId, int msg_id, int uploadSchedule) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("uploadSchedule", uploadSchedule);
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息上传状态OK
     */
    public void updateMessageUploadState(String ownerId, String friendId, int msg_id, boolean isUpload, String url) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isUpload", isUpload);
            builder.updateColumnValue("content", url);
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息下载状态OK
     */
    public void updateMessageDownloadState(String ownerId, String friendId, int msg_id, boolean isDownload, String filePath) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", isDownload);
            builder.updateColumnValue("filePath", filePath);
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新 群聊确认消息 的状态(不添加新字段了，以isDownload字段来标志，true 群组已确认 false 未确认)
    public void updateGroupVerifyMessageStatus(String ownerId, String friendId, String packetId, boolean isDownload) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", isDownload);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMessageContent(String ownerId, String friendId, String packetId, String content) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("content", content);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * OK 取与某人的聊天记录
     *
     * @param time     小于此time
     * @param pageSize 查询几条数据
     * @return
     */
    public List<ChatMessage> getSingleChatMessages(String ownerId, String friendId, long time, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", Constants.TYPE_READ).and().lt("timeSend", time);
            builder.orderBy("timeSend", false);
            builder.orderBy("_id", false);
            builder.limit((long) pageSize);
            builder.offset(0L);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * @param ownerId
     * @param friendId
     * @param time     Search >= timeSend 's Messages
     * @return
     */
    public List<ChatMessage> searchMessagesByTime(String ownerId, String friendId, double time) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", Constants.TYPE_READ).and()
                    .ge("timeSend", time);
            builder.orderBy("timeSend", false);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<ChatMessage> getOneGroupChatMessages(String ownerId, String friendId, double time, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        // 取出该群组最后一条漫游任务
        MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(ownerId, friendId);

        List<ChatMessage> messages = new ArrayList<>();
        try {
            if (mLastMsgRoamTask == null) {
                builder.where().ne("type", Constants.TYPE_READ)
                        //.and().ne("isExpired", 1)
                        .and().lt("timeSend", time);
                builder.orderBy("timeSend", false);
                builder.orderBy("_id", false);
                builder.limit((long) pageSize);
                builder.offset(0L);
            } else {
                builder.where().ne("type", Constants.TYPE_READ)
                        //.and().ne("isExpired", 1)
                        .and().ge("timeSend", mLastMsgRoamTask.getEndTime())
                        .and().lt("timeSend", time);
                builder.orderBy("timeSend", false);
                builder.orderBy("_id", false);
                builder.limit((long) pageSize);
                builder.offset(0L);
            }
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 取出课程 录制的消息 大于等于 startTime(开始录制的那条消息) 小于等于 endTime
    public List<ChatMessage> getCourseChatMessage(String ownerId, String friendId, long startTime, long endTime, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", Constants.TYPE_READ)
                    .and().ge("timeSend", startTime)
                    .and().le("timeSend", endTime);
            builder.orderBy("timeSend", false);
            builder.orderBy("_id", false);
            builder.limit((long) pageSize);
            builder.offset(0L);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 获取该群组内userId发送的群组控制消息
    public List<ChatMessage> getAllVerifyMessage(String ownerId, String friendId, String userId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = null;
        try {
            builder.where().eq("type", Constants.TYPE_TIP)
                    .and().eq("fromUserId", userId);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void updateMessageState(String ownerId, String friendId, String packetId, int messageState) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("messageState", messageState);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
            LogUtils.e(TAG, "消息发送状态更新成功-->packetId：" + packetId + "，messageState:" + messageState);
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "消息发送状态更新失败-->packetId：" + packetId + "，messageState:" + messageState);
        }
    }

    public void updateMessageLocationXY(String ownerId, String friendId, String packetId, String location_x, String location_y) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("location_x", location_x);
            builder.updateColumnValue("location_y", location_y);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMessageLocationXY(ChatMessage newmsg, String userid) {
        if (newmsg.isMySend()) {
            updateMessageLocationXY(userid, newmsg.getToUserId(), newmsg.getPacketId(), newmsg.getLocation_x(), newmsg.getLocation_y());
        } else {
            updateMessageLocationXY(userid, newmsg.getFromUserId(), newmsg.getPacketId(), newmsg.getLocation_x(), newmsg.getLocation_y());
        }
    }

    public boolean hasSameMessage(String ownerId, String friendId, String packetId) {
        boolean exist;
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = null;
        try {
            builder.where().eq("packetId", packetId);
            messages = dao.query(builder.prepare());
            if (messages != null && messages.size() > 0) {
                exist = true;
            } else {
                exist = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            exist = false;
        }
        return exist;
    }

    /**
     * 读取一条数据
     * 用于确认消息已读的时候获取数据库中的某条消息，根据packetId来查找
     */
    public ChatMessage findMsgById(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        ChatMessage messages = null;
        try {
            if (!TextUtils.isEmpty(packetId)) {
                builder.where().eq("packetId", packetId);
            }
            messages = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 删除与某人的聊天消息表
     */
    public void deleteMessageTable(String ownerId, String friendId) {
        String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
        if (mDaoMap.containsKey(tableName)) {
            mDaoMap.remove(tableName);
        }
        if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
            SQLiteRawUtil.dropTable(mHelper.getWritableDatabase(), tableName);
        }
    }




    public void updateNickName(String ownerId, String friendId, String fromUserId, String newNickName) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.where().eq("fromUserId", fromUserId);
            builder.updateColumnValue("fromUserName", newNickName);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取最后一条聊天记录
    public ChatMessage getLastChatMessage(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);

        ChatMessage chatMessage;
        if (dao == null) {
            return null;
        }

        try {
            QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
            builder.orderBy("timeSend", false);
            chatMessage = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return chatMessage;
    }

    /**
     * 查询已读消息是否重复
     *
     * @param content 内容，也就是源数据的packetid;
     * @return true 有重复 false 无重复
     */
    public boolean checkRepeatRead(String ownerId, String friendId, String userId, String content) {
        boolean b = false;
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        List<ChatMessage> messages = null;
        try {
            builder.where().eq("type", Constants.TYPE_READ)
                    .and().eq("content", content)
                    .and().eq("fromUserId", userId);
            messages = builder.query();
            if (messages != null && messages.size() > 0) {
                b = true;
            } else {
                b = false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 查询某条消息的已读成员列表
     *
     * @param loginUserId
     * @param roomId
     * @param packetId
     */
    public List<ChatMessage> queryFriendsByReadList(String loginUserId, String roomId, String packetId, int pager) {
        Dao<ChatMessage, Integer> dao = getDao(loginUserId, roomId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("type", Constants.TYPE_READ).and().eq("content", packetId);
            builder.orderBy("timeSend", false);
            long k = (pager + 1) * 10;
            builder.limit(k);
            List<ChatMessage> list = builder.query();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询某个朋友的某一条聊天记录
     * 用于消息界面的查询历史聊天记录
     */
    public List<Friend> queryChatMessageByContent(Friend friend, String content) {
        String loginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();

        Dao<ChatMessage, Integer> dao = getDao(loginUserId, friend.getUserId());
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        try {
            builder.where().eq("type", "1").and().like("content", "%" + content + "%");
            builder.orderBy("timeSend", true);

            List<ChatMessage> query = builder.query();
            if (query == null) {
                return null;
            }

            List<Friend> friends = new ArrayList<>();
            for (int i = 0; i < query.size(); i++) {
                ChatMessage chatMessage = query.get(i);
                Friend temp = new Friend();
                temp.setUserId(friend.getUserId());
                // 用于显示群组头像，
                temp.setRoomId(friend.getRoomId());
                temp.setNickName(friend.getNickName());
                temp.setRoomFlag(friend.getRoomFlag());
                temp.setContent(chatMessage.getContent());
                temp.setTimeSend(chatMessage.getTimeSend());
                // Todo 2019.2.18  现改为double查询，更加精确，不添加新字段了，就放在ChatRecordTimeOut字段内
                temp.setChatRecordTimeOut(chatMessage.getDoubleTimeSend());
               //新修改
                if(friend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)){

                }else{
                    friends.add(temp);

                }
            }
            return friends;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取指定Type类型的消息 过滤阅后即焚消息
    public List<ChatMessage> queryChatMessageByType(String ownerId, String friendId, int type) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        List<ChatMessage> messages = new ArrayList<>();
        if (dao == null) {
            return messages;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("type", type).and().ne("isReadDel", 1);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 根据关键字查询消息
    public List<ChatMessage> queryChatMessageByContent(String ownerId, String friendId, String content) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("type", "1").and().like("content", "%" + content + "%");
            builder.orderBy("timeSend", true);
            return builder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void decryptDES(ChatMessage chatMessage) {
        int isEncrypt = chatMessage.getIsEncrypt();
        if (isEncrypt == 1) {
            try {
                String decryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                String decryptContent = DES.decryptDES(chatMessage.getContent(), decryptKey);
                // 为chatMessage重新设值
                chatMessage.setContent(decryptContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理处理tigase/getLastChatList 获取到的特殊消息
     */
    public String handlerGetLastSpecialMessage(int isRoom, int type, String loginUserId, String from, String fromUserName, String toUserName) {
        String text = "";
        if (type == Constants.TYPE_BACK) {
            if (TextUtils.equals(from, loginUserId)) {
                text = MyApplication.getContext().getString(R.string.you) + " " + getString("JX_OtherWithdraw");
            } else {
                text = fromUserName + " " + getString("JX_OtherWithdraw");
            }
        } else if (type == Constants.TYPE_83) {
            // 单聊群聊一样的处理，
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了别人的红包 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.red_received_self, toUserName);
            } else {
                // 别人领取了我的红包
                text = MyApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromUserName, MyApplication.getContext().getString(R.string.you));
            }
        }else if (type == Constants.TYPE_CLOUD_RECEIVE_RED) {
            // 单聊群聊一样的处理，
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了别人的红包 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.cloud_red_received_self, toUserName);
            } else {
                // 别人领取了我的红包
                text = MyApplication.getContext().getString(R.string.cloud_tip_receive_red_packet_place_holder, fromUserName, MyApplication.getContext().getString(R.string.you));
            }
        } else if (type == Constants.TYPE_RED_BACK) {
            text = MyApplication.getContext().getString(R.string.tip_red_back);
        } else if (type == Constants.TYPE_CLOUD_BACK_RED) {
            text = MyApplication.getContext().getString(R.string.tip_red_back);
        } else if (type == Constants.TYPE_TRANSFER_RECEIVE) {
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.transfer_received_self);
            } else {
                // 对方领取了我的转账
                text = MyApplication.getContext().getString(R.string.transfer_received);
            }
        }else if (type == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.transfer_received_self_micro);
            } else {
                // 对方领取了我的转账
                text = MyApplication.getContext().getString(R.string.transfer_received_micro);
            }
        }
        return text;
    }

    /**
     * 处理tigase/ 获取到的特殊消息
     *
     * @param chatMessage
     * @return
     */
    public void handlerRoamingSpecialMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == Constants.TYPE_83||chatMessage.getType() == Constants.TYPE_CLOUD_RECEIVE_RED) {
            // 已过滤掉了
        } else if (chatMessage.getType() == Constants.TYPE_RED_BACK) {
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_red_back));
        } else if (chatMessage.getType() == Constants.TYPE_CLOUD_BACK_RED) {
            chatMessage.setType(Constants.TYPE_TIP);
            chatMessage.setContent(MyApplication.getContext().getString(R.string.cloud_tip_red_back));
        } else if (chatMessage.getType() == Constants.TYPE_TRANSFER_RECEIVE) {
            chatMessage.setType(Constants.TYPE_TIP);
            if (TextUtils.equals(chatMessage.getFromUserId(), CoreManager.requireSelf(MyApplication.getInstance()).getUserId())) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received_self));
            } else {
                // 对方领取了我的转账
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received));
            }
        }else if (chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER_RECEIVE) {
            chatMessage.setType(Constants.TYPE_TIP);
            if (TextUtils.equals(chatMessage.getFromUserId(), CoreManager.requireSelf(MyApplication.getInstance()).getUserId())) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received_self_micro));
            } else {
                // 对方领取了我的转账
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received_micro));
            }
        }
    }

    // 查询某条消息之后的消息，
    // 该消息不存在就返回null,
    @Nullable
    public List<ChatMessage> searchFromMessage(Context ctx, String ownerId, String friendId, ChatMessage fromMessage) throws Exception {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        Objects.requireNonNull(dao);
        ChatMessage localFromMessage = dao.queryBuilder().where().ne("type", Constants.TYPE_READ)
                //.and().ne("isExpired", 1)
                .and().eq("packetId", fromMessage.getPacketId())
                .queryForFirst();
        if (localFromMessage == null) {
            return null;
        }
        return dao.queryBuilder()
                .orderBy("timeSend", true)
                .orderBy("_id", true)
                .where().ne("type", Constants.TYPE_READ)
                //.and().ne("isExpired", 1)
                // 时间大于等于查询出来的localFromMessage,
                .and().ge("timeSend", localFromMessage.getTimeSend())
                .query();
    }



    /**
     * @return 返回true表示这条漫游消息需要保存处理，false就无视该消息，
     */
    public boolean roamingMessageFilter(int type) {
        return (type < 100
                // 拉漫游的红包领取消息不处理，
                && type != Constants.TYPE_83)||
                type== Constants.TYPE_CHANGE_ROOM_NAME||
                type== Constants.TYPE_NEW_NOTICE||
                type== Constants.NEW_MEMBER;
    }


    /**
     * 查询某条消息（通过服务端）
     *
     */
    @Nullable
    public boolean deleteMessage(String ownerId, String friendId, String message) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message);
            if (chatMessages != null && chatMessages.size() > 0) {
                dao.delete(chatMessages);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
