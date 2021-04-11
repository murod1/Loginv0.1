package com.example.loginv01;

import android.webkit.CookieSyncManager;

public class Login {
    import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

    public class Login extends Activity {

        String getDeviceID; // 스마트기기의 장치 고유값
        ProgressDialog dialog = null;
        EditText etId;
        EditText etPw;

        String loginID;
        String loginPW;
        CheckBox autologin;
        Boolean loginChecked;
        List<NameValuePair> params;
        public SharedPreferences settings;
        CookieManager cookieManager;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);

            // ActionBar 제거하기
            ActionBar actionbar = getActionBar();
            actionbar.hide();

            // 네트워크 연결상태 체크
            if(NetworkConnection() == false){
                NotConnected_showAlert();
            }

            etId = (EditText) findViewById(R.id.login_id_edit);
            etPw = (EditText) findViewById(R.id.login_pw_edit);
            autologin = (CheckBox) findViewById(R.id.autologinchk);

            settings = getSharedPreferences("settings",    Activity.MODE_PRIVATE);
            loginChecked = settings.getBoolean("LoginChecked", false);
            if (loginChecked) {
                etId.setText(settings.getString("loginID", ""));
                etPw.setText(settings.getString("loginPW", ""));
                autologin.setChecked(true);
            }

            if(!settings.getString("loginID", "").equals("")) etPw.requestFocus();

            CookieSyncManager.createInstance(this);
            cookieManager = CookieManager.getInstance();
            CookieSyncManager.getInstance().startSync();

            Button submit = (Button) findViewById(R.id.login_btn);
            submit.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View v) {
                    dialog = ProgressDialog.show(Login.this, "", "Validating user...", true);
                    new Thread(new Runnable() {
                        public void run() {
                            login();
                        }

                    }).start();
                }

            });

        }

        void login() {
            try {
                loginID = etId.getText().toString().trim();
                loginPW = etPw.getText().toString().trim();

                // 단말기의 ID 정보를 얻기 위해서는 READ_PHONE_STATE 권한이 필요
                TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephony.getDeviceId() != null){
                    getDeviceID = mTelephony.getDeviceId();  // 스마트폰 기기 정보
                } else {
                    getDeviceID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
                }

                String postURL = Value.IPADDRESS + "/loginChk.php";
                HttpPost post = new HttpPost(postURL);

                // 전달할 인자들
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("loginID", loginID));
                params.add(new BasicNameValuePair("loginPW", loginPW));
                params.add(new BasicNameValuePair("deviceID", getDeviceID));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                post.setEntity(ent);

                HttpClient httpclient = new DefaultHttpClient();

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                final String responsePost = httpclient.execute(post, responseHandler);

                System.out.println("DeviceID : " + getDeviceID);
                System.out.println("Response : " + responsePost);

                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.dismiss();
                    }
                });

                if(responsePost.equalsIgnoreCase("Login Success")){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Login.this,"Login Success", Toast.LENGTH_SHORT).show();
                        }
                    });

                    List<Cookie> cookies = ((DefaultHttpClient)httpclient).getCookieStore().getCookies();
                    if (!cookies.isEmpty()) {
                        for (int i = 0; i < cookies.size(); i++) {
                            String cookieString = cookies.get(i).getName() + "="
                                    + cookies.get(i).getValue();
                            Log.e("PHP_setCookie", cookieString);
                            cookieManager.setCookie(Value.IPADDRESS, cookieString);
                        }
                    }
                    Thread.sleep(500);

                    startActivity(new Intent(this.getApplicationContext(), MainActivity.class));
                    finish(); // finish()를 호출해서 Activity를 없애줌

                } else if(responsePost.equalsIgnoreCase("Phone Dismatch")){
                    deviceDismatch_showAlert();
                } else {
                    showAlert();
                }
            } catch(Exception e) {
                dialog.dismiss();
                System.out.println("Exception : " + e.getMessage());
            }

        }

        public void onStop(){
99999999999999999999999
            super.onStop();

            if (autologin.isChecked()) {
                settings = getSharedPreferences("settings",Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("loginID", loginID);
                editor.putString("loginPW", loginPW);
                editor.putBoolean("LoginChecked", true);

                editor.commit();
            } else {

                settings = getSharedPreferences("settings",    Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();
            }

        }

        public void deviceDismatch_showAlert(){
            Login.this.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle("등록단말 불일치");
                    builder.setMessage("최초 등록된 단말기가 아닙니다.\n" + "관리자에게 문의하여 단말기 변경신청을 하시기 바랍니다.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        public void showAlert(){
            Login.this.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle("로그인 에러");
                    builder.setMessage("로그인 정보가 일치하지 않습니다.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        private void NotConnected_showAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("네트워크 연결 오류");
            builder.setMessage("사용 가능한 무선네트워크가 없습니다.\n" + "먼저 무선네트워크 연결상태를 확인해 주세요.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish(); // exit

                            android.os.Process.killProcess(android.os.Process.myPid() );
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }

        private boolean NetworkConnection() {
            ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
            boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
            boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
            boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
                return true;
            }else{
                return false;
            }
        }


        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if( keyCode == KeyEvent.KEYCODE_BACK ) {
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Quit").setMessage("어플을 종료하시겠습니까?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();

                        android.os.Process.killProcess(android.os.Process.myPid() );
                    }
                }).setNegativeButton( "No", null ).show();

                return true;
            }

            return super.onKeyDown(keyCode, event);
        }

        @Override
        protected void onResume()
        {
            super.onResume();
            CookieSyncManager.getInstance().startSync();
        }

        @Override
        protected void onPause()
        {
            super.onPause();
            CookieSyncManager.getInstance().stopSync();
        }
    }


}
