package com.example.notebook.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notebook.R;
import com.example.notebook.db.MyDateBaseHelper;
import com.example.notebook.entity.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btn_add;
    private ListView listview_note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btn_add = findViewById(R.id.btn_add);
        listview_note = findViewById(R.id.listview_note);

        //用于存放从数据库中读出来的日记
        ArrayList<Note> notes = new ArrayList<Note>();
        //获得可读SQLiteDatabase对象
        MyDateBaseHelper dbHelper = new MyDateBaseHelper(this);

        //设置listview的adapter
        NoteBaseAdapter noteBaseAdapter = new NoteBaseAdapter(notes,this,R.layout.note_item);
        listview_note.setAdapter(noteBaseAdapter);

        //按钮点击事件
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        //单机查询，进行修改
        listview_note.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,com.example.notebook.activity.EditActivity.class);
                Note note = (Note) listview_note.getItemAtPosition(position);
                intent.putExtra("Title",note.getTitle().trim());
                intent.putExtra("Content",note.getContent().trim());
                intent.putExtra("Time",note.getTime().trim());
                intent.putExtra("Id",note.getId().toString().trim());
                intent.putExtra("Writer",note.getWriter().trim());
                startActivity(intent);
            }
        });

        //长按删除
        listview_note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("删除日记");
                dialog.setMessage("是否要删除此日记？");
                //通过back取消
                dialog.setCancelable(true);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Note note = (Note) listview_note.getItemAtPosition(position);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("Note", "Id = ?", new String[]{note.getId().toString()});
                        db.close();
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        notes.remove(position);
                        noteBaseAdapter.notifyDataSetChanged();

                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
                return true;
            }
        });


        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        //查询数据库中的内容
        Cursor cursor = sqLiteDatabase.query("Note",null,null,null,null,null,null);
        //是否查询到数据
        if(cursor.moveToFirst()){
            Note note;
            //是否是最后一个
            while(!cursor.isAfterLast()){
                note = new Note();
                note.setId(Integer.valueOf(cursor.getString(0)));
                note.setTitle(cursor.getString(1));
                note.setContent(cursor.getString(2));
                note.setTime(cursor.getString(3));
                note.setWriter(cursor.getString(4));
                notes.add(note);
                //移动到下一行数据
                cursor.moveToNext();
            }
        }
        cursor.close();
        dbHelper.close();



    }

    class NoteBaseAdapter extends BaseAdapter{
        private List<Note> notes;
        private Context context;
        private int layoutId;

        public NoteBaseAdapter(List<Note> notes, Context context, int layoutId) {
            this.notes = notes;
            this.context = context;
            this.layoutId = layoutId;
        }

        @Override
        public int getCount() {
            if(notes.size() > 0){
                return notes.size();
            }else{
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return notes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(
                        getApplicationContext()).inflate(R.layout.note_item, parent,
                        false);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.content = convertView.findViewById(R.id.tv_content);
                viewHolder.time = (TextView) convertView.findViewById(R.id.tv_time);
                viewHolder.writer = (TextView) convertView.findViewById(R.id.tv_writer);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String title = notes.get(position).getTitle();
            String content = notes.get(position).getContent();
            viewHolder.title.setText(title);
            viewHolder.content.setText(content);
            viewHolder.time.setText(notes.get(position).getTime());
            viewHolder.writer.setText(notes.get(position).getWriter());
            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView content;
        TextView time;
        TextView writer;
    }

}