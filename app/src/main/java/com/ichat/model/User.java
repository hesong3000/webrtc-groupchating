package com.ichat.model;

/**
 * Created by fwj on 2016/7/13.
 */
public class User implements Cloneable{
    private String account;         //登录账户名
    private String password;       //登录密码
    private String userID;         //唯一标识ID
    private String name;           //昵称或用户名称
    private String age;           //年龄
    private String sex;           //性别 0-男，1-女，2-未知
    private String Avatar;       //头像名称
    private String fileFullname;//zdd 头像的默认存储路径
    private String sign;         //个性签名

    public Object clone()
    {
        Object o=null;
        try {
            o=(User)super.clone();                    //Object 中的clone()识别出你要复制的是哪一个对象。
        } catch(CloneNotSupportedException e) {
            System.out.println(e.toString());
        }
        return o;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileFullname() {
        return fileFullname;
    }

    public void setFileFullname(String fileFullname) {
        this.fileFullname = fileFullname;
    }
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "User{" +
                "account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", sex='" + sex + '\'' +
                ", Avatar='" + Avatar + '\'' +
                ", fileFullname='" + fileFullname + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }

    public String transSex(String vSex){
        String sSex = "";
//        switch (vSex){
//            case "0":sSex = "男";break;
//            case "1":sSex = "女";break;
//           default:sSex = "未知";break;
//        }
        return sSex;
    }
}
