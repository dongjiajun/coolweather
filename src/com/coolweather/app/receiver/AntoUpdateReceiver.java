package com.coolweather.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AntoUpdateReceiver extends BroadcastReceiver {

	

	
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, AntoUpdateReceiver.class);
		context.startService(i);
	}

}
