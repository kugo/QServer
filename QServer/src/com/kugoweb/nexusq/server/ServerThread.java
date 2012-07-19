package com.kugoweb.nexusq.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class ServerThread extends Thread {

	private static final String TAG = "Q_SERVER";

	private final Socket mClient;
	private final int mCounter;
	private final Handler mHandler;
	private final Context mContext;

	/**
	 * @param client
	 * @param counter
	 * @param handler
	 * @param context
	 */
	public ServerThread(final Socket client, final int counter,
			final Handler handler, final Context context) {
		this.mClient = client;
		this.mCounter = counter;
		this.mHandler = handler;
		this.mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, this.getClass().getSimpleName() + " " + this.mCounter
				+ "#run");

		try {
			// リクエストの解析
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					this.mClient.getInputStream()));
			String s;
			while ((s = br.readLine()) != null) {
				Log.d(TAG, this.getClass().getSimpleName() + " "
						+ this.mCounter + "#run " + s);
				if (s.startsWith("GET ")) {
					// GETの行を適当に解析
					final StringTokenizer st = new StringTokenizer(s, " ");
					st.nextToken();
					final String path = st.nextToken();
					// リクエストに応じて適当に処理
					final String result = this.doRequest(path);
					// 結果出力
					final int code = result != null ? 200 : 404;
					final PrintStream ps = new PrintStream(
							this.mClient.getOutputStream());
					ps.println("HTTP/1.0 " + code + " OK");
					ps.println("MIME_version:1.0");
					ps.println("Content_Type:text/html");
					if (code == 200) {
						ps.println("Content_Length:" + result.length());
					} else {
						ps.println("Content_Length:" + 0);
					}
					ps.println("");
					ps.print(result);
					ps.flush();
					ps.close();
					break;
				}
			}
			br.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * リクエストに応じていろいろ処理する
	 * 
	 * @param path
	 * @return
	 */
	private String doRequest(final String path) {
		if ("/".equals(path)) {
			Log.d(TAG, this.getClass().getSimpleName() + " " + this.mCounter
					+ "#doRequest root");
			return this.doRootRequest();
		} else if (path.startsWith("/launcher?")) {
			Log.d(TAG, this.getClass().getSimpleName() + " " + this.mCounter
					+ "#doRequest launcher");
			return this.launchApp(path);
		} else {
			Log.d(TAG, this.getClass().getSimpleName() + " " + this.mCounter
					+ "#doRequest other");
			return null;
		}
	}

	/**
	 * pathで指定されたアクティビティを起動する
	 * 
	 * @param path
	 * @return
	 */
	private String launchApp(final String path) {
		final Uri uri = Uri.parse(path);
		final String pkg = uri.getQueryParameter("pkg");
		final String name = uri.getQueryParameter("name");
		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(pkg, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.mHandler.post(new Runnable() {

			@Override
			public void run() {
				ServerThread.this.mContext.startActivity(intent);
			}
		});

		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		final PackageManager pm = this.mContext.getPackageManager();
		final ResolveInfo info = pm.resolveActivity(intent, 0);
		sb.append("Launched <b>" + info.loadLabel(pm) + "</b><br />");
		sb.append("<a href=\"/\">Home</a>");
		sb.append("</body></html>");

		return sb.toString();
	}

	/**
	 * トップページの処理。とりあえずアプリ一覧を表示する
	 * 
	 * @return
	 */
	private String doRootRequest() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<b>Action Main and Category Launcher</b><br />");
		final Intent condition = new Intent(Intent.ACTION_MAIN);
		condition.addCategory(Intent.CATEGORY_LAUNCHER);
		final PackageManager pm = this.mContext.getPackageManager();
		final List<ResolveInfo> infos = pm.queryIntentActivities(condition, 0);
		final int size = infos.size();
		for (int i = 0; i < size; i++) {
			final ResolveInfo info = infos.get(i);
			final String label = info.loadLabel(pm).toString();
			final String pkg = info.activityInfo.packageName;
			final String name = info.activityInfo.name;
			sb.append("<a href=\"/launcher?pkg=" + pkg + "&name=" + name
					+ "\">" + label + "</a><br />");
			Log.d(TAG, info.loadLabel(pm).toString());
		}
		sb.append("</body></html>");

		return sb.toString();
	}
}
