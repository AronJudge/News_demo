package com.xiangxue.network.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

 
public class TecentBaseResponse {
    @SerializedName("showapi_res_code")
    @Expose
    public Integer showapiResCode;
    @SerializedName("showapi_res_error")
    @Expose
    public String showapiResError;
}
