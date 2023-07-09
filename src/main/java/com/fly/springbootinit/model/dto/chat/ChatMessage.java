package com.fly.springbootinit.model.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 7039859481880518080L;
    private String AIMessage;
    private String userMessage;
    private Date updateTime;
    private String AIAvatar;
    private String userAvatar;
    private String userName;
    private String AIName;
}
