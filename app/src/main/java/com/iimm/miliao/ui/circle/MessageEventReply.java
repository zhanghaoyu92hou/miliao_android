package com.iimm.miliao.ui.circle;

import com.iimm.miliao.bean.circle.Comment;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventReply {
    public final String event;
    public final Comment comment;
    public final int id;
    public final String name;

    public MessageEventReply(String event, Comment comment, int id, String name) {
        this.event = event;
        this.comment = comment;
        this.id = id;
        this.name = name;
    }
}