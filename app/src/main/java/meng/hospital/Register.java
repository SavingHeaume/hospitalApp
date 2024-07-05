package meng.hospital;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class
Register extends AppCompatActivity {
  private EditText name_et_ = null;
  private EditText age_et_ = null;
  private AutoCompleteTextView gender_auto_complete_ = null;
  private EditText address_et_ = null;
  private EditText cert_id_et_ = null;
  private EditText username_et_ = null;
  private EditText password_et_ = null;
  private Button register_btn_ = null;
  private Button return_btn_ = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_register);

    name_et_ = findViewById(R.id.nameEditText);
    age_et_ = findViewById(R.id.ageEditText);
    gender_auto_complete_ = findViewById(R.id.genderAutoCompleteTextView);
    address_et_ = findViewById(R.id.addressEditText);

    cert_id_et_ = findViewById(R.id.certIdEditText);
    username_et_ = findViewById(R.id.usernameEditText);
    password_et_ = findViewById(R.id.passwordEditText);
    register_btn_ = findViewById(R.id.registerButton);
    return_btn_ = findViewById(R.id.returnButton);

    String[] genders = new String[]{"男", "女"};
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
    gender_auto_complete_.setAdapter(adapter);

    register_btn_.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validateInputs()) {
          return;
        }
        PostPatient();
      }
    });

    return_btn_.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
      }
    });
  }

  private void PostPatient() {
    String name = name_et_.getText().toString();
    String age = age_et_.getText().toString();
    String gender = gender_auto_complete_.getText().toString();
    int sex;
    if (gender.equals("男")) {
      sex = 1;
    } else {
      sex = 0;
    }
    String address = address_et_.getText().toString();
    String certId = cert_id_et_.getText().toString();
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = getString(R.string.url) + "/admin/patient";
        JSONObject jsonObject = new JSONObject();

        try {
          jsonObject.put("name", name);
          jsonObject.put("age", Integer.valueOf(age));
          jsonObject.put("certId", certId);
          jsonObject.put("sex", sex);
          jsonObject.put("address", address);
          String json = jsonObject.toString();

          OkHttpClient okHttpClient = new OkHttpClient();
          MediaType JSON = MediaType.parse("application/json; charset=utf-8");
          RequestBody body = RequestBody.create(JSON, json);

          Request request = new Request.Builder().url(url).post(body).build();

          Response response = okHttpClient.newCall(request).execute();
          String responseString = response.body().string();

          JSONObject responseJson = new JSONObject(responseString);
          String message = responseJson.getString("message");

          if (message.equals("添加成功")) {
            PostRegister();
          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
              }
            });
          }


        } catch (JSONException | IOException e) {
          throw new RuntimeException(e);
        }

      }
    }).start();
  }

  private void PostRegister() {
    String certId = cert_id_et_.getText().toString();
    String username = username_et_.getText().toString();
    String password = password_et_.getText().toString();

    new Thread(new Runnable() {
      @Override
      public void run() {
        JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("certId", certId);
          jsonObject.put("username", username);
          jsonObject.put("password", password);

          String json = jsonObject.toString();
          String url = getString(R.string.url) + "/regest";

          OkHttpClient okHttpClient = new OkHttpClient();
          MediaType JSON = MediaType.parse("application/json; charset=utf-8");
          RequestBody body = RequestBody.create(JSON, json);

          Request request = new Request.Builder().url(url).post(body).build();

          Response response = okHttpClient.newCall(request).execute();
          String responseString = response.body().string();

          JSONObject responseJson = new JSONObject(responseString);
          String message = responseJson.getString("message");

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();

              if (message.equals("注册成功")) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
              }
            }
          });


        } catch (JSONException | IOException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();

  }

  boolean validateInputs() {
    EditText[] editTexts = {
            findViewById(R.id.nameEditText),
            findViewById(R.id.ageEditText),
            findViewById(R.id.genderAutoCompleteTextView),
            findViewById(R.id.addressEditText),
            findViewById(R.id.certIdEditText),
            findViewById(R.id.usernameEditText),
            findViewById(R.id.passwordEditText)
    };

    for (EditText editText : editTexts) {
      String string = editText.getText().toString();
      if (string.isEmpty()) {
        editText.setError("此字段不能为空");
        return true;
      }
    }
    return false;
  }

}