package algonquin.cst2335.greenbusmusicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);


        Button register_btn = findViewById(R.id.your_songs);
        TextView email = findViewById(R.id.email);

        register_btn.setOnClickListener(click -> startActivity(new Intent( this, PlaylistActivity.class))
        );

        String userEmail= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email.setText(userEmail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Green Bus Music App");
        toolbar.setNavigationOnClickListener(click -> finish());

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.singnout:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logout success ", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            case R.id.upload:
                startActivity(new Intent( this, UploadActivity.class));
                break;
            case R.id.playlist:
                startActivity(new Intent( this, PlaylistActivity.class));
                break;
            case R.id.profile:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
