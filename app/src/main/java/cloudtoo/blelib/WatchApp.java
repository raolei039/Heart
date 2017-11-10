package cloudtoo.blelib;

import java.util.List;
import android.app.Application;
import android.content.Intent;

import com.blelibrary.service.BluetoothLeService;

public class WatchApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Intent intent = new Intent(this,BluetoothLeService.class);
		startService(intent);
	}
}
