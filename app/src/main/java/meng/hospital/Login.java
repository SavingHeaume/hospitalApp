package meng.hospital;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import meng.hospital.patientFragment.AppointmentFragment;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {
  private EditText username_ET;
  private EditText password_ET;
  private Button login_Btn;

  SharedPreferences loginPreferences;
  SharedPreferences.Editor loginEditor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);

    username_ET = findViewById(R.id.usernameEditText);
    password_ET = findViewById(R.id.passwordEditText);
    login_Btn = findViewById(R.id.loginButton);

    load_name_and_pwd();

    login_Btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        login();
      }
    });
  }

  private void login() {
    // 获取用户输入的用户名和密码
    String username = username_ET.getText().toString().trim();
    String password = password_ET.getText().toString().trim();

    // 检查用户名和密码是否为空
    if (username.isEmpty() || password.isEmpty()) {
      Toast.makeText(Login.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
      return;
    }

    save_name_and_pwd(username, password);

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // 创建包含用户输入的JSON对象
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("username", username);
          jsonObject.put("password", password);
          String json = jsonObject.toString();

          OkHttpClient client = new OkHttpClient();
          MediaType JSON = MediaType.parse("application/json; charset=utf-8");
          RequestBody body = RequestBody.create(JSON, json);

          Request request = new Request.Builder()
                  .url("http://10.129.124.217:8088/login")
                  .post(body)
                  .build();

          Response response = client.newCall(request).execute();

          String responseString = response.body().string();
          JSONObject responseJson = new JSONObject(responseString);
          String message = responseJson.getString("message");
          char lastChar = message.charAt(message.length() - 1);

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              switch (lastChar) {
                case '1':
                  Toast.makeText(Login.this, "管理员登录成功", Toast.LENGTH_SHORT).show();

                  break;
                case '2':
                  Toast.makeText(Login.this, "医生登录成功", Toast.LENGTH_SHORT).show();
                  break;
                case '3':
                  Toast.makeText(Login.this, "患者登录成功", Toast.LENGTH_SHORT).show();
                  Intent intent = new Intent(Login.this, Patient.class);
                  startActivity(intent);
                  break;
                default:
                  Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                  break;
              }
            }
          });

        } catch (Exception e) {
          e.printStackTrace();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(Login.this, "发送失败", Toast.LENGTH_SHORT).show();
            }
          });
        }
      }
    }).start();
  }

  private void load_name_and_pwd() {
    loginPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
    username_ET.setText(loginPreferences.getString("userName", null));
    password_ET.setText(loginPreferences.getString("userPwd", null));
  }

  private void save_name_and_pwd(String userName, String userPwd) {
    loginEditor = loginPreferences.edit();

    loginEditor.putString("userName", userName);
    loginEditor.putString("userPwd", userPwd);

    loginEditor.commit();
  }
}