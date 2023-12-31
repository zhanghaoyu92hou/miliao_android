package com.iimm.miliao.ui.dialog;

import android.app.Activity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.dialog.base.BaseDialog;

/**
 * Created by Administrator on 2016/4/21.
 */
public class ChangeRoomNameDialogView extends BaseDialog {

    private TextView mTitleTv;
    private AutoCompleteTextView mContentET;
    private Button mCommitBtn;
    private View.OnClickListener mOnClickListener;

    {
        RID = R.layout.dialog_single_input_name;
    }

    public ChangeRoomNameDialogView(Activity activity) {
        mActivity = activity;
        initView();
    }

    public ChangeRoomNameDialogView(Activity activity, View.OnClickListener onClickListener) {
        mActivity = activity;
        initView();
        mOnClickListener = onClickListener;
    }

    // User In RoomInfoActivity Modify Group Name.Desc
    public ChangeRoomNameDialogView(Activity activity, String title, String hint, int maxLines, int lines, View.OnClickListener onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, maxLines, lines);
        this.mOnClickListener = onClickListener;
    }

    public ChangeRoomNameDialogView(Activity activity, String title, String hint, int maxLines, int lines, InputFilter[] i, View.OnClickListener onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, maxLines, lines, i);
        this.mOnClickListener = onClickListener;
    }

    protected void initView() {
        super.initView();
        mTitleTv = (TextView) mView.findViewById(R.id.title);
        mTitleTv.setText(InternationalizationHelper.getString("JXNewRoomVC_CreatRoom"));
        mView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mView.findViewById(R.id.colse_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mContentET = (AutoCompleteTextView) mView.findViewById(R.id.content);

        mCommitBtn = (Button) mView.findViewById(R.id.sure_btn);
//        mCommitBtn.setBackgroundColor(SkinUtils.getSkin(mActivity).getAccentColor());
    }

    private void setView(String title, String hint, int maxLines, int lines) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setText(title);
        }
        if (!TextUtils.isEmpty(hint)) {
            mContentET.setHint(hint);
        }

        mContentET.setFilters(new InputFilter[]{DialogHelper.mExpressionFilter, DialogHelper.mChineseEnglishNumberFilter});

        mCommitBtn.setOnClickListener(v -> {
            mDialog.dismiss();
            if (mOnClickListener != null)
                mOnClickListener.onClick(mContentET);
        });
    }

    private void setView(String title, String hint, int maxLines, int lines, InputFilter[] i) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setText(title);
        }
        if (!TextUtils.isEmpty(hint)) {
            mContentET.setHint(hint);
        }
        if (i != null) {
            mContentET.setFilters(i);
        }

        mCommitBtn.setOnClickListener(v -> {
            mDialog.dismiss();
            if (mOnClickListener != null)
                mOnClickListener.onClick(mContentET);
        });
    }

    public View getmView() {
        return mView;
    }

    public void setSureClick(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setHint(String hint) {
        mContentET.setHint(hint);
    }

    public void setLines(int lines) {
        mContentET.setLines(lines);
    }

    public void setMaxLines(int maxLines) {
        mContentET.setMaxLines(maxLines);
    }

    public void setFilters(InputFilter[] i) {
        mContentET.setFilters(i);
    }

    public String getContent() {
        return mContentET.getText().toString();
    }
}
