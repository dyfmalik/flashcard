package kr.co.bit.osf.flashcard.db;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("cid")
    public int cid;
    @SerializedName("cfront")
    public String cfront;
    @SerializedName("cback")
    public String cback;
    @SerializedName("description")
    public String description;
    @SerializedName("url")
    public String url;




}
