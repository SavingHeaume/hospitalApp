package meng.hospital.patientFragment;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
      holder.time_tv.setText("时间: " + record.getString("time").substring(0, 10));

      holder.get_record_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          try {
            GetRecordPdf(record.getInt("id"), context_);
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }
      });

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
    Button get_record_btn;

    public RecordViewHolder(@NonNull View itemView) {
      super(itemView);
      id_tv = itemView.findViewById(R.id.record_id);
      department_tv = itemView.findViewById(R.id.record_department);
      doctor_tv = itemView.findViewById(R.id.record_doctor);
      expenses_tv = itemView.findViewById(R.id.record_expenses);
      time_tv = itemView.findViewById(R.id.record_time);
      get_record_btn = itemView.findViewById(R.id.get_record_btn);
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
                department_tv.setText("科室: " + jsonObject.getString("department"));
                doctor_tv.setText("医生: " + jsonObject.getString("name"));
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

  private void GetRecordPdf(Integer id, Context context) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = context.getString(R.string.url) + "/android/getRecord/" + id;
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        try {
          Response response = okHttpClient.newCall(request).execute();

          if (!"application/pdf".equals(response.header("Content-Type"))) {
            return;
          }

          String fileName = "appointment.pdf";
          String contentDisposition = response.header("Content-Disposition");
          if (contentDisposition != null && contentDisposition.contains("filename=")) {
            fileName = contentDisposition.split("filename=")[1].replaceAll("\"", "");
          }

          byte[] pdfDate = response.body().bytes();
          Uri uri = savePdfToDownloads(context, pdfDate, fileName);

          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(context, "PDF下载完成", Toast.LENGTH_SHORT).show();
              openPdf(context, uri);
            }
          });


        } catch (IOException e) {
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(context, "PDF下载完成", Toast.LENGTH_SHORT).show();
            }
          });
          throw new RuntimeException(e);
        }
      }
    }).start();
  }


  private static Uri savePdfToDownloads(Context context, byte[] pdfData, String fileName) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return saveFileUsingMediaStore(context, pdfData, fileName);
    } else {
      return saveFileToExternalStorage(context, pdfData, fileName);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static Uri saveFileUsingMediaStore(Context context, byte[] pdfData, String fileName) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
    values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

    ContentResolver resolver = context.getContentResolver();
    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

    if (uri != null) {
      try (OutputStream outputStream = resolver.openOutputStream(uri)) {
        if (outputStream != null) {
          outputStream.write(pdfData);
          outputStream.flush();
          Log.d("PDF_SAVE", "PDF saved successfully to Downloads: " + fileName);
        }
      } catch (IOException e) {
        Log.e("PDF_SAVE", "Error saving PDF: " + e.getMessage());
        return null;
      }
    }
    return uri;
  }

  private static Uri saveFileToExternalStorage(Context context, byte[] pdfData, String fileName) {
    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File file = new File(downloadsDir, fileName);

    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(pdfData);
      fos.flush();
      Log.d("PDF_SAVE", "PDF saved successfully to Downloads: " + file.getAbsolutePath());
    } catch (IOException e) {
      Log.e("PDF_SAVE", "Error saving PDF: " + e.getMessage());
      return null;
    }
    return Uri.fromFile(file);
  }

  private static void openPdf(Context context, Uri pdfUri) {
    if (pdfUri != null) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(pdfUri, "application/pdf");
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

      try {
        context.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        Toast.makeText(context, "没有找到可以打开PDF的应用", Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(context, "PDF文件未能成功保存", Toast.LENGTH_LONG).show();
    }
  }


}
