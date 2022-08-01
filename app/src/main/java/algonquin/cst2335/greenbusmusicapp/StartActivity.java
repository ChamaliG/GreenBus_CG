package algonquin.cst2335.greenbusmusicapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button register_btn = findViewById(R.id.register_btn);
        Button login_btn = findViewById(R.id.login_btn);

        register_btn.setOnClickListener(click -> startActivity(new Intent( StartActivity.this, RegisterActivity.class))
        );

        login_btn.setOnClickListener(click -> startActivity(new Intent( StartActivity.this, LoginActivity.class))
        );

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(StartActivity.this , ProfileActivity.class));
            finish();
        }
    }

}
