package com.fujisoft.takephoto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class CompressActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CompressActivity";
    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 0x1111;
    private Button btnReadPic;
    private EditText etQuality;
    private ImageView ivShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        btnReadPic = (Button) findViewById(R.id.btn_read_photo);
        btnReadPic.setOnClickListener(this);
        etQuality = (EditText) findViewById(R.id.et_quality);
        ivShow = (ImageView) findViewById(R.id.iv_show);
    }

    /**
     * 读取照片大小
     */
    private void readPhoto() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/IMG_20171113_112808.jpg";
        Bitmap bit = BitmapFactory.decodeFile(path);
        Log.i(TAG, "压缩前图片的大小" + (bit.getByteCount() / 1024 / 1024) + "M宽度为" + bit.getWidth() + "高度为" + bit.getHeight());
//        qualityCompress(bit);
//        samplingRateCompress(path);
        File file = new File(path);
        Log.i(TAG, "readPhoto: " + file.getPath());
        lubanCompress(file);
    }

    /**
     * 鲁班压缩
     * 需要两个权限：sd卡读权限与写权限。
     */
    private void lubanCompress(File file) {
        Luban.with(this)
                .load(file)
                .setTargetDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart: ");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.i(TAG, "onSuccess: " + file.getPath());
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        ivShow.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: " + e.toString());
                    }
                }).launch();
    }

    /**
     * 采样率压缩
     *
     * @param path 图片路径
     */
    private void samplingRateCompress(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Integer.parseInt(etQuality.getText().toString().trim());
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        Log.i("wechat", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024)
                + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight());
    }

    /**
     * 质量压缩
     *
     * @param bit
     */
    private void qualityCompress(Bitmap bit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = Integer.parseInt(etQuality.getText().toString());
        bit.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.i("wechat", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024)
                + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight()
                + "bytes.length=  " + (bytes.length / 1024) + "KB"
                + "quality=" + quality);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_read_photo:
                checkPermissionBeforeReadPic();
                break;
        }
    }

    /**
     * 读取之前检查权限
     */
    private void checkPermissionBeforeReadPic() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, "checkPermissionBeforeReadPic: " + permissionWrite);
        if (Build.VERSION.SDK_INT >= 23) {
            if (permissionGranted) {
                readPhoto();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            readPhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_STORAGE:
                if (permissions.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        toast("用户同意了读取sd卡的权限");
                    } else {
                        Snackbar snackbar = SnackbarUtil.shortSnackbar(btnReadPic, "请到设置中打开对应权限", SnackbarUtil.Info).setActionTextColor(Color.WHITE).setAction("打开设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toast("打开设置");
                            }
                        });
                        snackbar.show();
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        toast("用户同意了写入sd卡的权限");
                    } else {
                        Snackbar snackbar = SnackbarUtil.shortSnackbar(btnReadPic, "请到设置中打开对应权限", SnackbarUtil.Info).setActionTextColor(Color.WHITE).setAction("打开设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toast("打开设置");
                            }
                        });
                        snackbar.show();
                    }
                }
                break;
        }
    }

    /**
     * 谈吐司
     *
     * @param message
     */
    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
