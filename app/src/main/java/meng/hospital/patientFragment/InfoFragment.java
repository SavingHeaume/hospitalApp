package meng.hospital.patientFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import meng.hospital.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class InfoFragment extends Fragment {

  private AutoCompleteTextView auto_complete_search_type_ = null;
  private TextInputEditText edit_text_search_ = null;
  private MaterialButton button_search_;
  private RecyclerView recycler_view_results_;
  private LottieAnimationView loading_animation_;
  private SearchResultAdapter adapter_;

  private String search_type_;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_info, container, false);

    auto_complete_search_type_ = view.findViewById(R.id.autoCompleteSearchType);
    edit_text_search_ = view.findViewById(R.id.editTextSearch);
    button_search_ = view.findViewById(R.id.buttonSearch);
    recycler_view_results_ = view.findViewById(R.id.recyclerViewResults);
//    loading_animation_ = view.findViewById(R.id.loadingAnimation);

    // 设置搜索类型下拉菜单
    String[] searchTypes = getResources().getStringArray(R.array.search_types);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, searchTypes);
    auto_complete_search_type_.setAdapter(adapter);

    // 下拉框监听
    auto_complete_search_type_.setOnItemClickListener((parent, view1, position, id) -> {
      String[] searchTypes_eng = getResources().getStringArray(R.array.search_types_eng);
      search_type_ = searchTypes_eng[position];
    });

    // 设置 RecyclerView
    recycler_view_results_.setLayoutManager(new LinearLayoutManager(requireContext()));
    this.adapter_ = new SearchResultAdapter(new ArrayList<>());
    recycler_view_results_.setAdapter(this.adapter_);

    button_search_.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String s = auto_complete_search_type_.getText().toString();
        if (s.isEmpty()) {
          Toast.makeText(requireContext(), "搜索种类不能为空", Toast.LENGTH_SHORT).show();
          return;
        }
        performSearch();
      }
    });

    return view;
  }

  private void performSearch() {
    String searchQuery = edit_text_search_.getText().toString();

    new Thread(new Runnable() {
      @Override
      public void run() {
        String url;
        url = getString(R.string.url) + "/patient/searchinfo" + "?name=" + searchQuery + "&type=" + search_type_;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try {
          Response response = okHttpClient.newCall(request).execute();
          String responseString = response.body().string();
          JSONObject jsonObject = new JSONObject(responseString);
          JSONArray jsonArray = jsonObject.getJSONObject("map").getJSONArray(search_type_);

          List<SearchResult> searchResults = new ArrayList<>();
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            searchResults.add(new SearchResult(search_type_, item));
          }

          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              adapter_.setResults(searchResults);
              recycler_view_results_.setVisibility(View.VISIBLE);
            }
          });

        } catch (IOException | JSONException e) {
          throw new RuntimeException(e);
        }


      }
    }).start();

  }


}

/**
 * SearchResult 类用于存储搜索结果的信息
 */
class SearchResult {
  /**
   * 搜索结果的类型（如"医生"、"疾病"或"药品"）
   */
  private String type;
  /**
   * 搜索结果的详细信息，以 JSONObject 形式存储
   */
  private JSONObject details;

  /**
   * 构造函数
   *
   * @param type    搜索结果的类型
   * @param details 搜索结果的详细信息
   */
  public SearchResult(String type, JSONObject details) {
    this.type = type;
    this.details = details;
  }

  /**
   * 获取搜索结果的类型
   *
   * @return 搜索结果的类型
   */
  public String getType() {
    return type;
  }

  /**
   * 获取搜索结果的详细信息
   *
   * @return 搜索结果的详细信息，以 JSONObject 形式
   */
  public JSONObject getDetails() {
    return details;
  }
}


/**
 * SearchResultAdapter 类用于管理搜索结果的 RecyclerView
 */
class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
  /**
   * 存储搜索结果的列表
   */
  private List<SearchResult> results;

  /**
   * 构造函数
   *
   * @param results 初始的搜索结果列表
   */
  public SearchResultAdapter(List<SearchResult> results) {
    this.results = results;
  }

  /**
   * 创建 ViewHolder
   *
   * @param parent   父视图
   * @param viewType 视图类型
   * @return 新创建的 ViewHolder
   */
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
    return new ViewHolder(view);
  }

  /**
   * 绑定 ViewHolder 的数据
   *
   * @param holder   ViewHolder 实例
   * @param position 数据在列表中的位置
   */
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    SearchResult result = results.get(position);
    holder.bind(result);
  }

  /**
   * 获取搜索结果的数量
   *
   * @return 搜索结果的数量
   */
  @Override
  public int getItemCount() {
    return results.size();
  }

  /**
   * 更新搜索结果列表
   *
   * @param results 新的搜索结果列表
   */
  public void setResults(List<SearchResult> results) {
    this.results = results;
    notifyDataSetChanged();
  }

  /**
   * ViewHolder 内部类，用于管理每个搜索结果项的视图
   */
  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView titleTextView;
    TextView detailsTextView;

    /**
     * 构造函数
     *
     * @param itemView 每个搜索结果项的视图
     */
    ViewHolder(View itemView) {
      super(itemView);
      titleTextView = itemView.findViewById(R.id.titleTextView);
      detailsTextView = itemView.findViewById(R.id.detailsTextView);
    }

    /**
     * 绑定搜索结果数据到视图
     *
     * @param result 搜索结果对象
     */
    void bind(SearchResult result) {
      titleTextView.setText(result.getType());
      StringBuilder details = new StringBuilder();
      JSONObject jsonObject = result.getDetails();
      Iterator<String> keys = jsonObject.keys();
      while (keys.hasNext()) {
        String key = keys.next();

        if (key.equals("username") || key.equals("password") || key.equals("id") || key.equals("certid")) {
          continue;
        }

        try {
          String value = jsonObject.getString(key);
          if (key.equals("type")) {
            value = GetDrugType(Integer.valueOf(value));
          }
          if (key.equals("expert")) {
            value = GetExpert(Integer.valueOf(value));
          }
          details.append(key).append(": ").append(value).append("\n");
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      detailsTextView.setText(details.toString().trim());
    }

    private String GetDrugType(int type) {
      switch (type) {
        case 0:
          return "颗粒剂";
        case 1:
          return "丸剂";
        case 2:
          return "散剂";
        case 3:
          return "酊剂";
        case 4:
          return "片剂";
        case 5:
          return "胶囊剂";
        default:
          return "未知";
      }
    }

    private String GetExpert(int expret) {
      switch (expret) {
        case 1:
          return "专家";
        default:
          return "非专家";
      }
    }
  }
}

