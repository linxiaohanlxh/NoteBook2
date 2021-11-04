package com.example.notebook.entity;

public class Note {
    private Integer id;
    private String title;
    private String content;
    private String time;
    private String writer;

    public Note(Integer id, String title, String content, String time,String writer) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
        this.writer = writer;
    }

    public Note() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }
}
