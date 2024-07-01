package meng.hospital.patientFragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import meng.hospital.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MedicalHistoryFragment extends Fragment {
  private int patient_id_;
  private RecyclerView recyclerView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_medical_history, container, false);

    SharedPreferences patient_preferences_ = requireActivity().getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_id_ = patient_preferences_.getInt("patientId", -1);

    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    GetMedicalHistory();

    return view;
  }

  private void GetMedicalHistory() {
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