package jp.techacademy.daisuke.kobayashi.qa_app;


// モデルクラス
import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    private String mTitle;
    private String mBody;
    private String mName;
    private String mUid;
    private String mQuestionUid;
    private int mGenre;
    private String mFav;
    private byte[] mBitmapArray;
    private ArrayList<Answer> mAnswerArrayList;

    // fav入替用
    public String setFav(String fav) {
        mFav = fav;
        return mFav;
    }

    // お気に入り判定用
    public String getFav() {
        return mFav;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public int getGenre() {
        return mGenre;
    }

    public byte[] getImageBytes() {
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return mAnswerArrayList;
    }

    public Question(String title, String body, String name, String uid, String questionUid, int genre, String fav, byte[] bytes, ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mFav = fav;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
    }
}