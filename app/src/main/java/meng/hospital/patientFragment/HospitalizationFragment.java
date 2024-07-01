package meng.hospital.patientFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import meng.hospital.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HospitalizationFragment extends Fragment {
  private int patient_id_;

  private LinearLayout otherRecordsContainer = null;
  private SharedPreferences patient_preferences_;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_hospitalization, container, false);

    patient_preferences_ = requireActivity().getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_id_ = patient_preferences_.getInt("patientId", -1);

    otherRecordsContainer = view.findViewById(R.id.other_records_container);

    GetHospitalization();

    return view;
  }

  private void GetHospitalization() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = getString(R.string.url) + "/android/hospitalization/" + patient_id_;
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
          Response response = okHttpClient.newCall(request).execute();
          String reqponseString = response.body().string();
          JSONObject jsonObject = new JSONObject(reqponseString);

          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              try {
                fillRecord(requireView(), jsonObject.getJSONObject("theLast"));

                JSONArray jsonArray = jsonObject.getJSONArray("others");
                for (int i = 0; i < jsonArray.length(); i++) {
                  JSONObject record = jsonArray.getJSONObject(i);
                  View recordView = getLayoutInflater().inflate(R.layout.item_hospital_record, otherRecordsContainer, false);
                  fillRecord(recordView, record);
                  otherRecordsContainer.addView(recordView);
                }

              } catch (JSONException e) {
                throw new RuntimeException(e);
              }
            }
          });

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }


  @SuppressLint("SetTextI18n")
  private void fillRecord(View view, JSONObject jsonObject) {
    try {
      TextView patientName = view.findViewById(R.id.patient_name);
      TextView medicalName = view.findViewById(R.id.medical_name);
      TextView roomInfo = view.findViewById(R.id.room_info);
      TextView in_time = view.findViewById(R.id.in_time);
      TextView out_time = view.findViewById(R.id.out_time);

      patientName.setText("姓名: " + jsonObject.getString("patientname"));
      medicalName.setText("病因: " + jsonObject.getString("medicalname"));
      roomInfo.setText("楼层: " + jsonObject.getString("floor") +
              ", 床位: " + jsonObject.getString("bed") +
              ", 门号: " + jsonObject.getString("door"));

      in_time.setText("入院时间: " + jsonObject.getString("intime"));

      out_time.setText("出院时间：" + jsonObject.optString("outtime", "N/A"));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}