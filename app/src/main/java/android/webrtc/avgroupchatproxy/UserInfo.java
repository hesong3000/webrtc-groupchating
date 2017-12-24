package android.webrtc.avgroupchatproxy;

import java.io.Serializable;

/**
 * Created by kai on 2016/11/12.
 */
public class UserInfo implements Serializable{

    public UserInfo(){
        userID = "";
        userName = "";
        userAvatar = "";
        userState = "";
        userSSRC_A = 0;
        userSSRC_V = 0;
        mediaIP = "";
        audioPort = 0;
        videoPort = 0;
    }

    public UserInfo(UserInfo userInfo){
        this.userID = userInfo.getUserID();
        this.userName = userInfo.getUserName();
        this.userAvatar = userInfo.getUserAvatar();
        this.userState = userInfo.getUserState();
        this.userSSRC_A = userInfo.getUserSSRC_A();
        this.userSSRC_V = userInfo.getUserSSRC_V();
        this.mediaIP = userInfo.getMediaIP();
        this.audioPort = userInfo.getAudioPort();
        this.videoPort = userInfo.getVideoPort();
    }

    public void updateUserInfo(UserInfo userInfo){
        this.userID = userInfo.getUserID();
        this.userName = userInfo.getUserName();
        this.userAvatar = userInfo.getUserAvatar();
        this.userState = userInfo.getUserState();
        this.userSSRC_A = userInfo.getUserSSRC_A();
        this.userSSRC_V = userInfo.getUserSSRC_V();
        this.mediaIP = userInfo.getMediaIP();
        this.audioPort = userInfo.getAudioPort();
        this.videoPort = userInfo.getVideoPort();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserState() {
        return userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public int getUserSSRC_A() {
        return userSSRC_A;
    }

    public void setUserSSRC_A(int userSSRC_A) {
        this.userSSRC_A = userSSRC_A;
    }

    public int getUserSSRC_V() {
        return userSSRC_V;
    }

    public void setUserSSRC_V(int userSSRC_V) {
        this.userSSRC_V = userSSRC_V;
    }

    public String getMediaIP() {
        return mediaIP;
    }

    public void setMediaIP(String mediaIP) {
        this.mediaIP = mediaIP;
    }

    public int getAudioPort() {
        return audioPort;
    }

    public void setAudioPort(int audioPort) {
        this.audioPort = audioPort;
    }

    public int getVideoPort() {
        return videoPort;
    }

    public void setVideoPort(int videoPort) {
        this.videoPort = videoPort;
    }

    private String userID;
    private String userName;
    private String userAvatar;
    private String userState;
    private int userSSRC_A;
    private int userSSRC_V;
    private String mediaIP;
    private int audioPort;
    private int videoPort;
}
