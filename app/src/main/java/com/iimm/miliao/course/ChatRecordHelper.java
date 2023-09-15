package com.iimm.miliao.course;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.MessageUploadChatRecord;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.mucfile.DownManager;
import com.iimm.miliao.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 聊天消息录制类
 * liuxuan 大神
 */
public class ChatRecordHelper {
    private String TAG = "ChatRecordHelper";
    /*############### 状态 ###############*/
    public static final int STATE_UN_RECORD = 0;                // 未录制
    public static final int STATE_RECORDING = 1;                // 录制中
    public static final int STATE_PAUSE_RECORD = 2;             // 暂停录制
    public static final int STATE_WAITING_RECORD = 3;           // 录制完成
    public static final int STATE_RECORD_FAILED = 4;            // 录制失败
    private volatile static ChatRecordHelper instance;
    private int mState; // 当前状态
    private long mStartTime;

    /*############### 单例 ###############*/
    private ChatRecordHelper() {
        mState = STATE_UN_RECORD;
    }

    public static ChatRecordHelper instance() {
        if (instance == null) {
            synchronized (DownManager.class) {
                if (instance == null) {
                    instance = new ChatRecordHelper();
                }
            }
        }
        return instance;
    }

    public int getState() { // 对外暴露
        return mState;
    }

    public void iniText(TextView tvRecord, ChatMessage message) {
        if (mState == STATE_RECORDING && message.getTimeSend() < mStartTime) {
            tvRecord.setVisibility(View.GONE);
            return;
        }

        int rid;
        if (mState == STATE_UN_RECORD) {
            rid = R.drawable.recording;
            tvRecord.setText(InternationalizationHelper.getString("JX_StartRecording"));
        } else {
            rid = R.drawable.stoped;
            tvRecord.setText(InternationalizationHelper.getString("JX_StopRecording"));
        }
    }

    public void start(ChatMessage chat) {
        if (mState == STATE_UN_RECORD && chat != null) {
            mStartTime = chat.getTimeSend();
            mState = STATE_RECORDING;
        }
    }

    public void stop(ChatMessage chatMessage, String toUserId) {
        if (mState == STATE_RECORDING && chatMessage != null) {
            List<ChatMessage> chatMessages = ChatMessageDao.getInstance().getCourseChatMessage(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), toUserId,
                    mStartTime, chatMessage.getTimeSend(), 100);

            List<ChatMessage> mCourseChatMessage = new ArrayList<>();
            for (int i = 0; i < chatMessages.size(); i++) {
                if (chatMessages.get(i).isMySend() && (chatMessages.get(i).getType() == Constants.TYPE_TEXT
                        || chatMessages.get(i).getType() == Constants.TYPE_VOICE
                        || chatMessages.get(i).getType() == Constants.TYPE_IMAGE
                        || chatMessages.get(i).getType() == Constants.TYPE_VIDEO
                        || chatMessages.get(i).getType() == Constants.TYPE_IMAGE_TEXT
                        || chatMessages.get(i).getType() == Constants.TYPE_IMAGE_TEXT_MANY)) {// 只录制自己发送的文本消息
                    mCourseChatMessage.add(chatMessages.get(i));
                }
            }

            Log.e(TAG, "stop: size:" + mCourseChatMessage.size());
            Collections.reverse(mCourseChatMessage);// 将集合倒序
            upLoadChatList(mCourseChatMessage);
            mState = STATE_UN_RECORD;
        }
    }

    public void reset() {
        mState = STATE_UN_RECORD;
    }

    /**
     * @param chatMessages
     * @see com.iimm.miliao.ui.message.ChatActivity
     */
    private void upLoadChatList(List<ChatMessage> chatMessages) {
        if (chatMessages != null && chatMessages.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ChatMessage chat : chatMessages) {
                sb.append(chat.getPacketId());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            String chatIds = sb.toString();

            EventBus.getDefault().post(new MessageUploadChatRecord(chatIds));
        }
    }
}
