package meng.hospital;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Patient extends AppCompatActivity {

  private int login_id_;
  private String patient_name_;
  private int patient_id_;
  SharedPreferences patient_preferences_ = null;
  SharedPreferences.Editor patient_editor_ = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_patient);

    Intent intent = getIntent();
    login_id_ = intent.getIntExtra("loginId", -1);

    getPatient();

    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment);


    if (navHostFragment != null) {
      NavController navController = navHostFragment.getNavController();
      NavigationUI.setupWithNavController(bottomNav, navController);

//      Bundle bundle = new Bundle();
//      bundle.putInt("patientId", patient_id_);
//      bundle.putString("patientName", patient_name_);
//
//      navController.navigate(R.id.appointmentFragment, bundle);
//      //navController.navigate(R.id.hospitalizationFragment, bundle);
    } else {
      throw new IllegalStateException("NavHostFragment not found");
    }
  }


  private void getPatient() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = "http://10.129.124.217:8088/android/findPatientByLoginId/" + login_id_;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
          Response response = client.newCall(request).execute();
          String responseString = response.body().string();
          JSONObject jsonObject = new JSONObject(responseString);

          patient_id_ = jsonObject.getInt("patientId");
          SavePatientId();

        } catch (Exception e) {
          throw new RuntimeException(e);
        }

      }
    }).start();
  }

  private void SavePatientId() {
    patient_preferences_ = getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_editor_ = patient_preferences_.edit();

    patient_editor_.putInt("patientId", patient_id_);

    patient_editor_.commit();
  }
}