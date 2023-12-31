package com.iimm.miliao.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/**
 * 修改部门名称
 */
public class ModifyDepartmentName extends BaseActivity implements View.OnClickListener {
    StructBeanNetInfo structBeanNetInfo;
    private EditText mCpyNemEdit;
    private String mDptNewName;
    private String mDepartmentId;
    private String mDepartmentName;
    private int departpostion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motify_dptname);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mDepartmentId = intent.getStringExtra("departmentId");
            mDepartmentName = intent.getStringExtra("departmentName");
            departpostion = intent.getIntExtra("departpostion", 0);
            structBeanNetInfo = (StructBeanNetInfo) intent.getExtras().getSerializable("data");
        } else {
            // ...
            finish();
        }
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.modify_department_name);
        mCpyNemEdit = (EditText) findViewById(R.id.department_edit);
        mCpyNemEdit.setText(mDepartmentName);
        findViewById(R.id.create_department_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.create_department_btn:
                mDptNewName = mCpyNemEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mDptNewName)) {
                    // 部门名不能为空
                    Toast.makeText(this, R.string.department_name_connot_null, Toast.LENGTH_SHORT).show();
                } else if (mDptNewName.equals(mDepartmentName)) {
                    Toast.makeText(this, R.string.department_name_connot_same, Toast.LENGTH_SHORT).show();
                } else {
                    createDepartment(mDptNewName, mDepartmentId);
                }
                break;
        }
    }

    private void createDepartment(String departmentName, String departmentId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("dpartmentName", departmentName);
        params.put("departmentId", departmentId);

        HttpUtils.get().url(coreManager.getConfig().MODIFY_DEPARTMENT_NAME)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(ModifyDepartmentName.this, R.string.modify_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            structBeanNetInfo.getDepartments().get(departpostion).setDepartName(departmentName);
                            EventBusCommpanyContorl commpanyContorl = EventBusCommpanyContorl.getInstance(structBeanNetInfo,
                                    EventBusCommpanyContorl.DEPARTMENT_UPDATENAME);
                            commpanyContorl.setmDepartPostion(departpostion);
                            EventBus.getDefault().post(commpanyContorl);
                            finish();
                        } else {
                            // 修改失败
                            Toast.makeText(ModifyDepartmentName.this, R.string.modify_fail, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ModifyDepartmentName.this);
                    }
                });
    }
}
