package com.kugoweb.nexusq.server;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	private static final String TAG = "Q_SERVER";

	private String mMyIP;
	private NfcAdapter mNfcAdapter;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.mMyIP = getIpAddress(this);
		final TextView urlView = (TextView) this.findViewById(R.id.tv_url);
		urlView.setText("http://" + this.mMyIP + ":" + ServerService.PORT);

		ServerService.startService(this);

		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		this.mNfcAdapter.setNdefPushMessageCallback(this, this);
		this.mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(this.getIntent()
				.getAction())) {
			Log.d(TAG, "ACTION_NDEF_DISCOVERED");
		}
	}

	/**
	 * return the IP address as a String
	 * 
	 * @param context
	 * @return
	 */
	private static String getIpAddress(final Context context) {
		final WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		final WifiInfo info = wm.getConnectionInfo();
		final int ip = info.getIpAddress();
		final String strIp = ((ip >> 0) & 0xFF) + "." + ((ip >> 8) & 0xFF)
				+ "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
		return strIp;
	}

	@Override
	public NdefMessage createNdefMessage(final NfcEvent event) {
		Log.d(TAG, "createNdefMessage");
		final String url = "http://" + this.mMyIP + ":" + ServerService.PORT
				+ "/";
		final NdefRecord record = NdefRecord.createUri(url);
		final NdefRecord[] records = new NdefRecord[] { record };
		final NdefMessage msg = new NdefMessage(records);

		return msg;
	}

	@Override
	public void onNdefPushComplete(final NfcEvent event) {
		Log.d(TAG, "onNdefPushComplete");
	}
}
