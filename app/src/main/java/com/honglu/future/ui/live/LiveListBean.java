package com.honglu.future.ui.live;

import java.io.Serializable;

//任务的bean类
public class LiveListBean implements Serializable{

   public boolean isLive(){//是否正在直播
       return false;
   }
    public String liveNum;//在线人数
    public String liveImg;//直播图片
    public String liveTitle;//直播标题
    public String liveDes;//直播描述
    public String liveTeacher;//直播老师
    public String liveTeacherDes;//直播老师简介
    public String liveTeacherICon;//直播老师的头像
    public boolean isFollow(){
        return false;
    }
}
