package com.example.notebook.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notebook.R;
import com.example.notebook.db.MyDateBaseHelper;
import com.example.notebook.entity.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {

    private Button btnSave;
    private Button btnCancel;
    private TextView showTime;
    private EditText showContent;
    private EditText showTitle;
    private EditText showWriter;

    private Note note;
    MyDateBaseHelper myDateBaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        init();
    }

    public void init() {
        myDateBaseHelper = new MyDateBaseHelper(this);
        btnCancel = findViewById(R.id.btn_return);
        btnSave = findViewById(R.id.btn_edit);
        showTime = findViewById(R.id.tv_time);
        showTitle = findViewById(R.id.et_title);
        showContent = findViewById(R.id.et_content);
        showWriter = findViewById(R.id.et_writer);

        Intent intent = this.getIntent();
        if (intent != null) {
            note = new Note();

            note.setTime(intent.getStringExtra("Time"));
            note.setTitle(intent.getStringExtra("Title"));
            note.setContent(intent.getStringExtra("Content"));
            note.setId(Integer.valueOf(intent.getStringExtra("Id")));
            note.setWriter(intent.getStringExtra("Writer"));

            showTime.setText(note.getTime());
            showTitle.setText(note.getTitle());
            showContent.setText(note.getContent());
            showWriter.setText(note.getWriter());
        }

        //input是获取将被解析的字符串
        String input = showContent.getText().toString();
        //将图片那一串字符串解析出来,即<img src=="xxx" />
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);

        //使用SpannableString了，这个不会可以看这里哦：http://blog.sina.com.cn/s/blog_766aa3810100u8tx.html#cmt_523FF91E-7F000001-B8CB053C-7FA-8A0
        SpannableString spannable = new SpannableString(input);
        while(m.find()){
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(EditActivity.this);
            int height = ScreenUtils.getScreenHeight(EditActivity.this);

            Bitmap bitmap = ImageUtils.getSmallBitmap(path,width,480);
            ImageSpan imageSpan = new ImageSpan(this, bitmap);
            spannable.setSpan(imageSpan,start,end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        showContent.setText(spannable);
        showContent.append("\n");
        //Log.d("YYPT_RGX_SUCCESS",content.getText().toString());

        //按钮点击事件
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase sqLiteDatabase = myDateBaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                String content = showContent.getText().toString();
                String title = showTitle.getText().toString();
                String writer = showWriter.getText().toString();

                values.put("Time", getTime());
                values.put("Title",title);
                values.put("Content",content);
                values.put("Writer",writer);

                sqLiteDatabase.update("Note",values,"Id=?",new String[]{note.getId().toString()});
                Toast.makeText(EditActivity.this,"修改成功",Toast.LENGTH_LONG).show();
                myDateBaseHelper.close();
                Intent intent = new Intent(EditActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = showContent.getText().toString();
                final String title = showTitle.getText().toString();
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle("提示框")
                        .setMessage("是否保存当前内容?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SQLiteDatabase sqLiteDatabase = myDateBaseHelper.getWritableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put("Time", getTime());
                                        values.put("Title",title);
                                        values.put("Content",content);
                                        sqLiteDatabase.update("Note",values,"Id=?",new String[]{note.getId().toString()});
                                        Toast.makeText(EditActivity.this,"修改成功",Toast.LENGTH_LONG).show();
                                        myDateBaseHelper.close();
                                        Intent intent = new Intent(EditActivity.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(EditActivity.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                }).show();
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

    private void initContent(){
    }
}