package algonquin.cst2335.greenbusmusicapp;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;

public class Song {
    private String title;
    private String album;
    private String audioRef;
    private String audioLink;
    private String key;
    private String artRefString;
    private String owner;
    private String artLink;
    public static String sortType;

    public Song(){}

    public Song(String title, String album, String audioLink, String artRef, String artLink ,String owner ) {
        this.title = title;
        this.album = album;
        this.audioLink = audioLink;
        this.owner = owner;
        this.artRefString = artRef;
        this.artLink = artLink;
    }

    public String getArtLink() {
        return artLink;
    }

    public void setArtLink(String artLink) {
        this.artLink = artLink;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public String getArtRefString() {
        return artRefString;
    }

    public void setArtRefString(String artRefString) {
        this.artRefString = artRefString;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    //public String getSortType() { return sortType; }

    //public void setSortType(String sortType){this.sortType=sortType;}

    @Exclude
    public String getKey() {
        return key;
    }
    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public void delete(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("songs");
        Query query=ref.child(getKey());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // remove the value at reference
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static Comparator<Song> SongTitleComparator = new Comparator<Song>() {

        public int compare(Song s1, Song s2) {
            String Song1 = s1.getTitle().toUpperCase();
            String Song2 = s2.getTitle().toUpperCase();

            Song song = new Song();
            if (song.sortType=="Des"){
                //Descending order
                return Song2.compareTo(Song1);
            }else{
                //Ascending order
                return Song1.compareTo(Song2);
            }
        }
    };

    public static Comparator<Song> SongAlbumComparator = new Comparator<Song>() {

        public int compare(Song s1, Song s2) {
            String Album1 = s1.getAlbum().toUpperCase();
            String Album2 = s2.getAlbum().toUpperCase();

            Song song = new Song();
            if (song.sortType=="Des"){
                //Descending order
                return Album2.compareTo(Album1);
            }else{
                //Ascending order
                return Album1.compareTo(Album2);
            }
        }
    };

    public static String setSortType(String sortType1){
        sortType = sortType1;
        return sortType;
    }
}
