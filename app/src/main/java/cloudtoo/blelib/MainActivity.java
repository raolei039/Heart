package cloudtoo.blelib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.blelibrary.service.BluetoothLeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {
    private static String TAG = "MainActivity";
    private ListView mListView;
    private ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String,Object>>();
    private SimpleAdapter mSimpleAdapter;
    private boolean mScanning=false;
    private BluetoothAdapter mBluetoothAdapter;
    public static final String ACTION_GATT_CONNECTED = "com.watch.service.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.watch.service.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_DATA_AVAILABLE = "com.watch.service.ACTION_DATA_AVAILABLE";

    //private BluetoothLeService mBluetoothLeService;
    private Handler  mHandler= new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1000://update ble list
                    BluetoothDevice device =(BluetoothDevice)msg.obj;
                    int rssi=msg.arg1;
                    As_UpdataDeviceList(device, rssi);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        Ct_initView();
        Log.e("test", "onCreate onCreate");

        Intent intent = new Intent(this,BluetoothLeService.class);
        startService(intent);
      //  Ct_BindService();

    }

    @Override
    public void onResume() {
        Ct_BindService();
        super.onResume();
    }

    @Override
    public void onPause() {
        As_ScanLeDevice(false);
        Ct_unBindService();
        Ct_OndestryAlert();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private BroadcastReceiver mbroadRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("test", "MainActivity action:" + action);
            if(action.equals(ACTION_GATT_CONNECTED))
            {
                Log.d(TAG, "收到连线广播，准备帅新界面显示功能菜单");
                Intent mIntent = new Intent(MainActivity.this,BleCMDActivity.class);
               // mIntent.putExtra("ItemName",name);
               // mIntent.putExtra("ItemAddress",adr);
                startActivity(mIntent);
            }else if (action.equals(ACTION_GATT_DISCONNECTED)) {
                Log.d(TAG, "收到断线广播");
            }else if (action.equals(ACTION_GATT_DATA_AVAILABLE)) {
                Log.d(TAG, "收到心跳");
                byte[] data = intent.getByteArrayExtra("com.watch.service.EXTRA_DATA");
                Log.d(TAG, String.valueOf(data[0]));
                Log.d(TAG, String.valueOf(data[1]));
                Log.d(TAG, String.valueOf(data[2]));
            }
        }
    };

    private void Ct_BindService()
    {
        IntentFilter intent = new IntentFilter();
        intent.addAction(ACTION_GATT_DISCONNECTED);
        intent.addAction(ACTION_GATT_CONNECTED);
        intent.addAction(ACTION_GATT_DATA_AVAILABLE);
        registerReceiver(mbroadRec, intent);
    }

    private void Ct_unBindService()
    {
        unregisterReceiver(mbroadRec);
    }
    private void Ct_initView()
    {
        // get ble peripheral result show list.
        mListView=(ListView)findViewById(R.id.listView);
        mSimpleAdapter = new SimpleAdapter(this,listItem,//需要绑定的数据
                R.layout.listview_item,//每一行的布局//动态数组中的数据源的键对应到定义布局的View中//
                 new String[] {"ItemName"
                ,"ItemAddress", "Itemrssi"},
                 new int[] {R.id.ble_name,R.id.ble_address,R.id.ble_rssi});
        mListView.setAdapter(mSimpleAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ct_ShowProgress("正在连接", 0);
                String name = (String) listItem.get(position).get("ItemName");
                String adr = (String) listItem.get(position).get("ItemAddress");
               /* Intent mIntent = new Intent(MainActivity.this,BleCMDActivity.class);
                mIntent.putExtra("ItemName",name);
                mIntent.putExtra("ItemAddress",adr);
                startActivity(mIntent);*/
                getBleService().connect(adr);
            }
        });
    }
    private void As_UpdataDeviceList(final BluetoothDevice device,int rssi)
    {

        Map<String, Object> nameMap1 = new HashMap<String, Object>();
        Map<String, Object> nameMap2 = new HashMap<String, Object>();
        String adress =device.getAddress();
        String name=device.getName();
        int size = listItem.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            nameMap2 = listItem.get(i);
            if (nameMap2.get("ItemAddress").equals(adress)) {
                nameMap2.put("Itemrssi", Integer.toString(rssi));
                break;
            }
        }
        if (i == size) {
            nameMap1.put("ItemName", name);
            nameMap1.put("ItemAddress", adress);
            nameMap1.put("Itemrssi", Integer.toString(rssi));
            listItem.add(nameMap1);
        }
        if(listItem != null && listItem.size() > 0)
            mSimpleAdapter.notifyDataSetChanged();

    }

    public void btn_search_clicked(View v)
    {
        listItem.clear();;
        if(mScanning==false)
            As_ScanLeDevice(true);
    }

    public void As_ScanLeDevice(final boolean enable) {

        if (enable)
        {
            //cleanDeviceList();
            if(mScanning==true)
            {
                mBluetoothAdapter.stopLeScan(As_mLeScanCallback);
            }
            mScanning = true;
            mBluetoothAdapter.startLeScan(As_mLeScanCallback);
        }
        else
        {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(As_mLeScanCallback);

        }
    }
    private BluetoothAdapter.LeScanCallback As_mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @SuppressLint("NewApi")
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,byte[] scanRecord) {
            {
                Message message =new Message();
                message.what=1000;
                message.obj=device;
                message.arg1=rssi;
                mHandler.sendMessage(message);
            }

        }
    };


    private   Dialog mdialog;
    private void Ct_ShowAlert(String message,int type)
    {
        if(mdialog==null)
          mdialog = new AlertDialog.Builder(this)
                .setTitle("cloudtoo")
                .setMessage(""+message)
                .setPositiveButton("确定",new ProgressDialog.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        mdialog.show();
    }
    private void Ct_OndestryAlert()
    {
        if(mdialog!=null)
        {
            mdialog.dismiss();
            mdialog=null;
        }
        Ct_DestryProgress();
    }
    private ProgressDialog mProgressdialog;
    private void Ct_ShowProgress(String message,int type)
    {
        if(mProgressdialog==null)
            mProgressdialog = new ProgressDialog(this);
        //设置进度条风格，风格为圆形，旋转的
        mProgressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //设置ProgressDialog 标题
        mProgressdialog.setTitle("Cloudtoo");
        //设置ProgressDialog 提示信息
        mProgressdialog.setMessage(message);
        //设置ProgressDialog 标题图标
        mProgressdialog.setIcon(android.R.drawable.ic_dialog_map);
        //设置ProgressDialog 的一个Button
        if(type==1)
            mProgressdialog.setButton("确定", new ProgressDialog.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mdialog.dismiss();
                }
            });
        //设置ProgressDialog 的进度条是否不明确
        mProgressdialog.setIndeterminate(false);
        //设置ProgressDialog 是否可以按退回按键取消
        mProgressdialog.setCancelable(true);
        //显示
        mProgressdialog.show();
    }
    private void Ct_DestryProgress()
    {
        if(mProgressdialog!=null)
        {
            mProgressdialog.dismiss();
            mProgressdialog=null;
        }
    }
}
