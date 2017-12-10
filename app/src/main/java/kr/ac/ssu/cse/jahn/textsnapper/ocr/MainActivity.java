package kr.ac.ssu.cse.jahn.textsnapper.ocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.ssu.cse.jahn.textsnapper.R;

public class MainActivity extends AppCompatActivity
{
    //앱기본폴더 경로
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TextSnapper/";
    //언어팩(traindata) 이름
    public static final String LANG = "eng";
    private static final String TAG = "Mainactivity";
    protected Button mButtonGallery;
    protected Button mButtonCamera;
    protected ImageView mImageView;
    protected EditText mText;
    protected String mPhotoDirPath;
    protected String mPhotoPath;
    protected boolean _taken;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_GALLERY = 1;

    private static final String PHOTO_TAKEN = "photo_taken";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mText = (EditText) findViewById(R.id.editText);
        mImageView = (ImageView)findViewById(R.id.imageView);
        //mButtonGallery = (Button) findViewById(R.id.gallery);
        mButtonGallery.setOnClickListener(new ButtonClickHandler());
        //mButtonCamera = (Button) findViewById(R.id.camera);
        mButtonCamera.setOnClickListener(new ButtonClickHandler());
        //권한요청
        request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA);

        /*
         앱 폴더 생성
         */
        mPhotoDirPath = DATA_PATH + "photo/";
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/", DATA_PATH + "photo/" };
        for (String path : paths)
        {
            File dir = new File(path);
            if (!dir.exists())
            {
                if (!dir.mkdirs())
                {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                }
                else
                    Log.v(TAG, "Created directory " + path + " on sdcard");
            }
        }

        // LANG.traineddata 파일을 앱내부(assets)에서 가져와 스토리지에 복사
        if (!(new File(DATA_PATH + "tessdata/" + LANG + ".traineddata")).exists())
        {
            try
            {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + LANG + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + LANG + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0)
                // {
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + LANG + " traineddata");
            } catch (IOException e)
            {
                Log.e(TAG, "Was unable to copy " + LANG + " traineddata " + e.toString());
            }
        }
    }

    public class ButtonClickHandler implements View.OnClickListener
    {
        public void onClick(View view)
        {
            /*switch (view.getId())
            {
            case R.id.gallery:
                fromGallery();
                break;
            case R.id.camera:
                fromCamera();
                break;
            }*/
        }
    }
    private void fromGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void fromCamera()
    {
        File photoFile = createImageFile();
        //파일프로바이더 사용
        Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".provider",photoFile);
        final Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        takePic.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePic, REQUEST_CAMERA);
        }
    }
    private File createImageFile()
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(mPhotoDirPath);
        File image;
        try
        {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        }
        catch (IOException e)
        {
            image=null;
            Log.e("TAG","Can't create TempFile");
        }
        // Save a file: path for use with ACTION_VIEW intents
        mPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Bitmap imageBitmap;
        Uri photoUri;
        Log.i(TAG, "resultCode: " + resultCode);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
            case REQUEST_GALLERY:
                photoUri = data.getData();
                Intent intent = new Intent(this, OCRService.class);
                intent.setDataAndType(photoUri,"image/*");
                intent.putExtra("imagesource", ImageSource.GALLERY);
                startService(intent);
                //mImageView.setImageBitmap(imageBitmap);
                break;
            case REQUEST_CAMERA:
                imageBitmap = BitmapFactory.decodeFile(mPhotoPath);
                mImageView.setImageBitmap(imageBitmap);
                onPhotoTaken();
                break;
            }
        }
        else
        {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean(TessOCRTestActivity.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(TessOCRTestActivity.PHOTO_TAKEN))
        {
            onPhotoTaken();
        }
    }

    protected void onPhotoTaken()
    {
        _taken = true;
        Toast.makeText(this, "OCR Started", Toast.LENGTH_SHORT).show();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath, options);

        try
        {
            ExifInterface exif = new ExifInterface(mPhotoPath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0)
            {

                // 이미지의 폭, 높이 가져오기
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // prerotate 설정
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e)
        {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, LANG);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();


        Log.v(TAG, "OCRED TEXT: " + recognizedText);
        //trim alpha-numeric version of it (if LANG is eng)
        if ( LANG.equalsIgnoreCase("eng") )
        {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }
		/*else if ( LANG.equalsIgnoreCase("kor") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}*/
        recognizedText = recognizedText.trim();

        if ( recognizedText.length() != 0 )
        {
            mText.setText(mText.getText().toString().length() == 0 ? recognizedText : mText.getText() + " " + recognizedText);
            mText.setSelection(mText.getText().toString().length());
        }
        Toast.makeText(this, "OCR done", Toast.LENGTH_SHORT).show();
        // Cycle done.
    }

    public void request(String... permissions)
    {
        //모든 권한이 있는지 확인
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(String permission:permissions)
        {
            if(ContextCompat.checkSelfPermission(this, permission)==PackageManager.PERMISSION_GRANTED)
                Log.v("TAG",permission+"권한 있음");
            else
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                {
                    Log.v("TAG", permission + "권한 설명필요");
                }
                else
                {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                    Log.v("TAG",permission+"권한 있음");
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (grantResults.length <= 0)
            return;
        for(int i = 0; i<grantResults.length; i++)
        {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                Log.v("TAG",permissions[i]+"권한 승인");
            else
                Log.v("TAG",permissions[i]+"권한 거부");
        }
    }
}
