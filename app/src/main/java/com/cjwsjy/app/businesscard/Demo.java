package com.cjwsjy.app.businesscard;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cjwsjy.app.commonui.WelcomeActivity;

import checkauto.camera.com.CameraActivity;
import checkauto.camera.com.Devcode;
import checkauto.camera.com.ImageChooser;
import checkauto.camera.com.util.CheckPermission;
import checkauto.camera.com.util.PermissionActivity;

public class Demo extends AppCompatActivity {

    Button btn;
    Button btn2;
    Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        activity=this;

        btn=findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 直接调用相机界面
                 */
                Intent intent = new Intent(Demo.this, CameraActivity.class);
                if(Build.VERSION.SDK_INT>=23){
                    CheckPermission checkPermission = new CheckPermission(activity);
                    if (checkPermission.permissionSet(ImageChooser.PERMISSION)) {
                        PermissionActivity.startActivityForResult(Demo.this,0,Devcode.DEVCODE,true,true,ImageChooser.PERMISSION);
                    } else {
                        intent = new Intent();
                        intent.setClass(Demo.this, CameraActivity.class);
                        intent.putExtra("devcode",Devcode.DEVCODE);
                        intent.putExtra("autocamera", true);
                        startActivity(intent);
                    }
                }else {
                    intent = new Intent();
                    intent.setClass(Demo.this, CameraActivity.class);
                    intent.putExtra("devcode",Devcode.DEVCODE);
                    intent.putExtra("autocamera", true);
                    startActivity(intent);
                }
            }
        });
        btn2=findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 直接调用相机界面
                 */
                Intent intent = new Intent(Demo.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
