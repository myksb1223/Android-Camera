package com.plokia.android_camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private static String TAG = "MainActivity";
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_ALBUM = 2;
	private static final int REQUEST_IMAGE_CROP = 3;
	private ImageView mImageView;
	private String mCurrentPhotoPath;
	private Uri contentUri;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mCurrentPhotoPath = null;		
		mImageView = (ImageView)findViewById(R.id.mImageView);
		File path = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES);		
		
		if(!path.exists()) {
			path.mkdirs();	
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.camera:
	        	dispatchTakePictureIntent();
	            return true;
	        case R.id.album:
						Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(intent, REQUEST_IMAGE_ALBUM);					        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void buttonPressed(View v) {
		if(v.getId() == R.id.cameraBtn) {
    	dispatchTakePictureIntent();			
		}
		else {
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, REQUEST_IMAGE_ALBUM);					        				
		}
	}
	
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
//            ...
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
        		contentUri = Uri.fromFile(photoFile);        	
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
				case REQUEST_IMAGE_ALBUM:
					contentUri = data.getData();
				case REQUEST_IMAGE_CAPTURE:
					rotatePhoto();					
					cropImage(contentUri);
					break;
				case REQUEST_IMAGE_CROP:
					Bundle extras = data.getExtras();
					if(extras != null) {
						Bitmap bitmap = (Bitmap)extras.get("data");						
						mImageView.setImageBitmap(bitmap);
						
						if(mCurrentPhotoPath != null) {
							File f = new File(mCurrentPhotoPath);
							if(f.exists()) {
								f.delete();
							}
							mCurrentPhotoPath = null;
						}
					}
					break;
			}
		}	    
	}	

	private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);
    Log.d(TAG, "storageDir : " + storageDir);
    Log.d(TAG, "fileName : " + imageFileName);
    File image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    );
    
    mCurrentPhotoPath = image.getAbsolutePath();
    
    return image;	
	}	
	
	private void galleryAddPic() {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCurrentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
	}
	
	private void cropImage(Uri contentUri) {
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
	  //indicate image type and Uri of image
	  cropIntent.setDataAndType(contentUri, "image/*");
	  //set crop properties
	  cropIntent.putExtra("crop", "true");
	  //indicate aspect of desired crop
	  cropIntent.putExtra("aspectX", 1);
	  cropIntent.putExtra("aspectY", 1);
	  //indicate output X and Y
	  cropIntent.putExtra("outputX", 256);
	  cropIntent.putExtra("outputY", 256);
	  //retrieve data on return
	  cropIntent.putExtra("return-data", true);
	  startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);		
	}
	
	public Bitmap getBitmap() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inInputShareable = true;
    options.inDither=false;
    options.inTempStorage=new byte[32 * 1024];
    options.inPurgeable = true;
    options.inJustDecodeBounds = false;
  	
  	File f = new File(mCurrentPhotoPath);
    
    FileInputStream fs=null;
    try {
        fs = new FileInputStream(f);
    } catch (FileNotFoundException e) {
        //TODO do something intelligent
        e.printStackTrace();
    }
    
    Bitmap bm = null;

    try {
        if(fs!=null) bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);
    } catch (IOException e) {
        //TODO do something intelligent
        e.printStackTrace();
    } finally{ 
        if(fs!=null) {
            try {
                fs.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }		
    return bm;
	}
	
  public void rotatePhoto() {
		ExifInterface exif;
		try {
			if(mCurrentPhotoPath == null) {
				mCurrentPhotoPath = contentUri.getPath();
			}
			exif = new ExifInterface(mCurrentPhotoPath);
	    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);	    
	    int exifDegree = exifOrientationToDegrees(exifOrientation);
	    if(exifDegree != 0) {
	    	Bitmap bitmap = getBitmap();			    	
		    Bitmap rotatePhoto = rotate(bitmap, exifDegree);
		    saveBitmap(rotatePhoto);			    			    
	    }	    					
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				

  }
  
	public int exifOrientationToDegrees(int exifOrientation)
	{
	  if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
	  {
	    return 90;
	  }
	  else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
	  {
	    return 180;
	  }
	  else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
	  {
	    return 270;
	  }
	  return 0;
	}
	
	public static Bitmap rotate(Bitmap image, int degrees)
	{
		if(degrees != 0 && image != null)
		{
			Matrix m = new Matrix();
			m.setRotate(degrees, (float)image.getWidth(), (float)image.getHeight());

			try
			{
				Bitmap b = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
				
				if(image != b)
				{
					image.recycle();
					image = b;
				}
					
				image = b;
			} 
			catch(OutOfMemoryError ex)
			{
				ex.printStackTrace();
			}
		}
		return image;
	}
	
	public void saveBitmap(Bitmap bitmap) {
  	File file = new File(mCurrentPhotoPath);
  	OutputStream out = null;
		try {
			out = new FileOutputStream(file);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bitmap.compress(CompressFormat.JPEG, 100, out) ;
		try {
			out.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
