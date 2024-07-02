package meng.hospital.patientFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import meng.hospital.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AppointmentRecordFragment extends Fragment {
  private int patient_id_;
  private SharedPreferences patient_preferences_ = null;
  private RecyclerView recyclerView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_appointment_record, container, false);

    patient_preferences_ = getActivity().getSharedPreferences("patient", Context.MODE_PRIVATE);
    patient_id_ = patient_preferences_.getInt("patientId", -1);

    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    GetAppointmentRecord();

    return view;
  }


  private void GetAppointmentRecord() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = getString(R.string.url) + "/android/getAppointmentByPatient/" + patient_id_;
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        try {
          Response response = okHttpClient.newCall(request).execute();
          String responseString = response.body().string();
          JSONObject jsonObject = new JSONObject(responseString);

          JSONArray jsonArray = jsonObject.getJSONArray("patientRecord");

          requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              RecordAdapter recordAdapter = new RecordAdapter(jsonArray, requireContext());
              recyclerView.setAdapter(recordAdapter);
            }
          });

        } catch (IOException | JSONException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();

  }

}

class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {
  private JSONArray appointment_record_;
  private Context context_;

  public RecordAdapter(JSONArray appointment_record, Context context) {
    this.appointment_record_ = appointment_record;
    this.context_ = context;
  }

  @NonNull
  @Override
  public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_record, parent, false);
    return new RecordViewHolder(view);
  }


  @Override
  public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
    try {
      JSONObject record = appointment_record_.getJSONObject(position);
      Integer doc_id = record.getInt("doctorid");

      SetDoctor(doc_id, holder.doctor_tv, holder.department_tv);

      holder.id_tv.setText("预约单号: " + record.getInt("id"));
      holder.expenses_tv.setText("费用: " + record.getString("expenses"));
      holder.time_tv.setText("时间: " + record.getString("time"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int getItemCount() {
    return appointment_record_.length();
  }

  public static class RecordViewHolder extends RecyclerView.ViewHolder {
    TextView id_tv, department_tv, doctor_tv, expenses_tv, time_tv;

    public RecordViewHolder(@NonNull View itemView) {
      super(itemView);
      id_tv = itemView.findViewById(R.id.record_id);
      department_tv = itemView.findViewById(R.id.record_department);
      doctor_tv = itemView.findViewById(R.id.record_doctor);
      expenses_tv = itemView.findViewById(R.id.record_expenses);
      time_tv = itemView.findViewById(R.id.record_time);
    }
  }

  private void SetDoctor(Integer id, TextView doctor_tv, TextView department_tv) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = context_.getString(R.string.url) + "/android/getDoctorById/" + id;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
          Response response = okHttpClient.newCall(request).execute();
          String string = response.body().string();
          JSONObject jsonObject = new JSONObject(string);

          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              try {
                department_tv.setText("科室: " +jsonObject.getString("department"));
                doctor_tv.setText("医生: " +jsonObject.getString("name"));
              } catch (JSONException e) {
                throw new RuntimeException(e);
              }
            }
          });

        } catch (IOException | JSONException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();

  }
}
