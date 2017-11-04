package jp.techacademy.daisuke.kobayashi.qa_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public int RESURT = 100;
    private Toolbar mToolbar;
    private int mGenre = 0;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mFavRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private ArrayList<Fav> mFavArrayList;

    private  ChildEventListener mEventListenerFavlist = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            if (map != null) {
                for(Object key : map.keySet()) {
                    String favKey = dataSnapshot.getKey();
                    //Log.d("fav",favKey);
                    Fav fav = new Fav(favKey);
                    mFavArrayList.add(fav);
                }
            }

            for(Fav fav : mFavArrayList){
                Log.d("addfav",fav.getFav());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    // QuestionsListAdapterにデータを設定
    // Firebaseからデータを取得する必要。データに追加・変化があった時に受け取るChildEventListenerを作成
    private ChildEventListener mEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // すでにリストにある場合は取得しない（お気に入り変更時用
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    return;
                }
            }


            // データ一通り設定
            HashMap map = (HashMap) dataSnapshot.getValue();

            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String fav = (String) map.get("fav");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            // Answerも,下の階層の取り方
            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {

                    // ここでさらに下の階層をtempに
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, fav, bytes, answerArrayList);
            mQuestionArrayList.add(question);

            // ここでリスト更新
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();

                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    // お気に入り用、コンテンツ全体を取得
    private ChildEventListener mEventListenerFav = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            /*
            for(Fav fav : mFavArrayList){
                Log.d("fabfirst",fav.getFav());
            }
            */

            // データ一通り設定
            HashMap qmap = (HashMap) dataSnapshot.getValue();
            if(qmap != null) {
                for (Object key : qmap.keySet()) {
                    for(Question question : mQuestionArrayList){
                        if(key.equals(question.getUid())){
                            return;
                        }
                    }

                    HashMap map = (HashMap) qmap.get((String) key);
                    String qkey = key.toString();
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String qfav = (String) map.get("fav");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    // Answerも,下の階層の取り方
                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object keya : answerMap.keySet()) {

                            // ここでさらに下の階層をtempに
                            HashMap temp = (HashMap) answerMap.get((String) keya);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // お気に入りに入ってるかどうか
                    for(Fav fav : mFavArrayList) {
                        Log.d("catchfav",fav.getFav());
                        if(fav.getFav().equals(qkey)) {
                            Question question = new Question(title, body, name, uid, qkey, mGenre, qfav, bytes, answerArrayList);
                            mQuestionArrayList.add(question);
                        }
                    }
                }
            }

            // ここでリスト更新
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();

                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent,RESURT);
                } else if (mGenre == 5) {
                    // お気に入りは動かさない
                    Snackbar.make(view, "お気に入りからは投稿できません。", Snackbar.LENGTH_LONG).show();
                } else {
                    // ジャンルを渡して質問作成画面を起動する

                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name){
            public void onDrawerOpened(View drawerView) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Menu menu = navigationView.getMenu();
                MenuItem menuItem1 = menu.findItem(R.id.nav_fav);
                if (user == null) {
                    menuItem1.setVisible(false);
                }else
                    menuItem1.setVisible(true);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                } else if (id == R.id.nav_fav) {
                    mToolbar.setTitle("お気に入り");
                    mGenre = 5;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                mFavArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                // 選択したジャンルにリスナーを登録する
                // リファレンス（データベースへの参照が空じゃなかったら
                if (mGenreRef != null) {
                    // 参照先から以前に登録されたイベントを削除
                    mGenreRef.removeEventListener(mEventListener);
                    mGenreRef.removeEventListener(mEventListenerFav);
                }

                // お気に入り一覧取得
                mFavRef = mDatabaseReference.child(Const.UsersFavPATH).child(user.getUid());
                mFavRef.addChildEventListener(mEventListenerFavlist);

                //if (mFavRef != null) {
                    // 参照先から以前に登録されたイベントを削除
                //    mFavRef.removeEventListener(mEventListenerFavlist);
                //}

                //mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                // クエリに書き換え

                // 条件分岐
                if (mGenre == 5) {
                    // お気に入りだったらお気に入りだけを表示
                    //Query mGenreRef = mDatabaseReference.child(Const.ContentsPATH).orderByChild("fav").equalTo("yes");

                    // ここをお気に入りに変える

                    if(mFavRef != null) {
                        mGenreRef = mDatabaseReference.child(Const.ContentsPATH);
                        mGenreRef.addChildEventListener(mEventListenerFav);
                    }

                } else {
                    // それ以外だったら各ジャンルのみを表示
                    //Query mGenreRef = mDatabaseReference.child(Const.ContentsPATH).orderByChild("genre").equalTo(String.valueOf(mGenre));
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    //mGenreRef = mDatabaseReference.child(Const.ContentsPATH);
                    mGenreRef.addChildEventListener(mEventListener);
                }
                //mGenreRef.addChildEventListener(mEventListener);
                return true;
            }
        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mFavArrayList = new ArrayList<Fav>();
        mAdapter.notifyDataSetChanged();


        // setOnItemClickListenerメソッドでリスナーを登録し、
        // リスナーの中で質問に相当するQuestionのインスタンスを渡してQuestionDetailActivityに遷移
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する

                int mFavFlag = 0;
                for (Fav fav:mFavArrayList) {
                    Log.d("Question",mQuestionArrayList.get(position).getQuestionUid());
                    Log.d("Fav",fav.getFav());
                    if (mQuestionArrayList.get(position).getQuestionUid().equals(fav.getFav())) {
                        mFavFlag = 1;
                    }
                }
                Log.d("intentflag",String.valueOf(mFavFlag));


                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                intent.putExtra("FavFlag", mFavFlag);
                startActivity(intent);

                // 画面遷移テスト
                //Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                //intent.putExtra("genre", mGenre);
                //startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //帰ってきたとき

    }
}
