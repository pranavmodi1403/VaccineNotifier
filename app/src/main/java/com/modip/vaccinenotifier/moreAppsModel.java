package com.modip.vaccinenotifier;

public class moreAppsModel {
    private int app_id;
    private String app_name;
    private String app_desc;
    private String app_logo_url;
    private String app_playstore_url;

    public moreAppsModel(int app_id, String app_name, String app_desc, String app_logo_url, String app_playstore_url) {
        this.app_id = app_id;
        this.app_name = app_name;
        this.app_desc = app_desc;
        this.app_logo_url = app_logo_url;
        this.app_playstore_url = app_playstore_url;
    }

    public int getApp_id() {
        return app_id;
    }

    public void setApp_id(int app_id) {
        this.app_id = app_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_desc() {
        return app_desc;
    }

    public void setApp_desc(String app_desc) {
        this.app_desc = app_desc;
    }

    public String getApp_logo_url() {
        return app_logo_url;
    }

    public void setApp_logo_url(String app_logo_url) {
        this.app_logo_url = app_logo_url;
    }

    public String getApp_playstore_url() {
        return app_playstore_url;
    }

    public void setApp_playstore_url(String app_playstore_url) {
        this.app_playstore_url = app_playstore_url;
    }
}
