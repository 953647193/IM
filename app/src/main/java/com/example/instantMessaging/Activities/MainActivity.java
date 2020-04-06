package com.example.instantMessaging.Activities;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.common.app.Activity;
import com.example.common.app.Fragment;
import com.example.factory.presenter.contact.ContactPresenter;
import com.example.instantMessaging.Fragments.main.ContactFragment;
import com.example.instantMessaging.Fragments.main.MessageFragment;
import com.example.instantMessaging.Fragments.main.MomentFragment;
import com.example.instantMessaging.MPopupWindow;
import com.example.instantMessaging.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import com.example.instantMessaging.MPopupWindow.onGetTypeClckListener;
import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends Activity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Fragment mCurrentFragment;
    //消息界面
    private MessageFragment mMessageFragment;
    //联系人界面
    private ContactFragment mContactFragment;
    //朋友圈界面
    private MomentFragment mMomentFragment;

    private ContactPresenter mContactPresenter;

    //设置更换头像相关参数
    private File outputImage;
    private Uri ImgUri;
    private MPopupWindow.Type type;
    private MPopupWindow mPopupWindow;

    @BindView(R.id.bottom_bar)
    BottomNavigationView mBottomBar;

    //绑定DrawerLayout
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    //绑定DrawerLayout中的NavigationView
    @BindView(R.id.nav_view)
    NavigationView navView;

    //DrawerLayout中的头像
    private CircleImageView mPersonPortrait;

    //Toolbar中作为导航的个人头像
    @BindView(R.id.img_portrait)
    ImageView mNavPortrait;

    @BindView(R.id.img_settings)
    ImageView mSettings;

//点击头像显示DrawerLayout
    @OnClick({R.id.img_portrait,R.id.img_settings})
    public void onViewClicked(View view){
        switch(view.getId()){
            case R.id.img_portrait:
                mDrawerLayout.openDrawer(GravityCompat.START);
                Log.d("MainActivity","you clicked portrait");
                break;
            case R.id.img_settings:
                Log.d("MainActivity","you clicked settings");
                //菜单
                break;
            default:
                break;

        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }

    //控件初始化
    @Override
    protected void initWidget() {
        super.initWidget();

        //设置bottomNavbar监听
        mBottomBar.setOnNavigationItemSelectedListener(this);

        //初始化界面为消息界面
        mMessageFragment = new MessageFragment();
        mCurrentFragment = mMessageFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_container_main, mMessageFragment).commit();

        //动态引用NavigationView的头部实例mPersonalPortrait并添加点击事件
        View navHeaderView = navView.inflateHeaderView(R.layout.nav_header);
        mPersonPortrait =  navHeaderView.findViewById(R.id.icon_portrait);
        mPersonPortrait.setOnClickListener(v -> {
            //创建MPopupWindow实例
            mPopupWindow = new MPopupWindow(MainActivity.this,MainActivity.this);
            //设置弹出的父界面
            View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main,null);
            mPopupWindow.showPopupWindow(rootView);
            //接口，传递ImgUri、Type和outputImage
            mPopupWindow.setOnGetTypeClckListener(new onGetTypeClckListener() {
                @Override
                public void getType(MPopupWindow.Type type) {
                    MainActivity.this.type=type;
                }
                @Override
                public void getImgUri(Uri ImgUri, File outputImage) {
                    MainActivity.this.ImgUri = ImgUri;
                    MainActivity.this.outputImage = outputImage;

                }
            });

        });
    }

    //得到Intent结果后处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            //拍照显示
            case 1:
                if (ImgUri!=null){
                    mPopupWindow.onPhoto(ImgUri,300,300);
                }
                break;
            //获取所选图片的uri并剪裁，动态权限申请
            case 2:
                if (data!=null){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                    }else{
                        Uri uri = data.getData();
                        mPopupWindow.onPhoto(uri,300,300);
                    }
                }
                break;
            //剪裁后进行设置
            case 3:
                if (type == MPopupWindow.Type.PHONE){//相册选择修剪后显示
                    if(data!=null){
                        if (Build.VERSION.SDK_INT>=19){
                        handleImageOnKitKat(mPopupWindow.imageCropUri);
                        }else{
                            //兼容4.4以下版本
                        handleImageBeforeKitKat(mPopupWindow.imageCropUri);
                        }
                    }
                }else if(type == MPopupWindow.Type.CAMERA){//相机拍照修建后显示
                    /*mPersonPortrait.setImageBitmap(BitmapFactory.decodeFile(outputImage.getPath()));*/
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mPopupWindow.imageCropUri));
                        mPersonPortrait.setImageBitmap(bitmap);
                        mNavPortrait.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            default:
                break;
        }
    }

    //权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1://相册调用申请
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mPopupWindow.openAlbum();
                }else{
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            case 2://Crop修剪后文件外部存储的申请
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Uri uri = getIntent().getData();
                    mPopupWindow.onPhoto(uri,300,300);
                }else{
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;

        }
    }

    //处理Android4.4以上版本的图片
    private void  handleImageOnKitKat(Uri uri){
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this,uri)){
            //处理Document类型的Uri,通过DocumentId
            String docId =  DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){//如果Uri的anthority是media格式，DocumentId还需再进行解析
                //解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }

        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);

        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    //处理Android4.4以下版本的图片
    private void handleImageBeforeKitKat(Uri uri){
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    //获取图片真实路径
    private String getImagePath(Uri uri,String selection){
        String path = null;
        //通过uri和selection来获取图片真实路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor!=null){
            if ((cursor.moveToFirst())){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //根据路径显示图片
    private void displayImage(String imagePath){
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mPersonPortrait.setImageBitmap(bitmap);
            mNavPortrait.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"Failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示入口
     * @param context the context
     */
    public static void show(Context context){
        context.startActivity(new Intent(context, MainActivity.class));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.main_message:
                changeFragment(mMessageFragment);
                return true;
            case R.id.main_contact:
                if(mContactFragment == null){
                    mContactFragment = new ContactFragment();
                }
                //创建联系人presenter实例
                mContactPresenter = new ContactPresenter(mContactFragment);
                changeFragment(mContactFragment);
                Log.d("testBottomVav", "contact");
                return true;
            case R.id.main_moment:
                if(mMomentFragment == null){
                    mMomentFragment = new MomentFragment();
                }
                changeFragment(mMomentFragment);
                Log.d("1213", "moment");
                return true;
        }
        return false;
    }


    /**
     * 切换fragement
     * @param fragment 要切换的目标fragment
     */
    private void changeFragment(Fragment fragment){
        if (mCurrentFragment != fragment){
            if(!fragment.isAdded()){
                getSupportFragmentManager().beginTransaction()
                        .hide(mCurrentFragment)
                        .add(R.id.layout_container_main, fragment).commit();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .hide(mCurrentFragment)
                        .show(fragment).commit();
            }
            mCurrentFragment = fragment;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPopupWindow!=null){
            mPopupWindow.dismiss();
        }
    }
}
