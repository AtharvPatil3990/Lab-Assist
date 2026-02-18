package com.android.labassist;

public class UserInfo {
    private static String  orgCode, role, instituteName, department, registrationID, email, userName;

    private static boolean isInfoSaved = false;

    UserInfo(){}
    public static void saveUserInfo(String email, String orgCode, String role, String instituteName, String department, String registrationID, String userName){
        UserInfo.email = email;
        UserInfo.orgCode = orgCode;
        UserInfo.role = role;
        UserInfo.userName = userName;
        UserInfo.instituteName = instituteName;
        UserInfo.department = department;
        UserInfo.registrationID = registrationID;
        isInfoSaved = true;
    }
    public static void setIsUserInfoSet(boolean isTure){
        isInfoSaved = isTure;
    }

    public static boolean isUserInfoSaved() {
        return isInfoSaved;
    }

    public static String getUserName(){
        return userName;
    }
    public static String getDepartment() {
        return department;
    }

    public static  void setDepartment(String department) {
        UserInfo.department = department;
    }

    public static String getEmail() {
        return email;
    }

    public static void setUserName(String userName){
        UserInfo.userName = userName;
    }
    public static void setEmail(String email) {
        UserInfo.email = email;
    }

    public static String getInstituteName() {
        return instituteName;
    }

    public static void setInstituteName(String instituteName) {
        UserInfo.instituteName = instituteName;
    }

    public static String getOrgCode() {
        return orgCode;
    }

    public static void setOrgCode(String orgCode) {
        UserInfo.orgCode = orgCode;
    }

    public static String getRegistrationID() {
        return registrationID;
    }

    public static void setRegistrationID(String registrationID) {
        UserInfo.registrationID = registrationID;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        UserInfo.role = role;
    }
}