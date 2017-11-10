package cloudtoo.blelib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.blelibrary.service.BluetoothLeService;

public class BaseActivity extends Activity {
	private BluetoothLeService mBluetoothLeService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {  
	            Window window = getWindow();  
	            // Translucent status bar  
	            window.setFlags(  
	                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,  
	                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  
	            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	         //   getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
	        } 
		 else
		 {
			 requestWindowFeature(Window.FEATURE_NO_TITLE);
			 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 }
	}
	private void Ble_BindeService()
	{
		Intent gattServiceIntent = new Intent(BaseActivity.this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	private void Ble_unBindservice()
	{
		unbindService(mServiceConnection);
	}
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};
	public BluetoothLeService getBleService()
	{
		return mBluetoothLeService;
	}
	 @Override
	    protected void onResume() {
	        super.onResume();
		    Ble_BindeService();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
			Ble_unBindservice();
	    }

	    @Override
	    public boolean dispatchTouchEvent(MotionEvent event) {
	        //注：回调 3
	        return super.dispatchTouchEvent(event);
	    }
	
}