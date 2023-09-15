package com.iimm.miliao.view.mucChatHolder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.map.MapHelper;
import com.iimm.miliao.ui.map.MapActivity;
import com.iimm.miliao.ui.tool.WebViewActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.view.RoundView;

import org.json.JSONObject;

import static com.iimm.miliao.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * 链接与地图Holder
 */
class LocationViewHolder extends AChatHolderInterface {
    RoundView ivAddress;
    TextView tvAddress;

    double mLatitude;
    double mLongitude;

    String mCurrtUrl;

    public LocationViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_location : R.layout.chat_to_item_location;
    }

    @Override
    public void initView(View view) {
        ivAddress = view.findViewById(R.id.chat_address_image);
        tvAddress = view.findViewById(R.id.chat_address_tv);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        ivAddress.setRadius(new float[]{12.0f, 12.0f, 12.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f});

        if (message.getType() == Constants.TYPE_LOCATION) {
            // 加载地图缩略图与位置
            // 直接展示消息里带的地址，也就是发送方地图截图上传的url,
            AvatarHelper.getInstance().displayMapUrl(message.getContent(), ivAddress);
            tvAddress.setText(message.getObjectId());
            if (TextUtils.isEmpty(message.getLocation_x())) {
                message.setLocation_x("0");
            }
            if (TextUtils.isEmpty(message.getLocation_y())) {
                message.setLocation_y("0");
            }
            MapHelper.LatLng latLng = new MapHelper.LatLng(Double.valueOf(message.getLocation_x()), Double.valueOf(message.getLocation_y()));
            mLatitude = latLng.getLatitude();
            mLongitude = latLng.getLongitude();
        } else {
            fillLinkData(message.getContent());
        }

        if (enableFire() && message.getIsReadDel() && isMysend) {

            ReadDelManager.getInstants().addReadMsg(message, this);
        }

    }

    @Override
    protected void onRootClick(View v) {
        sendReadMessage(mdata);
        ivUnRead.setVisibility(View.GONE);
        setRead();
        if (mdata.getType() == Constants.TYPE_LOCATION) {
            Intent intent = new Intent(mContext, MapActivity.class);
            intent.putExtra("latitude", mLatitude);
            intent.putExtra("longitude", mLongitude);
            intent.putExtra("address", mdata.getObjectId());
            mContext.startActivity(intent);
            if (enableFire() && mdata.getIsReadDel() && !isMysend) {

                ReadDelManager.getInstants().addReadMsg(mdata, this);
            }
        } else {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(EXTRA_URL, mCurrtUrl);
            mContext.startActivity(intent);
            if (enableFire() && mdata.getIsReadDel() && !isMysend) {

                ReadDelManager.getInstants().addReadMsg(mdata, this);
            }
        }
    }

    private void fillLinkData(String content) {
        try {
            JSONObject json = new JSONObject(content);
            String tile = json.getString("title");
            mCurrtUrl = json.getString("url");
            String img = json.getString("img");
            tvAddress.setText("[" + InternationalizationHelper.getString("JXLink") + "]" + " " + tile);
            AvatarHelper.getInstance().displayUrl(img, ivAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean enableUnRead() {
        return true;
    }


    @Override
    public boolean enableFire() {
        return true;
    }
}
