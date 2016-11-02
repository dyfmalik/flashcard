package kr.co.bit.osf.flashcard.db;

import android.os.Parcel;
import android.os.Parcelable;

public class CardDTO_ {
    private int id;
    private String name;
    private String imagePath;   // image full path
    private String imageName;   // drawable image name (only demo data)
    private int type;           // 0:user card, 1:demo card
    int seq;
    int boxId;                  // Box id

    public CardDTO_() { }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getBoxId() {
        return boxId;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }


}
