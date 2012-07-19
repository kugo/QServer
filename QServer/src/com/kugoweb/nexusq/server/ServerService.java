package com.kugoweb.nexusq.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ServerService extends Service {

	private static final String TAG = "Q_SERVER";
	static final int PORT = 8080;

	private Handler mHandler;

	/**
	 * start the web server service
	 * 
	 * @param context
	 */
	public static void startService(final Context context) {
		final Intent service = new Intent(context, ServerService.class);
		context.startService(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		Log.d(TAG, this.getClass().getSimpleName() + "#onStartCommand");
		this.mHandler = new Handler();
		new Thread(new Runnable() {

			@Override
			public void run() {
				ServerService.this.listen();
			}
		}).start();
		return START_STICKY;
	}

	/**
	 * start to listen requests from clients
	 */
	private void listen() {
		Log.d(TAG, this.getClass().getSimpleName() + "#listen 1");
		int counter = 0;
		try {
			final ServerSocket server = new ServerSocket(PORT);
			while (true) {
				Log.d(TAG, this.getClass().getSimpleName() + "#listen 2");
				final Socket client = server.accept();
				final ServerThread thred = new ServerThread(client, counter,
						this.mHandler, this);
				thred.start();
				counter++;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

}
