package cloudtoo.blelib;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class BleCMDActivity extends BaseActivity {
    private static String TAG = "BleCMDActivity";
    private ListView mListView;
    private ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String,Object>>();
    private SimpleAdapter mSimpleAdapter;
    private Ct_BleTestPresent mCt_BleTestPresent;

    public static final String ACTION_GATT_DATA_AVAILABLE = "com.watch.service.ACTION_DATA_AVAILABLE";

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_cmd);
        Ct_initView();
    }

    @Override
    public void onResume() {
        mCt_BleTestPresent = new Ct_BleTestPresent(BleCMDActivity.this);
        mCt_BleTestPresent.Ct_InitDate(listItem);
        super.onResume();

        IntentFilter intent = new IntentFilter();
        intent.addAction(ACTION_GATT_DATA_AVAILABLE);
        registerReceiver(mbroadRec, intent);
    }

    @Override
    public void onPause() {
        mCt_BleTestPresent.Ct_unBindService();
        mCt_BleTestPresent = null;
        super.onPause();

        unregisterReceiver(mbroadRec);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getBleService().real_close();
    }

    private BroadcastReceiver mbroadRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("test", "MainActivity action:" + action);
            if (action.equals(ACTION_GATT_DATA_AVAILABLE)) {
                byte[] data = intent.getByteArrayExtra("com.watch.service.EXTRA_DATA");
                String stringData = bytesToHex(data);
                Log.d(TAG, "receive data: " + stringData);
                switch (data[1]) {
                    case 0x52:
                        Log.d(TAG, "收到心跳");
                        // check data length
                        if (data[2] == 0x0F) {
                            // length correct
                            handleData(stringData, data);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void handleData(String stringData, byte[] data) {
        final Toast toast = Toast.makeText(this, "BPM :" + String.valueOf(data[8]), Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 800);
        String timeHex = byteToHex(data[7]) + byteToHex(data[6]) + byteToHex(data[5]) + byteToHex(data[4]);
        Log.d(TAG, "Time hex string :" + timeHex);
        long timestamp = Long.parseLong(timeHex, 16);
        Log.d(TAG, "UTC timestamp :" + timestamp);
        Date date = new Date(timestamp*1000L); // *1000 is to convert seconds to milliseconds
        String formattedDate = date.toString();
        Log.d(TAG, "UTC date :" + formattedDate);
        String tempString = "Origin data :" + stringData + "\n" +
                            "Watch date :" + formattedDate + "\n" +
                            "BPM :" + String.valueOf(data[8]) + "\n\n";

        wirteToFile(tempString);
    }

    private void wirteToFile(String tempString) {
        File file = new File("/mnt/sdcard", "BMP log.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{

            try {
                FileOutputStream stream = new FileOutputStream(file, true);
                stream.write(tempString.getBytes());
                stream.close();
            } catch (Exception e) {

            }
        }
    }

    // change byte array to string
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String byteToHex(byte bytes) {
        char[] hexChars = new char[2];
        int v = bytes & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }


    private void Ct_initView()
    {
        mListView=(ListView)findViewById(R.id.listView);
        mSimpleAdapter = new SimpleAdapter(this,listItem,//需要绑定的数据
                R.layout.listview_item_single,//每一行的布局//动态数组中的数据源的键对应到定义布局的View中//
                new String[] {"ItemName"},
                new int[] {R.id.ble_name}
        );
        mListView.setAdapter(mSimpleAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "click index :" + position);
                mCt_BleTestPresent.Ct_listItenOnclik(listItem,position);
            }
        });
    }
    public void btn_search_clicked(View v)
    {
        Log.d(TAG, "按下scan按钮，结束本页");
        finish();
    }
}
