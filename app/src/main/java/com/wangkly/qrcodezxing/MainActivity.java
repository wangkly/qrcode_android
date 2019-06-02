package com.wangkly.qrcodezxing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button scan;

    private Button generate;

    private EditText editText;

    private ImageView geCode;

    private TextView result;

    private Bitmap bitmap;

    public static final int  SCANREQUEST = 0;

    public static final int PERMISSION_REQUESTCODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.text);

        scan = findViewById(R.id.scan);

        geCode = findViewById(R.id.geCode);

        result = findViewById(R.id.textView);

        generate = findViewById(R.id.generate);

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)
                != PermissionChecker.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUESTCODE);

        }else {

            remainOperation();
        }

    }


    public void remainOperation(){

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,CaptureActivity.class);

                startActivityForResult(intent,SCANREQUEST);


            }
        });


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(editText.getText());

                bitmap = CodeCreator.createQRCode(text, 400, 400,null);

                geCode.setImageBitmap(bitmap);
                geCode.setVisibility(View.VISIBLE);

                result.setVisibility(View.GONE);

            }
        });


        geCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                dialog.setTitle("保存二维码");

                dialog.setMessage("保存二维码到相册?");

                dialog.setCancelable(false);

                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        saveBitmapToGallary();
                    }
                });

                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return false;
            }
        });

    }


    public void saveBitmapToGallary(){

        File saveDir = new File(Environment.getExternalStorageDirectory(),"qrcode");

        if(!saveDir.exists()){
            saveDir.mkdir();
        }


        String fileName = System.currentTimeMillis()+".jpg";

        File file = new File(saveDir,fileName);


        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);

            outputStream.flush();

            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),file.getAbsolutePath(),fileName,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse(file.getAbsolutePath())));

        Toast.makeText(MainActivity.this,"保存成功",Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SCANREQUEST && resultCode == RESULT_OK){
            if(data != null){
                String content =  data.getStringExtra(Constant.CODED_CONTENT);
//                Toast.makeText(this,content,Toast.LENGTH_LONG).show();
                result.setText(content);
                geCode.setVisibility(View.GONE);
                result.setVisibility(View.VISIBLE);
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUESTCODE && grantResults.length > 0){

            if(grantResults[0] == PermissionChecker.PERMISSION_GRANTED
                    && grantResults[1] == PermissionChecker.PERMISSION_GRANTED){
                    remainOperation();
            }else {

                Toast.makeText(MainActivity.this,"缺少相关权限",Toast.LENGTH_LONG).show();
            }
        }

    }
}
