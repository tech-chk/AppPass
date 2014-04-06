package com.chklab.apppass.app.models;

/**
 * Created by 010144 on 14/03/30.
 */
public class UserInfo {
    // Start プロジェクトで共通のインスタンスにする処理（シングルトンパターン）
    private static UserInfo instance = new UserInfo();

    private UserInfo(){}

    public static UserInfo getInstance() {
        return instance;
    }
    //  End  プロジェクトで共通のインスタンスにする処理（シングルトンパターン）

    private String birthday = "";
    private String firstName = "";
    private String lastName = "";
    private String sex = "";
    private String userId = "";
    private String userName = "";
    private int checkinSpotId = -1;

    /**
     *
     * @return
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     *
     * @param birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     */
    public String getSex() {
        return sex;
    }

    /**
     *
     * @param sex
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     *
     * @return
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return
     */
    public int getCheckinSpotId() {
        return checkinSpotId;
    }

    /**
     *
     * @param checkinSpotId
     */
    public void setCheckinSpotId(int checkinSpotId) {
        this.checkinSpotId = checkinSpotId;
    }

}
