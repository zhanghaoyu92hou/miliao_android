package com.iimm.miliao.ui.message.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.ui.mucfile.DownManager;
import com.iimm.miliao.ui.mucfile.MucFileDetails;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.mucfile.bean.MucFileBean;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.iimm.miliao.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * 查找指定内容
 * 文件、链接、音乐、交易
 */
public class SearchDesignationContent extends BaseActivity {

    public static int TYPE_FILE = 0;
    public static int TYPE_LINK = 1;
    public static int TYPE_PAY = 2;
    private int mSearchType;
    private String mSearchObject;
    private RecyclerView mRecyclerView;
    private DesignationContentAdapter mDesignationContentAdapter;
    private List<ChatMessage> mChatMessage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_designation_content);
        mSearchType = getIntent().getIntExtra("search_type", TYPE_FILE);
        mSearchObject = getIntent().getStringExtra("search_objectId");

        if (mSearchType == TYPE_FILE) {
            List<ChatMessage> fileList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_FILE);
            mChatMessage.addAll(fileList);
        } else if (mSearchType == TYPE_LINK) {
            List<ChatMessage> linkList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_LINK);
            List<ChatMessage> shareLinkList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_SHARE_LINK);
            mChatMessage.addAll(linkList);
            mChatMessage.addAll(shareLinkList);
        } else if (mSearchType == TYPE_PAY) {
            List<ChatMessage> redCloudList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_CLOUD_RED);
            List<ChatMessage> redList1 = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_READ_EXCLUSIVE);
            List<ChatMessage> redList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_RED);
            List<ChatMessage> transferList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_TRANSFER);
            List<ChatMessage> transferCloudList = ChatMessageDao.getInstance().queryChatMessageByType(coreManager.getSelf().getUserId(), mSearchObject, Constants.TYPE_CLOUD_TRANSFER);
            mChatMessage.addAll(redCloudList);
            mChatMessage.addAll(redList1);
            mChatMessage.addAll(redList);
            mChatMessage.addAll(transferList);
            mChatMessage.addAll(transferCloudList);
        }
        // 根据timeSend排序
        Comparator<ChatMessage> comparator = (o1, o2) -> (int) (o2.getDoubleTimeSend() - o1.getDoubleTimeSend());
        Collections.sort(mChatMessage, comparator);

        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        if (mSearchType == TYPE_FILE) {
            tvTitle.setText(getString(R.string.s_file));
        } else if (mSearchType == TYPE_LINK) {
            tvTitle.setText(getString(R.string.s_link));
        } else if (mSearchType == TYPE_PAY) {
            tvTitle.setText(getString(R.string.s_pay));
        }
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.s_dest_content_rcy);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDesignationContentAdapter = new DesignationContentAdapter(mChatMessage);
        mRecyclerView.setAdapter(mDesignationContentAdapter);
    }

    class DesignationContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ChatMessage> mChatMessageSource;

        public DesignationContentAdapter(List<ChatMessage> chatMessages) {
            this.mChatMessageSource = chatMessages;
            if (mChatMessageSource == null) {
                mChatMessageSource = new ArrayList<>();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new DesignationContentHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_designation, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ChatMessage chatMessage = mChatMessageSource.get(i);
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), chatMessage.getFromUserId());
            String name = chatMessage.getFromUserName();
            if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                name = friend.getRemarkName();
            }

            AvatarHelper.getInstance().displayAvatar(name, chatMessage.getFromUserId(),
                    ((DesignationContentHolder) viewHolder).mAvatarIv, true);
            if (chatMessage.getType() == Constants.TYPE_TRANSFER) {
                ((DesignationContentHolder) viewHolder).mNameTv.setText(getString(R.string.start_transfer, name));
            } else if (chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER) {
                ((DesignationContentHolder) viewHolder).mNameTv.setText(getString(R.string.micro_start_transfer, name));
            } else {
                ((DesignationContentHolder) viewHolder).mNameTv.setText(name);
            }
            ((DesignationContentHolder) viewHolder).mDateTv.setText(TimeUtils.getFriendlyTimeDesc(mContext,
                    chatMessage.getTimeSend()));
            if (chatMessage.getType() == Constants.TYPE_FILE) {
                // 文件
                fillFileData(chatMessage, ((DesignationContentHolder) viewHolder).mAbstractLl, ((DesignationContentHolder) viewHolder).mAbstractLeftIv,
                        ((DesignationContentHolder) viewHolder).mAbstractTopTv, ((DesignationContentHolder) viewHolder).mAbstractBottomTv);
            } else if (chatMessage.getType() == Constants.TYPE_LINK || chatMessage.getType() == Constants.TYPE_SHARE_LINK) {
                // 链接
                fillLinkData(chatMessage, ((DesignationContentHolder) viewHolder).mAbstractLl, ((DesignationContentHolder) viewHolder).mAbstractLeftIv,
                        ((DesignationContentHolder) viewHolder).mAbstractTopTv, ((DesignationContentHolder) viewHolder).mAbstractBottomTv);
            } else if (chatMessage.getType() == Constants.TYPE_RED || chatMessage.getType() == Constants.TYPE_READ_EXCLUSIVE || chatMessage.getType() == Constants.TYPE_TRANSFER || chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER || chatMessage.getType() == Constants.TYPE_CLOUD_RED) {
                // 红包与转账
                fillRedTransferData(chatMessage, ((DesignationContentHolder) viewHolder).mAbstractLl, ((DesignationContentHolder) viewHolder).mAbstractLeftIv,
                        ((DesignationContentHolder) viewHolder).mAbstractTopTv, ((DesignationContentHolder) viewHolder).mAbstractBottomTv);
            }
        }

        @Override
        public int getItemCount() {
            return mChatMessageSource.size();
        }

        private void fillFileData(ChatMessage chatMessage, LinearLayout ll, ImageView iv, TextView topTv, TextView bottomTv) {
            String filePath = TextUtils.isEmpty(chatMessage.getFilePath()) ? chatMessage.getContent() : chatMessage.getFilePath();
            int index = filePath.lastIndexOf(".");
            String type = filePath.substring(index + 1).toLowerCase();
            int start = filePath.lastIndexOf("/");
            String fileName = filePath.substring(start + 1).toLowerCase();

            if (type.equals("png") || type.equals("jpg")) {
                Glide.with(mContext)
                        .load(filePath)
                        .error(R.drawable.image_download_fail_icon)
                        .into(iv);
            } else {
                AvatarHelper.getInstance().fillFileView(type, iv);
            }
            topTv.setText(fileName);
            bottomTv.setText(XfileUtils.fromatSize(chatMessage.getFileSize()));

            ll.setOnClickListener(v -> {
                MucFileBean mucFileBean = new MucFileBean();
                mucFileBean.setName(fileName);
                mucFileBean.setNickname(fileName);
                mucFileBean.setUrl(chatMessage.getContent());
                mucFileBean.setSize(chatMessage.getFileSize());
                mucFileBean.setState(DownManager.STATE_UNDOWNLOAD);
                mucFileBean.setType(XfileUtils.getFileType(type));
                Intent intent = new Intent(mContext, MucFileDetails.class);
                intent.putExtra("data", mucFileBean);
                startActivity(intent);
            });
        }

        private void fillLinkData(ChatMessage chatMessage, LinearLayout ll, ImageView iv, TextView topTv, TextView bottomTv) {
            if (chatMessage.getType() == Constants.TYPE_LINK) {
                // 普通链接
                try {
                    JSONObject json = new JSONObject(chatMessage.getContent());
                    String linkTitle = json.getString("title");
                    String linkImage = json.getString("img");
                    String linkAddress = json.getString("url");

                    Glide.with(mContext)
                            .load(linkImage)
                            .error(R.drawable.browser)
                            .into(iv);
                    topTv.setText(linkTitle);

                    ll.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra(EXTRA_URL, linkAddress);
                        mContext.startActivity(intent);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //  ShareSDK 分享进来的链接
                try {
                    JSONObject json = new JSONObject(chatMessage.getObjectId());
                    String appName = json.getString("appName");
                    String appIcon = json.getString("appIcon");
                    String title = json.getString("title");
                    String subTitle = json.getString("subTitle");
                    String imageUrl = json.getString("imageUrl");
                    String linkShareAddress = json.getString("url");
                    String linkShareDownAppAddress = json.getString("downloadUrl");

                    if (TextUtils.isEmpty(appIcon) && TextUtils.isEmpty(imageUrl)) {
                        iv.setImageResource(R.drawable.browser);
                    } else if (TextUtils.isEmpty(imageUrl)) {
                        AvatarHelper.getInstance().displayUrl(appIcon, iv);
                    } else {
                        AvatarHelper.getInstance().displayUrl(imageUrl, iv);
                    }
                    topTv.setText(title);
                    bottomTv.setText(subTitle);

                    ll.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL, linkShareAddress);
                        intent.putExtra(WebViewActivity.EXTRA_DOWNLOAD_URL, linkShareDownAppAddress);
                        mContext.startActivity(intent);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void fillRedTransferData(ChatMessage chatMessage, LinearLayout ll, ImageView iv, TextView topTv, TextView bottomTv) {
            if (chatMessage.getType() == Constants.TYPE_RED || chatMessage.getType() == Constants.TYPE_READ_EXCLUSIVE) {
                // 红包
                iv.setImageResource(R.drawable.ic_chat_hongbao);
                topTv.setText(chatMessage.getContent());
            } else if (chatMessage.getType() == Constants.TYPE_CLOUD_RED) {
                // 云红包
                iv.setImageResource(R.drawable.ic_chat_hongbao);
                topTv.setText(chatMessage.getContent());
            } else {
                // 转账
                iv.setImageResource(R.drawable.ic_tip_transfer_money);
                topTv.setText("￥ " + chatMessage.getContent());
                bottomTv.setText(chatMessage.getFilePath());
            }

            ll.setOnClickListener(v -> {
                Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mSearchObject);
                if (friend != null) {
                    Intent intent = new Intent();
                    if (friend.getRoomFlag() == 0) { // 个人
                        intent.setClass(mContext, ChatActivity.class);
                        intent.putExtra(ChatActivity.FRIEND, friend);
                    } else {
                        intent.setClass(mContext, MucChatActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                        intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                    }

                    intent.putExtra("isserch", true);
                    intent.putExtra("jilu_id", chatMessage.getDoubleTimeSend());
                    startActivity(intent);
                }
            });
        }
    }

    class DesignationContentHolder extends RecyclerView.ViewHolder {

        private CircleImageView mAvatarIv;
        private TextView mNameTv, mDateTv;

        private LinearLayout mAbstractLl;
        private ImageView mAbstractLeftIv;
        private TextView mAbstractTopTv, mAbstractBottomTv;

        public DesignationContentHolder(@NonNull View itemView) {
            super(itemView);
            mAvatarIv = itemView.findViewById(R.id.avatar_iv);
            mNameTv = itemView.findViewById(R.id.name_tv);
            mDateTv = itemView.findViewById(R.id.date_tv);
            mAbstractLl = itemView.findViewById(R.id.abstract_ll);
            mAbstractLeftIv = itemView.findViewById(R.id.abstract_left_iv);
            mAbstractTopTv = itemView.findViewById(R.id.abstract_top_tv);
            mAbstractBottomTv = itemView.findViewById(R.id.abstract_bottom_tv);
        }
    }
}
