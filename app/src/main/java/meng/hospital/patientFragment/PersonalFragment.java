package meng.hospital.patientFragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import meng.hospital.Login;
import meng.hospital.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PersonalFragment extends Fragment {
  private int patient_id_;
  private String patient_name_;
  private RecyclerView recyclerView;
  private TextView account_name_text_view_ = null;
  private Button log_out_button_ = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_personal, container, false);

    SharedPreferences patient_preferences_ = requireActivity().getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_id_ = patient_preferences_.getInt("patientId", -1);
    patient_name_ = patient_preferences_.getString("patientName", "无姓名");

    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    log_out_button_ = view.findViewById(R.id.logoutButton);
    account_name_text_view_ = view.findViewById(R.id.accountNameTextView);
    log_out_button_.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
      }
    });

    account_name_text_view_.setText("账户名称: " + patient_name_);

    GetMedicalHistory();

    return view;
  }

  public void GetMedicalHistory() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = getString(R.string.url) + "/android/medicalhistory/" + patient_id_;
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
          Response response = okHttpClient.newCall(request).execute();
          if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
          }

          String responseString = response.body().string();
          JSONObject jsonObject = new JSONObject(responseString);
          JSONArray jsonArray = jsonObject.getJSONArray("medicalhistory");

          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              PatientAdapter patientAdapter = new PatientAdapter(jsonArray);
              recyclerView.setAdapter(patientAdapter);
            }
          });

        } catch (IOException | JSONException e) {
          throw new RuntimeException(e);
        }

      }
    }).start();
  }


}


class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private JSONArray medicalHistory;

    public PatientAdapter(JSONArray medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        try {
            JSONObject patient = medicalHistory.getJSONObject(position);
            holder.tvPatientName.setText(patient.getString("patientname"));
            holder.tvDiagnosis.setText(patient.getString("name"));
            holder.tvTime.setText(patient.getString("time"));
            holder.tvDoctorName.setText(patient.getString("doctorname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return medicalHistory.length();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDiagnosis, tvTime, tvDoctorName;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
        }
    }
}