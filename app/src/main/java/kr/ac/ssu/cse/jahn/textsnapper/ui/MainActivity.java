package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.ImageSource;
import kr.ac.ssu.cse.jahn.textsnapper.util.PrefUtils;
import kr.ac.ssu.cse.jahn.textsnapper.util.Utils;
import ly.img.android.ui.activities.ImgLyIntent;

import static kr.ac.ssu.cse.jahn.textsnapper.util.Utils.APP_PATH;
import static kr.ac.ssu.cse.jahn.textsnapper.util.Utils.copyTessdata;
import static kr.ac.ssu.cse.jahn.textsnapper.util.Utils.startEditor;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static int statusbarHeight;
    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_GALLERY = 101;
    public static final int REQUEST_EDIT = 102;
    public static final int REQUEST_MEDIA_PROJECTION = 103;
    public static final int REQUEST_PERMISSION_OVERLAY = 104;

    // Floating Action Button Overlay를 위한 요청 코드
    private static final String TAG = "Mainactivity";
    private static final String[] LANGS = {"eng", "kor"};
    protected String mPhotoDirPath = APP_PATH + "photo/";
    protected String mPhotoPath;

    protected MediaProjectionManager mProjectionManager;
    protected MediaProjection mProjection;
    protected ImageReader mImageReader;
    protected Display mDisplay;
    protected Intent mProjectionIntent;
    protected int mDensity;
    protected int mWidth;
    protected int mHeight;
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    protected static boolean isForeground = false;
    public static Activity mActivity;

    ActionBarDrawerToggle toggle;
    ViewPager viewPager;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //상단바 높이 저장
        statusbarHeight = Utils.getStatusBarHeight(getResources());
        //권한요청
        Utils.request(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

        //디렉토리 생성
        Utils.makeAppDir();
        //파일복사
        copyTessdata(getAssets(),LANGS);


        /**
         * DrawerLayout ==> 좌상단 메뉴버튼 클릭하면 나오는 버튼을 위한 코드
         * toggle.syncState()를 해야 메뉴 버튼이 추가되니 toggle.syncState()까지
         * 코드를 수정하지 말 것!
         **/
        drawer = (DrawerLayout) findViewById(R.id.mainDrawer);
        toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navi_view);
        navigationView.setNavigationItemSelectedListener(this);


        /**
         * 이하는 Main Activity 버튼에 관한 이벤트 처리
         *
         * 각 버튼에 대해 클릭되었을 때 나타나는 애니메이션 리스너를 달아준다
         */
        ImageView imageCamera = (ImageView)findViewById(R.id.imageCamera);
        ImageView imageGallery = (ImageView)findViewById(R.id.imageGallery);
        ImageView imageWidget = (ImageView)findViewById(R.id.imageWidget);

        imageCamera.setOnTouchListener(Utils.imageTouchEventListener);
        imageGallery.setOnTouchListener(Utils.imageTouchEventListener);
        imageWidget.setOnTouchListener(Utils.imageTouchEventListener);
        imageWidget.setOnClickListener(floatingButtonEventListener);

        imageGallery.setOnClickListener(new ButtonClickListener());
        imageCamera.setOnClickListener(new ButtonClickListener());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }
    public class ButtonClickListener implements View.OnClickListener
    {
        public void onClick(View view)
        {
            switch (view.getId())
            {
            case R.id.imageGallery:
                startGallery();
                break;
            case R.id.imageCamera:
                Utils.startCamera(mActivity);
                break;
            }
        }
    }

    public void startGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imageBitmap;
        Uri photoUri;
        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == RESULT_OK)
        {
            photoUri = data.getData();
            switch (requestCode)
            {
            case REQUEST_PERMISSION_OVERLAY:
                if (!Utils.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    startFloatingHead();
                }
                break;
            case REQUEST_MEDIA_PROJECTION:
                mProjectionIntent = data;
                data.putExtra("resultcode", resultCode);
                break;
            case REQUEST_GALLERY:
                startEditor(Utils.getRealPathFromUri(this, photoUri),mActivity);
                break;
            case REQUEST_CAMERA:
            case REQUEST_EDIT:
                String resultPath =
                        data.getStringExtra(ImgLyIntent.RESULT_IMAGE_PATH);
                String sourcePath =
                        data.getStringExtra(ImgLyIntent.SOURCE_IMAGE_PATH);

                if (sourcePath != null) {
                    // Scan camera file
                    File file =  new File(sourcePath);
                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    photoUri = Uri.fromFile(file);
                    scanIntent.setData(photoUri);
                    sendBroadcast(scanIntent);
                }

                if (resultPath != null) {
                    // Scan result file
                    File file =  new File(resultPath);
                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    photoUri = Uri.fromFile(file);
                    scanIntent.setData(photoUri);
                    sendBroadcast(scanIntent);
                }


                Log.e("TAG","onActivityResult(): 카메라 & 에디트"+photoUri);
                Intent result = new Intent(this, ResultActivity.class);
                result.setDataAndType(photoUri,"image/*");
                result.putExtra("imagesource", ImageSource.CROP);
                startActivity(result);
                break;
            }
        }
        else
        {
            Log.v(TAG, "User cancelled");
        }
    }

    private File createImageFile()
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
    /**
     * Floating Button Overlay!
     * 완성된 코드이므로 더 이상 수정할 필요 없음
     */
    ImageView.OnClickListener floatingButtonEventListener = new ImageView.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (Utils.canDrawOverlays(MainActivity.this)) {
                if(PrefUtils.isAvailable(getApplicationContext()))
                    startFloatingHead();
                else
                    Toast.makeText(getApplicationContext(), "FloatingButton 옵션이 비활성화되어 있습니다.", Toast.LENGTH_LONG).show();
            } else {
                requestPermission(REQUEST_PERMISSION_OVERLAY);
            }
        }
    };

    /**
     * TabLayout 리스너
     */
    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {        }
    };

    /**
     * Resumed 상태에서 SharedPreferences 복구
     * Preference 상태 변화에 따른 변화 코드를 작성할 때는
     * PrefFragment.java 에서 작성할 것
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences setRefer = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = setRefer.edit();
        editor.putString("ocrSelect",setRefer.getString("ocrSelect", "English"));
        editor.putBoolean("floatingButtonUse",setRefer.getBoolean("floatingButtonUse", true));
        editor.putBoolean("floatingButtonLocation",setRefer.getBoolean("floatingButtonLocation", false));
        editor.commit();
        isForeground = true;
    }

    @Override
    protected void onPause()
    {
        isForeground = false;
        super.onPause();
    }

    /**
     * 우측 상단의 메뉴 버튼 추가
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 우측 상단 메뉴 버튼이 눌렸을 때의 이벤트
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        Intent intent;
        if (toggle.onOptionsItemSelected(item))
            return true;
        switch (curId) {
            case R.id.menuConfig:
                intent = new Intent(this, ConfigActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigation Button이 눌렸을 때의 이벤트
     **/
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int curId = item.getItemId();
        Intent intent;

        switch (curId) {
            case R.id.navigation_config:
                intent = new Intent(this, ConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_guide:
                break;
            case R.id.navigation_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_favorite:
                drawer.closeDrawers();
                viewPager.setCurrentItem(1);
                break;
        }
        return true;
    }

    private void startFloatingHead() {
        boolean isAvailable = PrefUtils.isAvailable(getApplicationContext());
        /**
         * 서비스가 버튼을 클릭할 때 마다 실행되는 문제 해결
         */
        if(!FloatingService.isServiceActive() && isAvailable) {
            Intent intent = new Intent(getApplicationContext(), FloatingService.class);
            intent.putExtra("projection", mProjectionIntent);
            startService(intent);
        }
        /**
         * Option 연동
         */
        if(!isAvailable) {
            Toast.makeText(getApplicationContext(), "FloatingButton 옵션이 비활성화되어 있습니다.", Toast.LENGTH_LONG).show();
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
    /**
     * Floating Button을 위해서는 Overlay Permission을 반드시 얻어야 함
     **/
    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("기능을 이용하시기 위해서는\n권한을 부여받아야 합니다.");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (FloatingService.isServiceActive()) {
            Intent stopIntent = FloatingService.getCurrentFloatingService();
            stopService(stopIntent);
        }
    }
}