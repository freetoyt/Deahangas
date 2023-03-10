package com.gms.app.barcode;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LoginActivity extends AppCompatActivity {

    private EditText et_id, et_pass;
    private Button btn_login;
    private CheckBox chk_login;
    private String shared = "file";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = (EditText)findViewById(R.id.et_id);
        et_pass = (EditText)findViewById(R.id.et_pass);

        btn_login = (Button)findViewById(R.id.btn_login);
        chk_login = (CheckBox)findViewById(R.id.chk_login);

        //SharedPreferences 로그인 정보 유무 확인
        SharedPreferences sharedPreferences = getSharedPreferences(shared,0);
        String value = sharedPreferences.getString("id", "");

        if(value != null && value.length() > 0) {
            Toast.makeText(getApplicationContext(),"로그인이 되어 있습니다,",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            //intent.putExtra("id",id);
            //intent.putExtra("pw", name);
            startActivity(intent);
        }

        // 네트웍 상태체크
        ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService( LoginActivity.this.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //WIFI에 연결됨
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //LTE(이동통신망)에 연결됨
            }
        } else {
            // 연결되지않음
            AlertDialog.Builder builder
                    = new AlertDialog.Builder(LoginActivity.this,AlertDialog.THEME_HOLO_DARK);
            builder .setTitle("대한특수가스")
                    .setMessage("인터넷이 연결되지 않았습니다 ")
                    .setPositiveButton("확인", null);
            AlertDialog ad = builder.create();

            ad.show();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = et_id.getText().toString();
                String pw = et_pass.getText().toString();

                String url = getString(R.string.host_name)+"api/loginAction.do?id="+id+"&pw="+pw;//AA315923";

                // AsyncTask를 통해 HttpURLConnection 수행.
                NetworkTask networkTask = new NetworkTask(url, null);
                networkTask.execute();
                //data를 json으로 변환
                //JSONObject obj = new JSONObject(result.getContents());
            }
        });
    }


    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            Log.i("LoginActivity doInBackground","rseult="+result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("LoginActivity onPostExecute","s="+s);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            //tv_result.setText(s);
            boolean success = false;

            try {

                if(s == null || s.length() < 10) {
                    Toast.makeText(getApplicationContext(),"계정정보를 확인해주세요,",Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject jsonObject = new JSONObject(s);
                //Log.d("LoginActivity", "jsonObject ="+jsonObject.toString());
                success = jsonObject.getBoolean("success");
                //Log.e("LoginActivity", "success ="+success);
                if(success) {
                    String id = jsonObject.getString("userId");
                    String name = URLDecoder.decode(jsonObject.getString("userNm"),"UTF-8");

                    //SharedPreferences 저장
                    if(chk_login.isChecked()) {
                        SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        //String value = id.getText().toString();
                        editor.putString("id", id);
                        editor.putString("name", name);
                        editor.commit();
                    }
                    Toast.makeText(getApplicationContext(),"로그인이 성공하였습니다,",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("id",id);
                    intent.putExtra("pw", name);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"로그인이 실패하였습니다,",Toast.LENGTH_SHORT).show();
                    return;

                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException eu) {
                Toast.makeText(getApplicationContext(),"서버의 상태를 확인해주세요,",Toast.LENGTH_SHORT).show();
                eu.printStackTrace();
            }

        }
    }
}
