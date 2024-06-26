package meng.hospital.patientFragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import meng.hospital.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AppointmentFragment extends Fragment {

  private Button appointment_Btn = null;
  private AutoCompleteTextView spinnerDepartment = null;
  private AutoCompleteTextView spinnerDoctor = null;

//  @Override
//  public void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//
//  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_appointment, container, false);

    appointment_Btn = view.findViewById(R.id.button_confirm);
    spinnerDepartment = view.findViewById(R.id.spinner_department);
    spinnerDoctor = view.findViewById(R.id.spinner_doctor);

    // 从资源中获取字符串数组
    String[] departments = getResources().getStringArray(R.array.departments);
    // 创建 ArrayAdapter 并设置给 AutoCompleteTextView
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, departments);
    spinnerDepartment.setAdapter(adapter);

    // 设置下拉框监听
    spinnerDepartment.setOnItemClickListener((parent, view1, position, id) -> {
      String selectedDepartment = parent.getItemAtPosition(position).toString();
      fetchDoctorsForDepartment(selectedDepartment);
    });

    appointment_Btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
      }
    });

    return view;
  }

  private void fetchDoctorsForDepartment(String department) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        OkHttpClient client = new OkHttpClient();
        String usl = "http://10.129.124.217:8088/doctor/" + department;
        Request request = new Request.Builder()
                .url(usl)
                .build();

        client.newCall(request).enqueue(new Callback() {
          @Override
          public void onFailure(@NonNull Call call, @NonNull IOException e) {
            requireActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(requireContext(), "查找医生数据失败", Toast.LENGTH_SHORT).show();
              }
            });
          }

          @Override
          public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.isSuccessful()) {
              String responseData = response.body().string();
              try {
                JSONObject jsonObject = new JSONObject(responseData);
                JSONArray doctorsArray = jsonObject.getJSONArray("doctors");
                List<String> doctorNames = extracDoctorNames(doctorsArray);


                requireActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, doctorNames);
                    spinnerDoctor.setAdapter(doctorAdapter);
                  }
                });
              } catch (
                      JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(requireContext(), "Failed to parse doctor data", Toast.LENGTH_SHORT).show();
                  }
                });
              }
            } else {
              requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(requireContext(), "Failed to fetch doctors", Toast.LENGTH_SHORT).show();
                }
              });
            }
          }
        });
      }
    }).start();
  }

  private List<String> extracDoctorNames(JSONArray jsonArray) throws JSONException {
    List<String> doctorNames = new ArrayList<>();
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonDoctor = jsonArray.getJSONObject(i);
      String name = jsonDoctor.getString("name");
      doctorNames.add(name);
    }
    return doctorNames;
  }
}