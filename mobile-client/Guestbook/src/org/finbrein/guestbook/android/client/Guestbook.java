package org.finbrein.guestbook.android.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Guestbook extends Activity {

	private Button send;
	private EditText username, message;

	private static final int ACTION_TAKE_PHOTO_S = 2;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}


	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		int height = 60; // height in pixels
		int width = 60; // width in pixels 
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, height, width, false);

		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(scaled);
		mImageView.setVisibility(View.VISIBLE);
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath); 
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;

		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}

		startActivityForResult(takePictureIntent, actionCode);
	}

	private void handleSmallCameraPhoto(Intent intent) {
		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			//mCurrentPhotoPath = null;
		}
	}

	Button.OnClickListener mTakePicSOnClickListener = 
			new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		send = (Button) findViewById(R.id.send);
		username = (EditText) findViewById(R.id.username);
		message = (EditText) findViewById(R.id.message);

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String   mUsername = username.getText().toString();
				String  mMessage = message.getText().toString();
				
					send(mUsername, mMessage);
				
			}
		});

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;

		Button picSBtn = (Button) findViewById(R.id.btnIntendS);
		setBtnListenerOrDisable( 
				picSBtn, 
				mTakePicSOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
				);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode ==	ACTION_TAKE_PHOTO_S);
		if (resultCode == RESULT_OK) {
			handleSmallCameraPhoto(data);
		}

	}


	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
				);
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
			) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText(getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}

	public void send(String mUsername, String mPassword) {
		if(mCurrentPhotoPath == null)
			return;
			
		HttpURLConnection connection = null;
		String userName = mUsername;
		String message = mPassword;

		File binaryFile = new File(mCurrentPhotoPath);
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		String charset = "UTF-8";
		String response = null;

		DataOutputStream outputStream = null;

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 5*1024*1024;

		PrintWriter writer = null;
		try {
			URL url = new URL("http://app.cc.puv.fi/vaasaguestbook/");
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			outputStream = new DataOutputStream( connection.getOutputStream() );

			// Send normal param.
			outputStream.writeBytes("--" + boundary + CRLF);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"userName\"" + CRLF);
			outputStream.writeBytes("Content-Type: text/plain; charset=" + charset + CRLF);
			outputStream.writeBytes(CRLF);
			outputStream.writeBytes(userName + CRLF);
			outputStream.flush();

			// Send normal param.
			outputStream.writeBytes("--" + boundary + CRLF);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"message\"" + CRLF);
			outputStream.writeBytes("Content-Type: text/plain; charset=" + charset + CRLF);
			outputStream.writeBytes(CRLF);
			outputStream.writeBytes(message + CRLF);
			outputStream.flush();

			// Send normal image
			outputStream.writeBytes("--" + boundary + CRLF);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + mCurrentPhotoPath +"\"" + CRLF);
			outputStream.writeBytes(CRLF);

			FileInputStream input = null;
			try {
				input = new FileInputStream(binaryFile);
				bytesAvailable = input.available(); //add
				bufferSize = Math.min(bytesAvailable, maxBufferSize); //add
				buffer = new byte[bufferSize]; //add

				// Read file
				bytesRead = input.read(buffer, 0, bufferSize);

				while (bytesRead > 0)
				{
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = input.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = input.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(CRLF);
				outputStream.writeBytes("--" + boundary + "--" + CRLF);

				input.close();
				outputStream.flush();
				outputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
			}

			String line = "";               
			InputStreamReader isr = new InputStreamReader(connection.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			// Response from server after login process will be stored in response variable.                
			response = sb.toString();
			// You can perform UI operations here
			Toast.makeText(this,"Message from Server: \n"+ response, 0).show();             
			isr.close();
			reader.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
	}
}