package com.viilife.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler INSTANCE;

	private SimpleDateFormat format;
	
	private CrashHandler() {
		format = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.getDefault());
	}

	public static CrashHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new CrashHandler();
		return INSTANCE;
	}

	public void init() {
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (Config.DEBUG) {
			String msg = getCrashInfoToFile(ex);
			Log.i("viilife", msg);
		}
		if (mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	private String getCrashInfoToFile(Throwable ex) {
		if (ex == null) {
			return null;
		}
		StringWriter info = null;
		PrintWriter printWriter = null;
		FileWriter fw = null;
		try {
			info = new StringWriter();
			printWriter = new PrintWriter(info);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			StringBuffer sb = new StringBuffer(info.toString());
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File log = new File(Environment.getExternalStorageDirectory(),
						Config.LOG_FILE);
				if (!log.exists()) {
					log.createNewFile();
				}
				fw = new FileWriter(log, true);
				String date = format.format(System.currentTimeMillis());
				fw.write(date);
				fw.write(System.getProperty("line.separator"));
				fw.write(sb.toString());
				fw.write(System.getProperty("line.separator"));
				fw.write("---------------------</>");
				fw.write(System.getProperty("line.separator"));
				fw.flush();
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (printWriter != null) {
					printWriter.close();
				}
				if (info != null) {
					info.close();
				}
				if (fw != null) {
					fw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
