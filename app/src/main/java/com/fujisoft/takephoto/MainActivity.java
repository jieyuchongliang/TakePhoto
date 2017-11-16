package com.fujisoft.takephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * 拍照获取原图要注意（适配）： 1，相要获取原图，必须先存储再读取
 *                              2，SD卡权限问题，6.0之后要动态申请（只需申请读或者写权限的其中一个就行）。
 *                              3，URI问题，7.0之后有了特殊处理。详见github优秀文章。
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int TAKE_PHOTO = 0x1111;
    private static final int TAKE_ORIGINAL_PHOTO = 0x2222;
    private static final int PERMISSION_SD_CARD = 0x1111;
    private Button btnTakePhoto, btnTakePhoto1, btnCompressPic;
    private ImageView ivShowPhoto;
    private String sdPath;//SD卡的路径
    private String picPath;//图片存储路径
    private File photoFile;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        ivShowPhoto = (ImageView) findViewById(R.id.iv_show_photo);
        btnTakePhoto1 = (Button) findViewById(R.id.btn_take_photo1);
        btnCompressPic = (Button) findViewById(R.id.btn_take_photo2);
        btnCompressPic.setOnClickListener(this);
        btnTakePhoto1.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
    }

    private static final String TAG = "MainActivityy";

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_photo:
                takePhoto();
                break;
            case R.id.btn_take_photo1:
                takePhotoForOriginal();
                break;
            case R.id.btn_take_photo2:
                startActivity(new Intent(this, CompressActivity.class));
                break;
        }
    }

    /**
     * 拍照获取原图
     * 版本适配时的权限申请：sd卡的读取与写入只申请一个就ok了。
     */
    private void takePhotoForOriginal() {
        boolean permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, "permissionWrite: " + permissionWrite);
        if (Build.VERSION.SDK_INT >= 23) {
            if (permissionWrite) {
                takePhotoAfterPermission();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_SD_CARD);
            }
        } else {
            takePhotoAfterPermission();
        }
    }

    /**
     * 权限申请的回调
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_SD_CARD) {
            if (permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoAfterPermission();
                    Log.i(TAG, "onRequestPermissionsResult: 用户同意sd卡使用权限");
                } else {
                    Toast.makeText(this, "请通过设置打开存储权限", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 权限申请过后调用相机拍照
     */
    private void takePhotoAfterPermission() {
        //获取SD卡的路径
        sdPath = Environment.getExternalStorageDirectory().getPath();
        picPath = sdPath + "/" + getTime() + ".png";
        Log.i(TAG, "takePhotoForOriginal: " + picPath);
        File photoFile = new File(picPath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri = Uri.fromFile(photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, TAKE_ORIGINAL_PHOTO);
    }

    /**
     * 拍照获取bitmap图片
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                ivShowPhoto.setImageBitmap(bitmap);
                break;
            case TAKE_ORIGINAL_PHOTO:
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 10;
                Bitmap bitmap1 = BitmapFactory.decodeFile(picPath, options);
                ivShowPhoto.setImageBitmap(bitmap1);
                break;
        }
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public String getTime() {
        long time = System.currentTimeMillis() / 1000;//获取系统时间的10位的时间戳
        String str = String.valueOf(time);
        return str;
    }
}
