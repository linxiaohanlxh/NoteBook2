package com.example.notebook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notebook.R;
import com.example.notebook.db.MyDateBaseHelper;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddActivity extends AppCompatActivity {

    private EditText et_title;
    private EditText et_content;
    private EditText et_writer;
    private TextView tv_time;
    private Button btn_add;
    private Button btn_return;
    private Button btn_addPictureByAlbum;

    public static final int CHOOSE_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        init();
    }

    private void init() {
        et_title = findViewById(R.id.et_title);
        et_content = findViewById(R.id.et_content);
        et_writer = findViewById(R.id.et_writer);
        tv_time = findViewById(R.id.tv_time);
        btn_add = findViewById(R.id.btn_add);
        btn_return = findViewById(R.id.btn_return);
        btn_addPictureByAlbum = findViewById(R.id.btn_addPictureByAlbum);

        //设置当前时间
        tv_time.setText(getTime());

        //sqlite数据库
        MyDateBaseHelper myDateBaseHelper = new MyDateBaseHelper(this);

        //按钮点击事件
        //点击添加
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将数据写入数据库
                SQLiteDatabase sqLiteDatabase = myDateBaseHelper.getWritableDatabase();
                ContentValues note = new ContentValues();

                String title = et_title.getText().toString();
                String content = et_content.getText().toString();
                String time = tv_time.getText().toString();
                String writer = et_writer.getText().toString();
                //标题不能为空，以toast形式提示
                if (et_title.getText().toString().equals("")) {
                    Toast.makeText(AddActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //内容不能为空，以toast形式提示
                if (et_content.getText().toString().equals("")) {
                    Toast.makeText(AddActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                note.put("Title", title);
                note.put("Content", content);
                note.put("Time", time);
                //如果作者名为空，则设置默认值unknown
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                if (writer.equals("")) {
                    note.put("Writer", "unknown");
                    editor.putString("writer", "unknown");
                } else {
                    note.put("Writer", writer);
                    editor.putString("writer", writer);
                }
                editor.apply();

                sqLiteDatabase.insert("Note", null, note);
                //添加成功以toast形式提示
                Toast.makeText(AddActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                myDateBaseHelper.close();
                //跳转到主界面
                Intent intent = new Intent(AddActivity.this, com.example.notebook.activity.MainActivity.class);
                startActivity(intent);
            }
        });

        //点击返回
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到主界面
                Intent intent = new Intent(AddActivity.this, com.example.notebook.activity.MainActivity.class);
                startActivity(intent);
            }
        });

        //点击从相册添加照片
        btn_addPictureByAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGallery();
            }
        });
    }

    //获取当前时间
    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = sdf.format(date);
        return str;
    }


    //region 调用图库
    private void callGallery(){
        Intent getAlbum = new Intent(Intent.ACTION_PICK);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,1);
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //参考网址：http://blog.csdn.net/abc__d/article/details/51790806

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        if (requestCode == 1) {
            try {
                // 获得图片的uri
                Uri originalUri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                // 好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                // 按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String path = getImagePath(data.getData(),null);
                Toast.makeText(AddActivity.this,path,Toast.LENGTH_SHORT).show();
                insertImg(path);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AddActivity.this, "图片插入失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //region 插入图片
    private void insertImg(String path){
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path,tagPath);
            insertPhotoToEditText(ss);
            et_content.append("\n");
            Log.d("YYPT", et_content.getText().toString());
        }
    }
    //endregion

    //region 将图片插入到EditText中
    private void insertPhotoToEditText(SpannableString ss){
        Editable et = et_content.getText();
        int start = et_content.getSelectionStart();
        et.insert(start,ss);
        et_content.setText(et);
        et_content.setSelection(start+ss.length());
        et_content.setFocusableInTouchMode(true);
        et_content.setFocusable(true);
    }
    //endregion

    private SpannableString getBitmapMime(String path,String tagPath) {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径

        int width = ScreenUtils.getScreenWidth(AddActivity.this);
        int height = ScreenUtils.getScreenHeight(AddActivity.this);


        Bitmap bitmap = ImageUtils.getSmallBitmap(path,width,480);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }



    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //  通过 Uri 和 selection 来获取真实图片的路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToNext()){
                //  MediaStore.Images.Media.insertImage —— 得到保存图片的原始路径
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }




    //重写startActivityForResult中的onActivityResult方法
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK){
//            //取得数据
//            Uri uri = data.getData();
//            ContentResolver cr = AddActivity.this.getContentResolver();
//            Bitmap bitmap = null;
//            Bundle extras = null;
//            //如果是选择照片
//            if(requestCode == 1){
//
//                try {
//                    //将对象存入Bitmap中
//                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//
//
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//
//            int imgWidth = bitmap.getWidth();
//            int imgHeight = bitmap.getHeight();
//            double partion = imgWidth*1.0/imgHeight;
//            double sqrtLength = Math.sqrt(partion*partion + 1);
//            //新的缩略图大小
//            double newImgW = 480*(partion / sqrtLength);
//            double newImgH = 480*(1 / sqrtLength);
//            float scaleW = (float) (newImgW/imgWidth);
//            float scaleH = (float) (newImgH/imgHeight);
//
//            Matrix mx = new Matrix();
//            //对原图片进行缩放
//            mx.postScale(scaleW, scaleH);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
//            final ImageSpan imageSpan = new ImageSpan(this,bitmap);
//            SpannableString spannableString = new SpannableString();
//            spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
//            //光标移到下一行
//            et_content.append("\n");
//            Editable editable = et_content.getEditableText();
//            int selectionIndex = et_content.getSelectionStart();
//            spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
//
//            //将图片添加进EditText中
//            editable.insert(selectionIndex, spannableString);
//            //添加图片后自动空出两行
//            et_content.append("\n\n");
//        }
//    }

}