package algonquin.cst2335.greenbusmusicapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;


public class UploadActivity extends AppCompatActivity {

    String currentUser;
    String art_ref;
    String art_link;

    EditText title;
    EditText album;
    TextView song_selected;
    TextView art_selected;
    Button upload_song;
    Button upload_art;
    Button upload_btn;

    Uri audioUri;
    Uri imageUri;

    DatabaseReference db;
    StorageReference songsStorageRef;
    StorageReference imagesStorageRef;
    StorageTask uploadTask;

    ActivityResultLauncher<Intent> getAudioContentLauncher;
    ActivityResultLauncher<Intent> getImageContentLauncher;

    ProgressDialog pd;



    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

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
                break;
            case R.id.playlist:
                startActivity(new Intent( this, PlaylistActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent( this, ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        title = findViewById(R.id.title);
        album = findViewById(R.id.album);

        song_selected = findViewById(R.id.song_selected_txt);
        art_selected = findViewById(R.id.art_selected_txt);
        upload_btn = findViewById(R.id.upload_btn);
        upload_song = findViewById(R.id.upload_song_btn);
        upload_art = findViewById(R.id.upload_art_btn);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Green Bus Music App");
        toolbar.setNavigationOnClickListener(click -> finish());

        db = FirebaseDatabase.getInstance().getReference().child("songs");
        songsStorageRef = FirebaseStorage.getInstance().getReference().child("songs");
        imagesStorageRef = FirebaseStorage.getInstance().getReference().child("images");

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        upload_btn.setOnClickListener(c -> uploadToFirebase());
        upload_song.setOnClickListener(c -> openAudioFile());
        upload_art.setOnClickListener(c -> openImageFile());

        getAudioContentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().getData() != null) {
                        audioUri = result.getData().getData();
                        String fileName = getFileName(audioUri);
                        song_selected.setText(fileName);
                    }
                });
        getImageContentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        String fileName = getFileName(imageUri);
                        art_selected.setText(fileName);
                    }
                });
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String filename = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        if (filename == null) {
            filename = uri.getPath();
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }
        }
        return filename;
    }

    public void openAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            getAudioContentLauncher.launch(intent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getAudioContentLauncher.launch( new Intent (Intent.ACTION_GET_CONTENT).setType("audio/*"));
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }

            default:
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void uploadToFirebase() {
        String selectedSong = song_selected.getText().toString();
        String title_txt = title.getText().toString();
        if (selectedSong.equals(getString(R.string.no_file))) {
            Toast.makeText(getApplicationContext(), "Please select an audio file.", Toast.LENGTH_LONG).show();
        }else if (title_txt == null || title_txt.trim().equals("")){
            Toast.makeText(getApplicationContext(), "A title is required.", Toast.LENGTH_LONG).show();
        } else {
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Upload in progress already", Toast.LENGTH_LONG).show();
            } else {
                uploadSongObjectToDatabase();
            }
        }
    }

    private void uploadSongObjectToDatabase() {
        String title_txt = title.getText().toString();
        String album_txt = album.getText().toString();

        if (audioUri != null) {
            pd = new ProgressDialog(this);

            pd.setTitle("Uploading...");
            pd.setMessage("Please wait as your song is uploaded.");
            pd.show();

            StorageReference songRef = songsStorageRef.child(title_txt+ "_" + System.currentTimeMillis() + "." + getFileExtension(audioUri));
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("name", title_txt).setCustomMetadata("Uploaded by", currentUser)
                    .build();
            String temp = art_selected.getText().toString();
            if (! temp.equals(getString(R.string.no_file))){
                String artref = title_txt+"_art_"+System.currentTimeMillis() + "." + getFileExtension(imageUri);
                StorageReference imgRef = imagesStorageRef.child(artref);

                uploadTask = imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        art_ref = artref;
                        art_link = uri.toString();
                    });
                });
            }

            uploadTask = songRef.putFile(audioUri, metadata).addOnSuccessListener(taskSnapshot -> {
                songRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    Song song = new Song(title_txt, album_txt, uri.toString(), art_ref, art_link, currentUser);

                    String uploadId = db.push().getKey();
                    song.setKey(uploadId);

                    Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    db.child(uploadId).setValue(song);
                    startActivity(new Intent( UploadActivity.this, PlaylistActivity.class));


                });
            });
        } else {
            Toast.makeText(getApplicationContext(), "No audio song selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void openImageFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        getImageContentLauncher.launch(intent);
    }
}
