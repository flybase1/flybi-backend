package com.fly.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI聊天
 * @TableName aichat
 */
@TableName(value ="aichat")
@Data
public class Aichat implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 用户头像
     */
    @TableField(value = "userAvatar")
    private String userAvatar;

    /**
     * 用户发送
     */
    @TableField(value = "userMessage")
    private String userMessage;

    /**
     * AI消息
     */
    @TableField(value = "AIMessage")
    private String AIMessage;

    /**
     * 
     */
    @TableField(value = "AIAvatar")
    private String AIAvatar;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    private String userName;

    private String AIName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Aichat other = (Aichat) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserAvatar() == null ? other.getUserAvatar() == null : this.getUserAvatar().equals(other.getUserAvatar()))
            && (this.getUserMessage() == null ? other.getUserMessage() == null : this.getUserMessage().equals(other.getUserMessage()))
            && (this.getAIMessage() == null ? other.getAIMessage() == null : this.getAIMessage().equals(other.getAIMessage()))
            && (this.getAIAvatar() == null ? other.getAIAvatar() == null : this.getAIAvatar().equals(other.getAIAvatar()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserAvatar() == null) ? 0 : getUserAvatar().hashCode());
        result = prime * result + ((getUserMessage() == null) ? 0 : getUserMessage().hashCode());
        result = prime * result + ((getAIMessage() == null) ? 0 : getAIMessage().hashCode());
        result = prime * result + ((getAIAvatar() == null) ? 0 : getAIAvatar().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", userAvatar=").append(userAvatar);
        sb.append(", userMessage=").append(userMessage);
        sb.append(", AIMessage=").append(AIMessage);
        sb.append(", AIAvatar=").append(AIAvatar);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}