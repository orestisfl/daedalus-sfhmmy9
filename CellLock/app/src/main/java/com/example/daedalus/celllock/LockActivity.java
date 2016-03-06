package com.example.daedalus.celllock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LockActivity extends AppCompatActivity {
    Button mButton;
    EditText mEdit;
    TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lock);
        // Pick activity you want to redirected
        final Intent intent = new Intent(this, MainActivity.class);

        mButton = (Button) findViewById(R.id.unlockButton);
        mEdit = (EditText) findViewById(R.id.unlockPass);
        mText = (TextView) findViewById(R.id.lockedInfo);

        // hardcoded password
        final String secretCode = "1";
        mButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        if (secretCode.equals(mEdit.getText().toString())) {
                            mText.setText("UNLOCKED");

                            startActivity(intent);
                        }
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
    }
}
