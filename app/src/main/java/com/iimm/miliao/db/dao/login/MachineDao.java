package com.iimm.miliao.db.dao.login;

import android.util.Log;

import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class MachineDao {
    private static MachineDao instance = null;
    private Map<String, Machine> mMachines;

    public static MachineDao getInstance() {
        if (instance == null) {
            synchronized (MachineDao.class) {
                if (instance == null) {
                    instance = new MachineDao();
                }
            }
        }
        return instance;
    }

    private MachineDao() {
        mMachines = new HashMap<>();
    }

    private Machine geMachine(String machineName) {
        return mMachines.get(machineName);
    }

    public void loadMachineList(TimerListener timerListener) {
        for (String s : MyApplication.machine) {
            Machine machine = new Machine();
            machine.setMachineName(s);
            machine.setOnLine(false);
            machine.setSendReceipt(false);
            machine.setTimerListener(timerListener);
            mMachines.put(s, machine);
        }
    }

    public void resetMachineStatus() {
        for (String s : MyApplication.machine) {
            updateMachineOnLineStatus(s, false);
        }
    }

    // 更新设备在线状态
    public void updateMachineOnLineStatus(String machineName, boolean onLineStatus) {
        Machine machine = geMachine(machineName);
        if (machine != null) {
            machine.setOnLine(onLineStatus);
            if (onLineStatus) {
                //machine.resetTimer();
                machine.setSendReceipt(true);
            } else {
                //machine.stopTimer();
                machine.setSendReceipt(false);
            }
        }
        if (BuildConfig.DEBUG) {
            StringBuffer stringBuffer = new StringBuffer();
            if (mMachines != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mMachines.forEach((s, machine1) -> {
                        stringBuffer.append(s).append(":").append(machine1.isOnLine()).append("--");
                    });
                }else{
                    Set<String> keySet = mMachines.keySet();
                    for (String next : keySet) {
                        Machine machine1 = mMachines.get(next);
                        stringBuffer.append(next).append(":").append(machine1.isOnLine()).append("--");
                    }
                }
                Log.d("消息监听", "目前在线状态：" + stringBuffer.toString());
            }
        }
    }

    // 获取设备在线状态
    public boolean getMachineOnLineStatus(String machineName) {
        Machine machine = geMachine(machineName);
        if (machine != null) {
            return machine.isOnLine();
        }
        return false;
    }

    // 更新设备发送回执状态
    public void updateMachineSendReceiptStatus(String machineName, boolean sendReceiptStatus) {
        Machine machine = geMachine(machineName);
        if (machine != null) {
            machine.setSendReceipt(sendReceiptStatus);
        }
    }

    // 获取设备发送回执状态
    public boolean getMachineSendReceiptStatus(String machineName) {
        Machine machine = geMachine(machineName);
        if (machine != null) {
            return machine.isSendReceipt();
        }
        return false;
    }
}

