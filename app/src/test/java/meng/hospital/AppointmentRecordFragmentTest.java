package meng.hospital;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import meng.hospital.patientFragment.AppointmentRecordFragment;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AppointmentRecordFragmentTest {
  private AppointmentRecordFragment fragment;
  private OkHttpClient mockClient;

//  @Before
//  public void setUp() {
//    mockClient = mock(OkHttpClient.class);
//    FragmentScenario<AppointmentRecordFragment> scenario = FragmentScenario.launch(AppointmentRecordFragment.class);
//    scenario.onFragment(fragment -> {
//      this.fragment = fragment;
//      fragment.setOkHttpClient(mockClient); // Inject the mock client
//    });
//  }

  @Test
  public void testGetHospitalization() throws IOException {
    // 模拟OkHttpClient和Response

  }

  @Test
  public void test2() {

  }
  @Test
  public void test3() {

  }
  @Test
  public void test4() {

  }

}
