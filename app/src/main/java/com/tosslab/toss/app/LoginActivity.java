package com.tosslab.toss.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_login)
public class LoginActivity extends Activity {

    @ViewById(R.id.edtxt_login_email)
    EditText edtxtLoginId;
    @ViewById(R.id.edtxt_login_password)
    EditText edtxtLoginPassword;

    @Click(R.id.btn_login)
    void pressLoginButton() {
        moveToMainActivity();
    }

    public void moveToMainActivity() {
        MainActivity_.intent(this).start();
        finish();
    }
}
