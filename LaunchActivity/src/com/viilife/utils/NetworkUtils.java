package com.viilife.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkUtils {

	// {"code":"200","data":{"m_":"http:\/\/viilife.mydour.com\/avatar\/000\/00\/00\/m_d41d8cd98f00b204e9800998ecf8427e.png","orignal":"http:\/\/viilife.mydour.com\/avatar\/000\/00\/00\/d41d8cd98f00b204e9800998ecf8427e.png"}}
	public static void updateImg(final Context context, final String token,
			final File file, final String cb) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (true) {
					onPost(context, token, file, cb);
					return;
				}

				AndroidHttpClient client = AndroidHttpClient.newInstance(
						"viilife/android", context);
				ConnManagerParams.setTimeout(client.getParams(), 30000);
				try {
					String url = "http://api.viilife.com/upimg/avatar?app=wx&token="
							+ token;
					Log.i("viilife", url);
					HttpPost postRequest = new HttpPost(url);
					postRequest.addHeader("Content-Type",
							"application/x-www-form-urlencoded");
					MultipartEntityBuilder b = MultipartEntityBuilder.create();
					b.addBinaryBody("file", file);
					postRequest.setEntity(b.build());
					HttpConnectionParams.setConnectionTimeout(
							postRequest.getParams(), 45000);
					HttpConnectionParams.setSoTimeout(postRequest.getParams(),
							45000);
					HttpResponse rsp = client.execute(postRequest);
					if (rsp.getStatusLine().getStatusCode() == 200) {
						String path = "";
						String josn = EntityUtils.toString(rsp.getEntity(),
								"utf-8");
						Log.i("viilife", josn);
						JSONObject json = new JSONObject(josn);
						JSONObject data = json.optJSONObject("data");
						if (!JSONObject.NULL.equals(data)) {
							path = data.optString("m_") + "?_t="
									+ System.currentTimeMillis();
						}
						Intent i = new Intent("ACTION_UPLOAD_IMG");
						i.putExtra("upload", true);
						i.putExtra("url", path);
						i.putExtra("callback", cb);
						LocalBroadcastManager.getInstance(context)
								.sendBroadcast(i);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	private static void onPost(final Context context, final String token,
			final File file, final String cb) {
		try {
			String url_ = "http://api.viilife.com/upimg/avatar?app=wx&token="
					+ token;
			URL url = new URL(url_);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(45000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(true);

			connection.setRequestMethod("POST");

			// 设置请求头内容
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			DataOutputStream dos = new DataOutputStream(
					connection.getOutputStream());
			FileInputStream fin = new FileInputStream(file);

			int bytesAvailable, bufferSize, bytesRead;
			int maxBufferSize = 1 * 1024 * 512;

			bytesAvailable = fin.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			bytesRead = fin.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fin.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fin.read(buffer, 0, bufferSize);
			}

			int responseCode = connection.getResponseCode();
			Log.i("viilife", "responseCode = " + responseCode);
			if (responseCode == 200) {
				InputStream is = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024 * 8];
				int size = 0;
				while ((size = is.read(buf)) >= 1) {
					baos.write(buf, 0, size);
				}
				byte[] data = baos.toByteArray();
				String json_ = new String(data);

				is.close();
				baos.close();
				Log.i("viilife", json_);
				String path = "";
				JSONObject json = new JSONObject(json_);
				JSONObject d = json.optJSONObject("data");
				if (!JSONObject.NULL.equals(d)) {
					path = d.optString("m_") + "?_t="
							+ System.currentTimeMillis();
				}
				Intent i = new Intent("ACTION_UPLOAD_IMG");
				i.putExtra("upload", true);
				i.putExtra("url", path);
				i.putExtra("callback", cb);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}
			if (dos != null) {
				dos.flush();
				dos.close();
			}
			if (fin != null) {
				fin.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
