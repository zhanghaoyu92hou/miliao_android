package com.xuan.xuanhttplibrary.okhttp.callback;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.UserCheckedActivity;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Administrator
 * @time 2017/3/30 0:45
 * @des ${TODO}
 */

public abstract class ListCallback<T> implements Callback {

    private static String TAG = "ListCallback";
    private Class<T> mClazz;
    private Handler mDelivery;

    public ListCallback(Class<T> clazz) {
        mClazz = clazz;
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public abstract void onResponse(ArrayResult<T> result);

    public abstract void onError(Call call, Exception e);

    @Override
    public void onFailure(Call call, IOException e) {
        Log.i(HttpUtils.TAG, "服务器请求失败" + e.getMessage());
        errorData(call, e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.code() == 200) {
            try {
                String body = response.body().string();
                Log.i(HttpUtils.TAG, "服务器数据包：" + body);
                ArrayResult<T> result = new ArrayResult<T>();
                JSONObject jsonObject = JSON.parseObject(body);

                result.setResultCode(jsonObject.getIntValue(Result.RESULT_CODE));
                result.setResultMsg(jsonObject.getString(Result.RESULT_MSG));

                String data = jsonObject.getString(Result.DATA);
                if (!TextUtils.isEmpty(data)) {
                    result.setData(JSON.parseArray(data, mClazz));
                }
                successData(result);
            } catch (Exception e) {
                Log.i(HttpUtils.TAG, "数据解析异常:" + e.getMessage());
                errorData(call, new Exception("数据解析异常"));
            }
        } else {
            Log.i(HttpUtils.TAG, "服务器请求异常");
            errorData(call, new Exception("服务器请求异常"));
        }
    }

    protected void successData(final ArrayResult<T> data) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (data.getResultCode() == 1030101 || data.getResultCode() == 1030102) {
                    Log.e(TAG, "run: STATUS_USER_TOKEN_OVERDUE  data.getResultCode():" + data.getResultCode());
                    // 缺少访问令牌 || 访问令牌过期或无效
                    MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_OVERDUE;
                    UserCheckedActivity.start(MyApplication.getContext());
                    return;
                }
                onResponse(data);
            }
        });
    }

    protected void errorData(final Call call, final Exception e) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                onError(call, e);
            }
        });
    }
}
