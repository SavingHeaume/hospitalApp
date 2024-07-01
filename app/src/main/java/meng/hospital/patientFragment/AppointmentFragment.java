package meng.hospital.patientFragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.DatePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import meng.hospital.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AppointmentFragment extends Fragment {

  private Button appointment_Btn = null;
  private AutoCompleteTextView spinnerDepartment = null;
  private AutoCompleteTextView spinnerDoctor = null;
  private DatePicker date_picker = null;

  private String patient_name_;
  private int patient_id_;
  private int doctor_id_;
  private int expenses_;
  private List<Integer> doctor_ids_ = new ArrayList<>();

  private int expenses_array_[] = {100, 120, 80, 150, 130, 90};

  private SharedPreferences patient_preferences_ = null;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_appointment, container, false);

    appointment_Btn = view.findViewById(R.id.button_confirm);
    spinnerDepartment = view.findViewById(R.id.spinner_department);
    spinnerDoctor = view.findViewById(R.id.spinner_doctor);
    date_picker = view.findViewById(R.id.date_picker);

    patient_preferences_ = getActivity().getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_id_ = patient_preferences_.getInt("patientId", -1);

    // 从资源中获取字符串数组
    String[] departments = getResources().getStringArray(R.array.departments);
    // 创建 ArrayAdapter 并设置给 AutoCompleteTextView
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, departments);
    spinnerDepartment.setAdapter(adapter);

    // 设置下拉框监听
    spinnerDepartment.setOnItemClickListener((parent, view1, position, id) -> {
      String selectedDepartment = parent.getItemAtPosition(position).toString();
      fetchDoctorsForDepartment(selectedDepartment);
      expenses_ = expenses_array_[position];
    });

    spinnerDoctor.setOnItemClickListener((parent, view1, position, id) -> {
      doctor_id_ = doctor_ids_.get(position);
    });

    appointment_Btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AddAppointment();
      }
    });

    return view;
  }

  private void fetchDoctorsForDepartment(String department) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        OkHttpClient client = new OkHttpClient();
        String usl = getString(R.string.url) + "/doctor/" + department;
        Request request = new Request.Builder()
                .url(usl)
                .build();

        try {
          Response response = client.newCall(request).execute();
          String responseData = response.body().string();
          JSONObject jsonObject = new JSONObject(responseData);
          JSONArray doctorsArray = jsonObject.getJSONArray("doctors");
          List<String> doctorNames = ExtracDoctorNamesAndId(doctorsArray);
          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(requireContext(),
                      android.R.layout.simple_dropdown_item_1line, doctorNames);
              spinnerDoctor.setAdapter(doctorAdapter);
            }
          });

        } catch (Exception e) {
          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(requireContext(), "获取医生信息失败", Toast.LENGTH_SHORT).show();
            }
          });
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  private List<String> ExtracDoctorNamesAndId(JSONArray jsonArray) throws JSONException {
    List<String> doctorNames = new ArrayList<>();
    doctor_ids_.clear();
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonDoctor = jsonArray.getJSONObject(i);
      String name = jsonDoctor.getString("name");
      Integer id = jsonDoctor.getInt("id");
      doctor_ids_.add(id);
      doctorNames.add(name);
    }
    return doctorNames;
  }

  private void AddAppointment() {
    new Thread(new Runnable() {
      @Override
      public void run() {

        int day = date_picker.getDayOfMonth();
        int month = date_picker.getMonth() + 1;
        int year = date_picker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = simpleDateFormat.format(calendar.getTime());

        JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("patientid", patient_id_);
          jsonObject.put("doctorid", doctor_id_);
          jsonObject.put("time", date);
          jsonObject.put("expenses", expenses_);
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }

        String json = jsonObject.toString();
        String url = getString(R.string.url) + "/patient/appointment";

        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=urf-8");
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
          Response response = okHttpClient.newCall(request).execute();
          String responseString = response.body().string();
          JSONObject jsonObject1 = new JSONObject(responseString);
          String message = jsonObject1.getString("message");

          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
          });

        } catch (Exception e) {
          throw new RuntimeException(e);
        }


      }
    }).start();
  }
}