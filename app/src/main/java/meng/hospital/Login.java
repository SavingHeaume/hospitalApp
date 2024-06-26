package meng.hospital;

import android.content.Intent;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);

    username_ET = findViewById(R.id.usernameEditText);
    password_ET = findViewById(R.id.passwordEditText);
    login_Btn = findViewById(R.id.loginButton);

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
}