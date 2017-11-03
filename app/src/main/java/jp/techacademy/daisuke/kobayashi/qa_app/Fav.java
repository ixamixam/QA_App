package jp.techacademy.daisuke.kobayashi.qa_app;

// モデルクラス
import java.io.Serializable;

public class Fav implements Serializable {
    private String mFav;

    public Fav(String fav) {
        mFav = fav;
    }

    public String getFav() {
        return mFav;
    }
}
