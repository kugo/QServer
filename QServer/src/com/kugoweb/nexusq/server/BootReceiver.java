package com.kugoweb.nexusq.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			ServerService.startService(context);
		}
		// TODO Auto-generated method stub

	}

}