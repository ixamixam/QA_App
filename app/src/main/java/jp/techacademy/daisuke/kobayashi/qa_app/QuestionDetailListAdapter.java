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
    private DatabaseReference mFabReference;

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
            final String fab = mQustion.getFab();

            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFabReference = mDatabaseReference.child(Const.ContentsPATH).child(questionUid);
            //final String fab = mFabTemp.toString();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            //-------------------------------------------------
            final Map<String, Object> data = new HashMap<>();

            //ログインしてるかどうかの判定（してなかったらボタンは表示せず何もしない
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
            } else {
                // ここから追加分、お気に入りボタン
                final ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewFab);


                // ユーザーのお気に入り一覧を取得


                //お気に入りに入っていればfab01
                //なければfab00をセット
                if (fab.equals("no")) {
                    imageView1.setImageResource(R.drawable.fab00);
                } else {
                    imageView1.setImageResource(R.drawable.fab01);
                }

                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String questionUid = mQustion.getQuestionUid();
                        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference genreRef = dataBaseReference.child(Const.ContentsPATH);

                        if (fab.equals("no")) {
                            imageView1.setImageResource(R.drawable.fab01);
                            data.put("fab", "yes");
                            genreRef.child(questionUid).updateChildren(data);
                            //fab書き換え

                        } else {
                            imageView1.setImageResource(R.drawable.fab00);
                            data.put("fab", "no");
                            genreRef.child(questionUid).updateChildren(data);
                        }

                        Log.d("test:", fab);
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