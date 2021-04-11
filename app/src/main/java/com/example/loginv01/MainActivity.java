import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;

    // 서버 정보를 파싱하기 위한 변수 선언
    String myJSON;
    private static final String TAG_RESULTS="result";
    private static final String TAG_UID = "uid";
    private static final String TAG_NAME = "name";
    private static final String TAG_Mobile ="mobile";

    JSONArray peoples = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ActionBar 제거하기
        ActionBar actionbar = getActionBar();
        actionbar.hide();

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new ListViewAdapter(this);

        // 서버에 있는 정보를 읽어다가 mAdapter.addItem 에 추가하는 과정
        getDbData(Value.IPADDRESS + "/mobile/get_json.php");

        mListView.setAdapter(mAdapter);

        TextView searchView = (TextView) findViewById(R.id.SearchView);
        TextView phonebookView = (TextView) findViewById(R.id.PhonebookView);

        searchView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "검색화면으로 이동합니다", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, Search_Item.class);
                startActivity(intent);
            }
        });

        phonebookView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "내 폰의 전화번호부를 가져옵니다", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, PhonebookActivity.class);
                startActivity(intent);
            }

        });

    }


    private void getDbData(String string) {
        class GetDataJSON extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];


                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // 세션 쿠키 전달
                    String cookieString = CookieManager.getInstance().getCookie(Value.IPADDRESS);

                    StringBuilder sb = new StringBuilder();

                    if(conn != null){ // 연결되었으면
                        //add request header
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                        if (cookieString != null) {
                            conn.setRequestProperty("Cookie", cookieString);
                            Log.e("PHP_getCookie", cookieString);
                        }
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(10000);
                        conn.setUseCaches(false);
                        conn.setDefaultUseCaches(false);
                        conn.setDoOutput(true); // POST 로 데이터를 넘겨주겠다는 옵션
                        //conn.setDoInput(true);

                        int responseCode = conn.getResponseCode();
                        System.out.println("GET Response Code : " + responseCode);
                        if(responseCode == HttpURLConnection.HTTP_OK){ // 연결 코드가 리턴되면
                            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            String json;
                            while((json = bufferedReader.readLine())!= null){
                                sb.append(json + "\n");
                            }
                        }
                        bufferedReader.close();
                    }
                    return sb.toString().trim();

                } catch(Exception e){
                    return new String("Exception: " + e.getMessage());
                }

            }

            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute(string);

    }

    protected void showList() {
        // 서버에서 읽어온 정보를 mAdapter 에 저장하고 화면에 출력
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String uid = c.getString(TAG_UID);
                String name = c.getString(TAG_NAME);
                String mobile = c.getString(TAG_Mobile);
                Drawable myIcon = getResources().getDrawable( R.drawable.ic_launcher );

                mAdapter.addItem(myIcon,uid,name,mobile);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class ViewHolder {
        public LinearLayout child_layout;
        public ImageView mImage;
        public Button childListBtn;
        public TextView name;
        public TextView mobile;
    }

    private class ListViewAdapter extends BaseAdapter {

        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                viewHolder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, parent, false);

                view.setBackgroundColor(0x00FFFFFF);
                view.invalidate();

                viewHolder.child_layout = (LinearLayout) view.findViewById(R.id.child_layout);
                viewHolder.mImage = (ImageView) view.findViewById(R.id.mImage);
                viewHolder.childListBtn = (Button ) view.findViewById(R.id.childListBtn);
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                viewHolder.mobile = (TextView) view.findViewById(R.id.mobile);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final ListData mData = mListData.get(position);

            if (mData.mImage != null) {
                viewHolder.mImage.setVisibility(View.VISIBLE);
                viewHolder.mImage.setImageDrawable(mData.mImage);
            } else {
                viewHolder.mImage.setVisibility(View.GONE);
            }

            viewHolder.childListBtn.setText(mData.uid);
            viewHolder.name.setText(mData.name);
            viewHolder.mobile.setText(mData.mobile);

            viewHolder.childListBtn.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View v) {

                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(50);

                    AlertDialog showdialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(mData.name)
                            .setMessage(mData.mobile + " 통화하시겠습니까?")
                            .setPositiveButton("예",
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog,int which) {

                                            Intent i = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+ mData.mobile));
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                        }

                                    })
                            .setNegativeButton(
                                    "아니오",
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog,int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                    showdialog.show();
                }

            });

            return view;
        }

        public void addItem(Drawable icon, String uid, String name, String mobile){
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.mImage = icon;
            addInfo.uid = uid;
            addInfo.name = name;
            addInfo.mobile = mobile;

            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
            mAdapter.notifyDataSetChanged();
        }


    }

    // Back 버튼을 누르면 어플 종료여부 확인 처리
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK ) {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Quit").setMessage("어플을 종료하시겠습니까?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which) {
                    moveTaskToBack(true); // 본 Activity finish후 다른 Activity가 뜨는 걸 방지.
                    finish();
                    //application 프로세스를 강제 종료
                    android.os.Process.killProcess(android.os.Process.myPid() );
                }
            }).setNegativeButton( "No", null ).show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}

