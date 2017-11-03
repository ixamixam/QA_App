package jp.techacademy.daisuke.kobayashi.qa_app;

import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QuestionDetailListAdapter extends BaseAdapter {
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavReference;

    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }

            String body = mQustion.getBody();
            String name = mQustion.getName();
            final String questionUid = mQustion.getQuestionUid();
            final String fav = mQustion.getFav();

            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFavReference = mDatabaseReference.child(Const.ContentsPATH).child(questionUid);
            //final String fav = mFavTemp.toString();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            //-------------------------------------------------
            final Map<String, Object> data = new HashMap<>();

            //ログインしてるかどうかの判定（してなかったらボタンは表示せず何もしない
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
            } else {
                // ここから追加分、お気に入りボタン
                final ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewFav);

                // お気に入りに入っていればfav01
                // なければfav00をセット
                if (fav.equals("no")) {
                    imageView1.setImageResource(R.drawable.fav00);
                } else {
                    imageView1.setImageResource(R.drawable.fav01);
                }

                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String questionUid = mQustion.getQuestionUid();
                        String userID = user.getUid();
                        String questionGenre = String.valueOf(mQustion.getGenre());
                        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();

                        DatabaseReference genreRefContents = dataBaseReference.child(Const.ContentsPATH);
                        DatabaseReference genreRefFav = dataBaseReference.child(Const.UsersFavPATH);

                        //genreRef.child(questionUid).updateChildren(data);


                        if (fav.equals("no")) {
                            imageView1.setImageResource(R.drawable.fav01);
                            data.put("fav", "yes");

                            //fav書き換え
                            genreRefContents.child(questionGenre).child(questionUid).updateChildren(data);
                            //genreRefFav.child(userID).child(questionUid).setValue();
                            genreRefFav.child(userID).child(questionUid).updateChildren(data);


                        } else {
                            imageView1.setImageResource(R.drawable.fav00);
                            data.put("fav", "no");
                            genreRefContents.child(questionGenre).child(questionUid).updateChildren(data);
                            genreRefFav.child(userID).child(questionUid).removeValue();
                            //genreRefFav.child(userID).child(questionUid).updateChildren(data);

                        }
                        Log.d("test:", fav);
                        Log.d("test:", "test");
                    }
                });
            }
            //-------------------------------------------------

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
        }else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }
        return convertView;
    }
}