package meng.hospital;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.fragment.app.testing.FragmentScenario;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.TextView;

import java.io.IOException;

import meng.hospital.patientFragment.HospitalizationFragment;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class HospitalizationFragmentTest {

  private HospitalizationFragment fragment;

  @Before
  public void setUp() {

  }

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

