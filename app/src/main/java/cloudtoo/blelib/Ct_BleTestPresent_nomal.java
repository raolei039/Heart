package cloudtoo.blelib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blelibrary.bleinterface.AsSynResultListener;
import com.blelibrary.bleinterface.SynMapDataResult;
import com.blelibrary.datahead.BaseResultMessage;
import com.blelibrary.datahead.BaseSettingMessage;
import com.blelibrary.datahead.BleProtocol;
import com.blelibrary.datahead.CalenEventData;
import com.blelibrary.datahead.Firbatteryinfo;
import com.blelibrary.datahead.GpsData;
import com.blelibrary.datahead.HeartData;
import com.blelibrary.datahead.NotiMessageOnOff;
import com.blelibrary.datahead.PersonMessage;
import com.blelibrary.datahead.PreSleepSetting;
import com.blelibrary.datahead.RemindData;
import com.blelibrary.datahead.Sedentariness;
import com.blelibrary.datahead.SleepData;
import com.blelibrary.datahead.SleepTotaldata;
import com.blelibrary.datahead.SportData;
import com.blelibrary.datahead.SportGoalData;
import com.blelibrary.service.BluetoothLeService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 16-7-19.
 */
public class Ct_BleTestPresent_nomal {
    private Context mContext;
    private String date="123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private int[] bleprotocol=new int[]
            {BleProtocol.BleCMD_getHareMessage,BleProtocol.BleCMD_getsn,BleProtocol.BleCMD_getPersonMessage,
                    BleProtocol.BleCMD_timeset,BleProtocol.BleCMD_restore,BleProtocol.BleCMD_firmupdata,BleProtocol.BleCMD_factory,
                    BleProtocol.BleCMD_bindmessage,BleProtocol.BleCMD_dialback,BleProtocol.BleCMD_sedentariness,BleProtocol.BleCMD_remind,
                    BleProtocol.BleCMD_noti,BleProtocol.BleCMD_contrl,BleProtocol.BleCMD_no_messagepush,BleProtocol.BleCMD_messagepush,
                    BleProtocol.BleCMD_incomingpush,BleProtocol.BleCMD_uint_set,
                    BleProtocol.BleCMD_weather,BleProtocol.BleCMD_musicname,BleProtocol.BleCMD_searchphone,BleProtocol.BleCMD_timezone,
                    BleProtocol.BleCMD_basesetting,BleProtocol.BleCMD_calenevent,
                    BleProtocol.BleCMD_entrymode,BleProtocol.BleCMD_sensorset,BleProtocol.BleCMD_tem_comtrl,BleProtocol.BleCMD_sport,
                    BleProtocol.BleCMD_sleep,BleProtocol.BleCMD_heart,BleProtocol.BleCMD_gps,BleProtocol.BleCMD_sportgoal,BleProtocol.BleCMD_sleepgoal,
                    BleProtocol.BleCMD_heartgoal,BleProtocol.BleCMD_gpsgoal,BleProtocol.BleCMD_uvgoal,BleProtocol.BleCMD_goal};

    private String[][] BLECMD_NAMELIST =new String[][]
            {
                    {"54","获取gps"},
                    {"55","删除gps"},
                    {"56","发送文件"},
                    {"0","获取SN号"},
                    {"50","写SN号"},
                    {"1","获取设备信息"},
                    {"4","设置时间"},
                    {"5","恢复出厂设置"},
                    {"2","设置个人信息"} ,
                    {"3","获取个人信息"},
                    {"51","进入升级模式"},
                    {"8","设置久坐提醒"},
                    {"9","获取久坐提醒"},
                    {"10","增加闹钟提醒"},
                    {"11","删除闹钟提醒"},
                    {"12","修改闹钟提醒"},
                    {"13","清空所有闹钟提醒"},
                    {"14","读取所有闹钟提醒"},
                    //  {"15","设置消息推送开关"}, {"16","获取消息推送开关"},
                    {"17","获取当前运动数据"},
                    {"18","获取历史运动数据"},
                    {"19","清除历史运动数据"},
                    {"20","获取睡眠数据"},
                    {"21","清除睡眠数据"},
                    {"22","获取目标值"},
                    {"23","设置目标值"},
                    {"38","获取预设睡眠"},
                    {"39","设置预设睡眠"},
                    {"40","进入睡眠"},
                    {"41","退出睡眠"},
                    {"42","获取日历事件"},
                    {"43","设置日历事件"},
                    {"44","来电提醒"},
                    {"45","停止来电提醒"},
                    {"46","未接来电提醒"},
                    {"47","非短信推送"},
                    {"48","短信推送"},
                    {"49","邮件推送"}
            };

    public Ct_BleTestPresent_nomal(Context m)
    {
        this.mContext=m;
        Ct_BindService();
    }
    private void Ct_BindService()
    {
       IntentFilter intent = new IntentFilter();
       intent.addAction(MainActivity.ACTION_GATT_CONNECTED);
       intent.addAction(MainActivity.ACTION_GATT_DISCONNECTED);
       for(int i=0;i<bleprotocol.length;i++)
       {
           intent.addAction(BleProtocol.ReceMessageHead+bleprotocol[i]);
       }
       intent.addAction(BleProtocol.ReceMessageHead+BleProtocol.Ble_Low_battery);
       intent.addAction(BleProtocol.ReceMessageHead+BleProtocol.Ble_memory_full);
       mContext.registerReceiver(mbroadRec, intent);
    }
    public void Ct_unBindService()
    {
       mContext.unregisterReceiver(mbroadRec);
    }
    private BluetoothLeService getBleService()
    {
       return ((BleCMDActivity)mContext).getBleService();
    }
    private BroadcastReceiver mbroadRec = new BroadcastReceiver() {
                @Override
            public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    Log.v("test", "action:" + action);
                    Ct_DestryProgress();
                    if(action.equals(MainActivity.ACTION_GATT_DISCONNECTED))
                    {
                        if(mContext!=null)
                       (((Activity) mContext)).finish();
                    }
                    else
                    {
                       Analysis_Result(intent);
                    }
            }
        };
        private void Analysis_Result(Intent intent)
        {
                String action = intent.getAction();
                //sos 消息，拨打电话
                if(action.equals(BleProtocol.ReceMessageHead+BleProtocol.BleCMD_tem_comtrl))
                {
                   Intent dial = new Intent(Intent.ACTION_CALL, Uri.parse("tel:10010"));
                   mContext.startActivity(dial);
                   return ;
                }
            //SDK v1.1.1 更新
                if(action.equals(BleProtocol.ReceMessageHead+BleProtocol.Ble_Low_battery))
                {//低电量
                    return ;
                }
                if(action.equals(BleProtocol.ReceMessageHead+BleProtocol.Ble_memory_full))
                {//内存满
                    return ;
                }
                Object d= intent.getSerializableExtra(BleProtocol.ReceMessage) ;
                if(d instanceof String)
                {
                    //if(action.equals(BleProtocol.ReceMessageHead+BleProtocol.BleCMD_getsn))
                    {
                        Ct_ShowAlert((String) d, 0);
                        return ;
                    }
                }
                else if(d instanceof BaseResultMessage)
                {
                    BaseResultMessage result =(BaseResultMessage)d;
                    if(result.result==0)
                    {
                            if(result.pincode!=null && result.pincode.length()>0)
                                    Ct_ShowAlert(((BaseResultMessage) d).pincode,0);
                            else
                                    Ct_ShowAlert("成功",0);
                            return ;
                    }
                }
                else if(d instanceof Firbatteryinfo)
                {
                    Firbatteryinfo result=(Firbatteryinfo)d;
                    // if(result.==0)
                    {
                            String show= "版本号："+result.firmware+"\n"+
                                    "电量："+result.battery_level+"\n"+
                                    "产品类型："+ result.product;
                            Ct_ShowAlert(show,0);
                            return ;
                    }
                }
                else if(d instanceof PersonMessage)
                {
                    PersonMessage result=(PersonMessage)d;
                    {
                        String show= "年龄："+result.age+"\n"+
                                "性别："+result.sex+"\n"+
                                "身高："+ result.height+"\n"+
                                "体重："+ result.weight;
                        Ct_ShowAlert(show,0);
                        return ;
                    }
                }
                else if(d instanceof SportData)
                {
                    {
                            SportData result=(SportData)d;
                            String show="步数："+result.steps+"\n"+
                                    "卡路里："+result.cal+"\n"+
                                    "距离："+result.dis+"\n"
                                    ;
                            Ct_ShowAlert(show,0);
                            return ;
                    }
                }
                else if(d instanceof List)//(action.equals(BleProtocol.ReceMessageHead+BleProtocol.BleCMD_sport))
                {
                    if(((List) d).size()>0)
                    {
                            Object data=((List) d).get(0);
                            if(data instanceof SportData)
                            {
                                    List<SportData> datalist= (List<SportData>)d ;
                                    if(datalist!=null) {
                                            String show = "";
                                            for (SportData result : datalist) {
                                                    show = "步数：" + result.steps + "\n" +
                                                            "卡路里：" + result.cal + "\n" +
                                                            "距离：" + result.dis + "\n"+
                                                            "时间："+ timeStamp2format(result.sport_timestamp  ) ;
                                            }
                                            Ct_ShowAlert(show, 0);
                                            return;
                                    }
                            }
                            else if(data instanceof SleepData)
                            {
                                    List<SleepData> datalist= (List<SleepData>)d ;
                                    if(datalist!=null) {
                                            String show = "";
                                            for (SleepData result : datalist) {
                                                    show = "时间：" + timeStamp2format(result.sleep_timestamp)  + "\n" +
                                                            "睡眠类型：" + result.sleep_type + "\n"
                                                    ;
                                            }
                                            Ct_ShowAlert(show, 0);
                                            return;
                                    }
                            }
                            else if(data instanceof SleepTotaldata)
                            {
                                //SDK v1.1.1 更新
                                List<SleepTotaldata> datalist= (List<SleepTotaldata>)d ;
                                if(datalist!=null) {
                                    String show = "";
                                    for (SleepTotaldata result : datalist) {
                                        show = "时间：" + timeStamp2format(result.time_stamp)  + "\n" +
                                                "清醒：" + result.awake + "\n"+
                                                "深睡：" + result.deepsleep + "\n"+
                                                "浅睡：" + result.lightsleep + "\n"
                                        ;
                                    }
                                    Ct_ShowAlert(show, 0);
                                    return;
                                }
                            }
                            else if(data instanceof CalenEventData)
                            {
                                    List<CalenEventData> datalist= (List<CalenEventData>)d ;
                                    if(datalist!=null) {
                                            String show = "";
                                            for (CalenEventData result : datalist) {
                                                    show = "时间：" + timeStamp2format(result.calenevent_time)  + "\n" +
                                                            "时间索引值：" + result.calenevent_index + "\n"
                                                    ;
                                            }
                                            Ct_ShowAlert(show, 0);
                                            return;
                                    }
                            }
                            else if(data instanceof RemindData)
                            {
                                List<RemindData> result=(List<RemindData>)d;
                                String show="";
                                for(RemindData m:result) {
                                    show += "重复日期：" + m.remind_repeat + "\n" +
                                            "提醒时间：" + m.remind_time / 60 + ":" + m.remind_time % 60 + "\n" +
                                            "提醒类型：" + m.remind_type+"\n";
                                }
                                Ct_ShowAlert(show,0);
                                return ;
                            }
                            else if(data instanceof GpsData)
                            {
                                List<GpsData> result=(List<GpsData>)d;
                                String show="";
                                for(GpsData m:result) {
                                    show += "gps 时间：" + timeStamp2format(m.gps_time_stamp) + "\n" +
                                            "gps 经度：" + m.gps_lat + "\n" +
                                            "gps 纬度：" + m.gps_lon+"\n";
                                }
                                Ct_ShowAlert(show,0);
                                return ;
                            }
                    }
                    else
                    {
                            Ct_ShowAlert("成功",0);
                            return ;
                    }
                }
                else if(d instanceof SportGoalData)
                {
                    {
                        SportGoalData result=(SportGoalData)d;
                        String show="目标步数："+result.Goal_steps+"\n"+
                                "目标卡路里："+result.Goal_cal+"\n"+
                                "目标距离："+result.Goal_dis+"\n"
                                ;
                        Ct_ShowAlert(show,0);
                        return ;
                    }
                }
                else if(d instanceof PreSleepSetting)
                {
                    {
                        PreSleepSetting result=(PreSleepSetting)d;
                        String show="周期："+result.week_set+"\n"+
                                "开始："+result.start_time/60+":"+result.start_time%60+"\n"+
                                "结束："+result.end_time/60+":"+result.end_time%60+"\n"
                                ;
                        Ct_ShowAlert(show,0);
                        return ;
                    }
                }
                Ct_ShowAlert("失败", 0);
        }

        public void Ct_InitDate(ArrayList<Map<String, Object>> listItem)
        {

                listItem.clear();
                for(int i=0;i< BLECMD_NAMELIST.length;i++)
                {
                        Map<String, Object> nameMap1 = new HashMap<String, Object>();
                        nameMap1.put("ItemName", (String)BLECMD_NAMELIST[i][1]);
                        //nameMap1.put("ItemAddress", adress);
                        //nameMap1.put("Itemrssi", Integer.toString(rssi));
                        listItem.add(nameMap1);
                }
        }
    public void Ct_listItenOnclik(ArrayList<Map<String, Object>>listItem,int position)
    {
            String cmdname = (String) listItem.get(position).get("ItemName");
            int cmd_index=0;
            for(int i=0;i<BLECMD_NAMELIST.length;i++)
            {
                    if(cmdname.equals(BLECMD_NAMELIST[i][1]))
                    {
                      cmd_index=Integer.parseInt(BLECMD_NAMELIST[i][0]);
                    }
            }
            Ct_ShowProgress("正在发送数据", 0);
            if (cmd_index == 0) {
                getBleService().BLEdata_GetSN();
            }
            else if(cmd_index == 1)
            {
                getBleService().BLEdata_Get_BatteryInfo();
            }
            else if(cmd_index == 2)
            {
                PersonMessage d= new PersonMessage((byte)0,(byte)23,170,60*100);
                getBleService().BLEdata_SetPersonMessage(d);
            }
            else if(cmd_index == 3)
            {
                getBleService().BLEdata_GetPersonMessage();
            }
            else if(cmd_index == 4)
            {
                Calendar calendar = Calendar.getInstance();
                getBleService().BLEdata_SetTime(calendar);
                //getBleService().BLEdata_SetTime();
            }
            else if(cmd_index == 5)
            {
                getBleService().BLEdata_Restore();
            }
            else if(cmd_index == 8)
            {
                Sedentariness d= new Sedentariness(0xff,(byte)0x1e,0x08*60,0x12*60,0x64);
                getBleService().BLEdata_Set_sedentariness((Object) d);
            }
            else if(cmd_index == 9)
            {
                    getBleService().BLEdata_Get_sedentariness();
            }
            else if(cmd_index == 10)
            {
                    RemindData d= new RemindData(0,0xff,0x00,(12*60+13),"");
                    getBleService().BLEdata_Add_remind(d);
            }
            else if(cmd_index == 11)
            {
                    RemindData d= new RemindData(0,0xff,0x00,(12*60+13),"");
                    getBleService().BLEdata_del_remind(d);
            }
            else if(cmd_index == 12)
            {
                    RemindData d= new RemindData(0,0xff,0x00,(13*60+14),"");
                    getBleService().BLEdata_modis_remind(d);
            }
            else if(cmd_index == 13)
            {
                    getBleService().BLEdata_del_allremind();
            }
            else if(cmd_index == 14)
            {
                    getBleService().BLEdata_Get_allremind();
            }
            else if(cmd_index == 15)
            {
                    getBleService().BLEdata_SetNotiOnOff(0xff);
            }
            else if(cmd_index == 16)
            {
                    getBleService().BLEdata_GetNotiOnOff();
            }
            else if(cmd_index == 17)
            {
                    getBleService().BLEdata_GetCurrSport();
            }
            else if(cmd_index == 18)
            {
                    getBleService().BLEdata_GetHistrySport();
            }
            else if(cmd_index == 19)
            {
                    getBleService().BLEdata_DelHistrySport();
            }
            else if(cmd_index == 20)
            {
                    getBleService().BLEdata_GetSleep();
            }
            else if(cmd_index == 21)
            {
                    getBleService().BLEdata_DelSleep();
            }
            else if(cmd_index == 22)
            {
                    getBleService().BLEdata_GetGoal();
            }else if(cmd_index == 23)
            {
                    SportGoalData send= new SportGoalData();
                    send.Goal_steps=8000;
                    send.Goal_dis=3000;
                    send.Goal_cal=350000;
                    getBleService().BLEdata_SetGoal(send);
            }
            else if(cmd_index == 38)
            {
                    getBleService().BLEdata_GetPreSleep();
            }
            else if(cmd_index == 39) {
                    PreSleepSetting send= new PreSleepSetting(0xff,22*60,9*60);
                    getBleService().BLEdata_SetPreSleep(send);
            }
            else if(cmd_index == 40)
            {
                    getBleService().BLEdata_EntrySleep();
            }
            else if(cmd_index == 41) {
                    getBleService().BLEdata_ExitSleep();
            }
            else if(cmd_index == 42)
            {
                    getBleService().BLEdata_GetCalenEvent();
            }
            else if(cmd_index == 43)
            {
                    List<CalenEventData> event = new ArrayList<CalenEventData>();
                    for(int i=0;i<1;i++)
                    {
                            long time= System.currentTimeMillis()/1000L+3*60*(i+1);
                            CalenEventData sing= new CalenEventData(i,time,"Test title-"+i);
                            event.add(sing);
                    }
                    getBleService().BLEdata_AddCalenEvent(event);
            }
            else if(cmd_index == 44)
            {
                    String num="10010";
                    getBleService().BLEdata_Incoming(num);
            }
            else if(cmd_index == 45)
            {
                    //String num="10010";
                    getBleService().BLEdata_StopIncoming();
            }
            else if(cmd_index == 46)
            {
                    getBleService().BLEdata_MissCallPushNum(1);
            }
            else if(cmd_index == 47)
            {
                    getBleService().BLEdata_SocPushNum(1);
            }
            else if(cmd_index == 48)
            {
                    getBleService().BLEdata_SmsPushNum(1);
            }
            else if(cmd_index == 49)
            {
                    getBleService().BLEdata_EmailPushNum(1);
            }
            else if(cmd_index == 50)
            {
                Ct_DestryProgress();
                Ct_entrySnEditScreen();
            }
            else if(cmd_index == 51)
            {
                getBleService().BLEdata_firmupdata();
            }
            else if(cmd_index == 54)
            {
                getBleService().BLEdata_getgpsdata(new AsSynResultListener()
                {
                    @Override
                    public void As_BleSynFail(int status) {

                    }

                    @Override
                    public void As_BleSynSucess(List<?> data) {
                        List<GpsData> result=(List<GpsData>)data;
                        String show="";
                        for(GpsData m:result) {
                            show += "gps 时间：" + timeStamp2format(m.gps_time_stamp) + "\n" +
                                    "gps 经度：" + m.gps_lat + "\n" +
                                    "gps 纬度：" + m.gps_lon+"\n";
                        }
                        if(result.size()==0)
                        {
                            show="no gps data";
                        }
                        Ct_DestryProgress();
                        Ct_ShowAlert(show,0);
                    }
                });
            }
            else if(cmd_index == 55)
            {
                getBleService().BLEdata_delgpsdata();
            }
            else if(cmd_index == 56)
            {
                //BLEdata_Maperasure(int adress,int len) 擦除数据
                //BLEdata_Maperasureall() //擦除全部数据
                getBleService().BLEdata_Initsenfile(new SynMapDataResult() {

                    @Override
                    public void BleSynResult(int adress, int datalen, int reslut) {
                        //状态值 reslut ==1：已经在升级状态
                        //       reslut ==2：  null值
                        //4 无升级的蓝牙服务
                        //5:未处在升级状态
                        //当为0 才能进行升级
                        if(reslut==0x10) {
                            getBleService().BLEdata_Senfile(0xffff, new byte[]{0, 0, 0, 0, 0});
                            //结束 退出升级模式
                            // getBleService().BLEdata_MapUpdataDone();
                        }
                    }
                });
            }

    }
        private void Ct_entrySnEditScreen()
        {
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.dialog_edit, null);
        final AlertDialog dlg = new AlertDialog.Builder(mContext)
                .setTitle("请输入三位数字或者字母")
                .setView(textEntryView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                                EditText secondPwd = (EditText) textEntryView.findViewById(R.id.sn_number);
                                TextView prj = (TextView) textEntryView.findViewById(R.id.proname);
                            Log.v("test","secondPwd.getText().toString():"+secondPwd.getText().toString().length());
                                if (secondPwd.getText().toString().length() == 3) {
                                        Calendar calendar = Calendar.getInstance();
                                        int i = calendar.get(Calendar.YEAR) - 2014;
                                        int week = calendar.get(Calendar.WEEK_OF_YEAR);

                                        int start = week / date.length();
                                        int sn_2 = week % date.length();
                                        String sn_date = date.substring(i - 1, i) + date.substring(start, start + 1) + date.substring(sn_2, sn_2 + 1);
                                        //获取当前时间为本周的第几天
                                        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                                        if (day == 0)
                                                sn_date = sn_date + "7";
                                        else
                                                sn_date = sn_date + Integer.valueOf(day);
                                        String SN = prj.getText().toString() + sn_date + secondPwd.getText().toString().toUpperCase();
                                    Log.v("test","SN:"+SN);
                                    Ct_ShowProgress("正在发送数据", 0);
                                        getBleService().BLEdata_WriteSN(SN);

                                }
                        }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
        })
                .create();
        dlg.show();
     }

        private ProgressDialog mdialog;
        private void Ct_ShowProgress(String message,int type)
        {
                if(mdialog==null)
                        mdialog = new ProgressDialog(mContext);
                //设置进度条风格，风格为圆形，旋转的
                mdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                //设置ProgressDialog 标题
                mdialog.setTitle("Cloudtoo");
                //设置ProgressDialog 提示信息
                mdialog.setMessage(message);
                //设置ProgressDialog 标题图标
                mdialog.setIcon(android.R.drawable.ic_dialog_map);
                //设置ProgressDialog 的一个Button
                if(type==1)
                        mdialog.setButton("确定", new ProgressDialog.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        mdialog.dismiss();
                                }
                        });
                //设置ProgressDialog 的进度条是否不明确
                mdialog.setIndeterminate(false);
                //设置ProgressDialog 是否可以按退回按键取消
                mdialog.setCancelable(true);
                //显示
                mdialog.show();
        }
        private void Ct_DestryProgress()
        {
                if(mdialog!=null)
                {
                        mdialog.dismiss();
                        mdialog=null;
                }
        }
        private void Ct_ShowAlert(String message,int type)
        {
                final Dialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle("收到数据：")
                        .setMessage(""+message)
                        .setPositiveButton("确定",new ProgressDialog.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                }
                        })
                        .create();
                dialog.show();
        }
        public static String timeStamp2format(long time_stamp){

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String reTime = df.format(time_stamp * 1000L);

                return reTime;
        }
}
