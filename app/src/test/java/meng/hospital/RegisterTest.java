package meng.hospital;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;

import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RegisterTest {

  private Register registerActivity;

  @Before
  public void setUp() {
    registerActivity = Robolectric.buildActivity(Register.class).create().get();
  }

  @Test
  public void testValidateInputs_AllFieldsFilled() {
    // 填充所有输入字段
    ((EditText) registerActivity.findViewById(R.id.nameEditText)).setText("John Doe");
    ((EditText) registerActivity.findViewById(R.id.ageEditText)).setText("30");
    ((AutoCompleteTextView) registerActivity.findViewById(R.id.genderAutoCompleteTextView)).setText("男");
    ((EditText) registerActivity.findViewById(R.id.addressEditText)).setText("123 Test St");
    ((EditText) registerActivity.findViewById(R.id.certIdEditText)).setText("1234567890");
    ((EditText) registerActivity.findViewById(R.id.usernameEditText)).setText("johndoe");
    ((EditText) registerActivity.findViewById(R.id.passwordEditText)).setText("password");

    // 假设 validateInputs() 返回 false 表示验证通过
    assertFalse(registerActivity.validateInputs());
  }

  @Test
  public void testValidateInputs_MissingField() {
    // 填充除了一个字段外的所有字段
    ((EditText) registerActivity.findViewById(R.id.nameEditText)).setText("John Doe");
    ((EditText) registerActivity.findViewById(R.id.ageEditText)).setText("30");
    ((AutoCompleteTextView) registerActivity.findViewById(R.id.genderAutoCompleteTextView)).setText("男");
    ((EditText) registerActivity.findViewById(R.id.addressEditText)).setText("123 Test St");
    ((EditText) registerActivity.findViewById(R.id.certIdEditText)).setText("1234567890");
    ((EditText) registerActivity.findViewById(R.id.usernameEditText)).setText("johndoe");
    // 密码字段留空

    // 假设 validateInputs() 返回 true 表示验证失败
    assertTrue(registerActivity.validateInputs());
  }
}


