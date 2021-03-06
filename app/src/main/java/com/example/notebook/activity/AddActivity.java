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

        //??????????????????
        tv_time.setText(getTime());

        //sqlite?????????
        MyDateBaseHelper myDateBaseHelper = new MyDateBaseHelper(this);

        //??????????????????
        //????????????
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????
                SQLiteDatabase sqLiteDatabase = myDateBaseHelper.getWritableDatabase();
                ContentValues note = new ContentValues();

                String title = et_title.getText().toString();
                String content = et_content.getText().toString();
                String time = tv_time.getText().toString();
                String writer = et_writer.getText().toString();
                //????????????????????????toast????????????
                if (et_title.getText().toString().equals("")) {
                    Toast.makeText(AddActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                //????????????????????????toast????????????
                if (et_content.getText().toString().equals("")) {
                    Toast.makeText(AddActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                note.put("Title", title);
                note.put("Content", content);
                note.put("Time", time);
                //??????????????????????????????????????????unknown
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
                //???????????????toast????????????
                Toast.makeText(AddActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                myDateBaseHelper.close();
                //??????????????????
                Intent intent = new Intent(AddActivity.this, com.example.notebook.activity.MainActivity.class);
                startActivity(intent);
            }
        });

        //????????????
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
                Intent intent = new Intent(AddActivity.this, com.example.notebook.activity.MainActivity.class);
                startActivity(intent);
            }
        });

        //???????????????????????????
        btn_addPictureByAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGallery();
            }
        });
    }

    //??????????????????
    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = sdf.format(date);
        return str;
    }


    //region ????????????
    private void callGallery(){
        Intent getAlbum = new Intent(Intent.ACTION_PICK);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,1);
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //???????????????http://blog.csdn.net/abc__d/article/details/51790806

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        // ?????????????????????ContentProvider??????????????? ????????????ContentResolver??????
        ContentResolver resolver = getContentResolver();
        if (requestCode == 1) {
            try {
                // ???????????????uri
                Uri originalUri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                // ?????????android????????????????????????????????????????????????Android??????
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                // ?????????????????? ????????????????????????????????????????????????
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // ????????????????????? ???????????????????????????????????????????????????
                cursor.moveToFirst();
                // ???????????????????????????????????????
                String path = getImagePath(data.getData(),null);
                Toast.makeText(AddActivity.this,path,Toast.LENGTH_SHORT).show();
                insertImg(path);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AddActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //region ????????????
    private void insertImg(String path){
        String tagPath = "<img src=\""+path+"\"/>";//?????????????????????<img>??????
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path,tagPath);
            insertPhotoToEditText(ss);
            et_content.append("\n");
            Log.d("YYPT", et_content.getText().toString());
        }
    }
    //endregion

    //region ??????????????????EditText???
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
        SpannableString ss = new SpannableString(tagPath);//??????????????????<img>?????????????????????

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
        //  ?????? Uri ??? selection ??????????????????????????????
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToNext()){
                //  MediaStore.Images.Media.insertImage ?????? ?????????????????????????????????
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }




    //??????startActivityForResult??????onActivityResult??????
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK){
//            //????????????
//            Uri uri = data.getData();
//            ContentResolver cr = AddActivity.this.getContentResolver();
//            Bitmap bitmap = null;
//            Bundle extras = null;
//            //?????????????????????
//            if(requestCode == 1){
//
//                try {
//                    //???????????????Bitmap???
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
//            //?????????????????????
//            double newImgW = 480*(partion / sqrtLength);
//            double newImgH = 480*(1 / sqrtLength);
//            float scaleW = (float) (newImgW/imgWidth);
//            float scaleH = (float) (newImgH/imgHeight);
//
//            Matrix mx = new Matrix();
//            //????????????????????????
//            mx.postScale(scaleW, scaleH);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
//            final ImageSpan imageSpan = new ImageSpan(this,bitmap);
//            SpannableString spannableString = new SpannableString();
//            spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
//            //?????????????????????
//            et_content.append("\n");
//            Editable editable = et_content.getEditableText();
//            int selectionIndex = et_content.getSelectionStart();
//            spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
//
//            //??????????????????EditText???
//            editable.insert(selectionIndex, spannableString);
//            //?????????????????????????????????
//            et_content.append("\n\n");
//        }
//    }

}