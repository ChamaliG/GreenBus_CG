package algonquin.cst2335.greenbusmusicapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PlaylistActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView playlist;
    FloatingActionButton upload_btn;
    Button sort_btn;
    ArrayList<Song> songs;
    SongAdapter adt;
    ValueEventListener listener;

    Query songsByUser;

    boolean songIsPlaying = false;
    int positionOfPlayingSong;

    MediaPlayer mediaPlayer;


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

        setContentView(R.layout.activity_playlist);
        playlist = findViewById(R.id.recyclerView);

        upload_btn = findViewById(R.id.upload_btn);
        sort_btn = findViewById(R.id.xv);

        songs = new ArrayList<>();
        adt = new SongAdapter();

        TextView no_songs = findViewById(R.id.no_songs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Green Bus Music App");
        toolbar.setNavigationOnClickListener(click -> finish());

        no_songs.setVisibility(View.INVISIBLE);

        upload_btn.setOnClickListener(click -> startActivity(new Intent( PlaylistActivity.this, UploadActivity.class))
        );

        //sort_btn.setOnClickListener(click -> Sort_Songs());

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        songsByUser = FirebaseDatabase.getInstance().getReference("songs").orderByChild("owner").equalTo(currentUser);

        playlist.setAdapter(adt);
        playlist.setLayoutManager(new LinearLayoutManager(this));

        listener = songsByUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                songs.clear();
                for (DataSnapshot data: snapshot.getChildren()){
                    Song song = data.getValue(Song.class);
                    song.setKey(data.getKey());
                    songs.add(song);
                    adt.notifyItemChanged(songs.size()-1);
                }
                if (songs.isEmpty()){
                    no_songs.setVisibility(View.VISIBLE);
                }else {
                    no_songs.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void Sort_Songs(String sortField, String sortType){
        playlist = findViewById(R.id.recyclerView);
        adt = new SongAdapter();

        playlist.setAdapter(adt);
        playlist.setLayoutManager(new LinearLayoutManager(this));

        Song.setSortType(sortType);
        if (sortField.equals("Title")){
            Collections.sort(songs,Song.SongTitleComparator);
        }else if (sortField.equals("Album") ) {
            Collections.sort(songs,Song.SongAlbumComparator);
        }else {
            Collections.sort(songs, Song.SongUploadTimeComparator);
        }
        adt.notifyItemChanged(songs.size()-1);

    }
//---Added code for sort button pop up
    public void sortpopup(View v){
        PopupMenu popup = new PopupMenu(this,v);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.sort_options);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch ((item.getItemId())){
            case R.id.az:
                Sort_Songs("Title","Asc");
                Toast.makeText(this,"Title - Sort By Ascending", Toast.LENGTH_SHORT).show();
                return  true;
            case R.id.za:
                Sort_Songs("Title","Des");
                Toast.makeText(this,"Title - Sort By Descending", Toast.LENGTH_SHORT).show();
                return  true;

            case R.id.azalb:
                Sort_Songs("Album","Asc");
                Toast.makeText(this,"Album - Sort By Ascending", Toast.LENGTH_SHORT).show();
                return  true;
            case R.id.zaalb:
                Sort_Songs("Album","Des");
                Toast.makeText(this,"Album - Sort By Descending", Toast.LENGTH_SHORT).show();
                return  true;
            case R.id.azupt:
                Sort_Songs("UploadTime","Asc");
                Toast.makeText(this,"Upload Time - Most Recent", Toast.LENGTH_SHORT).show();
                return  true;
            case R.id.zaupt:
                Sort_Songs("UploadTime","Des");
                Toast.makeText(this,"Album - Least Recent", Toast.LENGTH_SHORT).show();
                return  true;
            default:
                return  false;
        }
    }
//----End of sort pop up
    private class SongView extends RecyclerView.ViewHolder{

        TextView title_txt;
        TextView album_txt;
        ImageView imageView;

        int position = -1;
        boolean playable = true;

        public SongView(View itemView) {
            super(itemView);

            FloatingActionButton play_button = itemView.findViewById(R.id.play_pause);
            ImageView delete_btn = itemView.findViewById(R.id.delete);

            title_txt = itemView.findViewById(R.id.title);
            album_txt = itemView.findViewById(R.id.album);
            imageView = itemView.findViewById(R.id.albumart);

            delete_btn.setOnClickListener(c -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistActivity.this);
                builder.setMessage("Are you sure you want to delete? Action cannot be undone.");
                builder.setTitle("Delete Alert");
                builder.setNegativeButton("No", (dialog, cl) -> {
                });
                builder.setPositiveButton("Yes", (dialog, cl) -> {
                    int removedSongIndex = position;

                    songs.get(position).delete();
                    songs.remove(position);
                    adt.notifyItemRemoved(position);
                }).show();
            });

            play_button.setOnClickListener(c -> {
                if (playable && !songIsPlaying){
                    play_button.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                    try{
                        playSong(songs.get(position), position);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    playable = false;


                }else if (playable){
                    stopSong();
                    play_button.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
                    try{
                        playSong(songs.get(position), position);
                        play_button.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    playable = false;

                }else {
                    play_button.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
                    stopSong();
                    playable = true;
                }

            });
        }

        public void setPosition(int p){
            position = p;
        }


    }

    private class SongAdapter extends RecyclerView.Adapter<SongView>{
        @Override
        public SongView onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater inflater = getLayoutInflater();
            View savedSong = inflater.inflate(R.layout.fragment_song,parent,false);
            return new SongView(savedSong);
        }

        @Override
        public void onBindViewHolder(SongView holder, int position) {
            if (songs.get(position).getArtRefString() != null){
                StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + songs.get(position).getArtRefString());
                Glide.with(PlaylistActivity.this)
                        .load(ref)
                        .into(holder.imageView);
            }
            holder.title_txt.setText(songs.get(position).getTitle());
            holder.album_txt.setText(songs.get(position).getAlbum());
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }


    }

    public void playSong(Song song, int positionOfSongToPlay) throws IOException {
        stopSong();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(song.getAudioLink());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();

        songIsPlaying = true;
        positionOfPlayingSong = positionOfSongToPlay;
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            adt.notifyItemChanged(positionOfPlayingSong);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        songIsPlaying = false;

    }

}
