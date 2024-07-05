package meng.hospital;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.EditText;

@RunWith(RobolectricTestRunner.class)
public class LoginTest {

  private Login loginActivity;

  @Before
  public void setUp() {
    loginActivity = Robolectric.buildActivity(Login.class).create().get();
  }

  @Test
  public void testValidateInputs_AllFieldsFilled() {
    // 使用反射来设置 EditText 的内容
    ((EditText) loginActivity.findViewById(R.id.usernameEditText)).setText("testuser");
    ((EditText) loginActivity.findViewById(R.id.passwordEditText)).setText("password");

    // 验证结果，假设 validateInputs 返回 false 表示输入有效
    assertFalse(loginActivity.validateInputs());
  }

  @Test
  public void testValidateInputs_MissingUsername() {
    // 使用反射来设置 EditText 的内容
    ((EditText) loginActivity.findViewById(R.id.passwordEditText)).setText("password");

    // 验证结果，假设 validateInputs 返回 true 表示输入无效
    assertTrue(loginActivity.validateInputs());
  }

  @Test
  public void testSaveAndLoadCredentials() {
    String testUsername = "testuser";
    String testPassword = "testpass";

    loginActivity.save_name_and_pwd(testUsername, testPassword);
    loginActivity.load_name_and_pwd();

    EditText usernameET = loginActivity.findViewById(R.id.usernameEditText);
    EditText passwordET = loginActivity.findViewById(R.id.passwordEditText);

    assertEquals(testUsername, usernameET.getText().toString());
    assertEquals(testPassword, passwordET.getText().toString());
  }
}
