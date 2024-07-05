package meng.hospital;
import android.widget.Spinner;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.fragment.app.testing.FragmentScenario;
import static org.junit.Assert.*;

import meng.hospital.patientFragment.AppointmentFragment;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AppointmentFragmentTest {

  private AppointmentFragment fragment;

  @Before
  public void setUp() {
    FragmentScenario<AppointmentFragment> scenario = FragmentScenario.launchInContainer(
            AppointmentFragment.class,
            null,
            R.style.AppTheme // 确保使用正确的主题
    );
    scenario.onFragment(f -> {
      fragment = f;
    });
  }

  @Test
  public void testValidateInputs_AllFieldsFilled() {
    // 设置所有输入字段
    ((EditText) fragment.getView().findViewById(R.id.spinner_department)).setText("内科");
    ((EditText) fragment.getView().findViewById(R.id.spinner_doctor)).setText("张医生");

    // 假设 validateInputs() 返回 false 表示验证通过
    assertFalse(fragment.validateInputs());
  }

  @Test
  public void testValidateInputs_MissingField() {
    // 设置部分输入字段
    ((EditText) fragment.getView().findViewById(R.id.spinner_department)).setText("内科");
    // 医生字段未设置

    // 假设 validateInputs() 返回 true 表示验证失败
    assertTrue(fragment.validateInputs());
  }
}

