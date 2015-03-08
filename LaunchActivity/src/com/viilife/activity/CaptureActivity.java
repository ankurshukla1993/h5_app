package com.viilife.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.hybridsquad.android.library.BasePhotoCropActivity;
import org.hybridsquad.android.library.CropHelper;
import org.hybridsquad.android.library.CropParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.viilife.R;

public class CaptureActivity extends BasePhotoCropActivity {

	// private ImageView imageView;
	private WebView webView;

	private String action = "";

	CropParams mCropParams = new CropParams();

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.capture_layout);
		init();
		action = getIntent().getStringExtra("callback");
	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void init() {
		findViewById(R.id.fetch_capture).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onCapture();
					}
				});
		findViewById(R.id.fetch_gallery).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onFetchPic();
					}
				});
	}

	@JavascriptInterface
	public void onCapture() {
		// Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
		// Environment.getExternalStorageDirectory(), "camera.jpg")));
		// startActivityForResult(intent, 9528);

		Intent intent = CropHelper.buildCaptureIntent(mCropParams.uri);
		startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
	}

	@JavascriptInterface
	public void onFetchPic() {
		// Intent intent = new Intent(Intent.ACTION_PICK,
		// MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		// intent.setType("image/*");
		// startActivityForResult(intent, 9528);
		Intent intent = CropHelper.buildCropFromGalleryIntent(mCropParams);
		startActivityForResult(intent, CropHelper.REQUEST_CROP);
	}

	@Override
	public CropParams getCropParams() {
		return mCropParams;
	}

	@Override
	public void onPhotoCropped(Uri uri) {
		Intent i = new Intent("ACTION_UPLOAD_IMG");
		String filePath = uri.getPath();
		Log.i("viilife", "Crop Uri in path: " + filePath);
		i.putExtra("url", filePath);
		i.putExtra("callback", action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
		finish();
	}

	@Override
	public void onCropCancel() {
		Toast.makeText(this, "Crop canceled!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCropFailed(String message) {
		Toast.makeText(this, "Crop failed:" + message, Toast.LENGTH_LONG)
				.show();
	}

	private void onCorpImg(Uri uri, int outputX, int outputY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 2);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}

	// @Override
	public void onActivityResult_(int requestCode, int resultCode, Intent data) {

		if (requestCode == 9528 && resultCode == Activity.RESULT_OK) {
			Uri uri = null;
			if (data == null) {
				uri = Uri.fromFile(new File(Environment
						.getExternalStorageDirectory(), "camera.jpg"));
			} else {
				uri = data.getData();
			}
			onCorpImg(uri, 400, 400, 9527);
			return;
		}
		if (requestCode == 9527 && resultCode == Activity.RESULT_OK) {
			Bitmap myBitmap = null;
			String filePath = "";
			if (data == null) {
				filePath = new File(Environment.getExternalStorageDirectory(),
						"camera.jpg").getAbsolutePath();
				myBitmap = justifyPic(filePath, true);
			} else {
				Uri uri = data.getData();
				if (uri != null) {
					try {
						String[] proj = { MediaStore.Images.Media.DATA };

						ContentResolver resolver = getContentResolver();

						Cursor cursor = resolver.query(uri, proj, null, null,
								null);

						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

						cursor.moveToFirst();

						filePath = cursor.getString(column_index);
						cursor.close();
						myBitmap = justifyPic(filePath, false);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}

			if (myBitmap != null) {
				// imageView.setImageBitmap(myBitmap);
				// webView.loadUrl("javascript:uploadPic('" + filePath + "')");
				myBitmap.recycle();

				Intent i = new Intent("ACTION_UPLOAD_IMG");
				i.putExtra("url", filePath);
				i.putExtra("callback", action);
				LocalBroadcastManager.getInstance(this).sendBroadcast(i);
				finish();
			}
		}
	}

	private Bitmap justifyPic(String filePath, boolean compress) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		options.inSampleSize = calculateInSampleSize(options, 960, 1600);

		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		if (compress) {
			FileOutputStream fos = null;
			try {

				File f = new File(filePath);
				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				fos = new FileOutputStream(filePath);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int ro = readPictureDegree(filePath);
		bm = rotateBitmap(bm, ro);
		return bm;
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

	private int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	private Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
		if (bitmap == null)
			return null;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
}
