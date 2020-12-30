package com.myapps.audioplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.myapps.audioplayer.databinding.ActivityMainBinding;
import com.myapps.audioplayer.databinding.SongBottomFragmentBinding;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;

    ActivityMainBinding binding;

    ArrayList <Song> songs = new ArrayList<>();

    SongBottomSheetFragment songBottomSheetFragment;

    Song curSong = null;

    int curPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        songBottomSheetFragment = new SongBottomSheetFragment();

        binding.playImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!player.isPlaying()) {
                    player.start();
                    binding.playImageView.setImageResource(R.drawable.ic_baseline_pause_24);
                    //songBottomSheetFragment.show(getSupportFragmentManager(), "tag");
                } else {
                    player.pause();
                    binding.playImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
            }
        });

        binding.openSongBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curSong != null) {
                    songBottomSheetFragment.show(getSupportFragmentManager(), "tag");
                }
            }
        });


        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                player = new MediaPlayer();

                findMusic();

                SongsAdapter adapter = new SongsAdapter(getApplicationContext(), getLayoutInflater(), songs);
                binding.songsRecView.setAdapter(adapter);
                binding.songsRecView.addOnItemTouchListener(new SongClickListener(getApplicationContext(), binding.songsRecView, new SongClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        try {
                            if(binding.controlButtonsLayout.getVisibility() != View.VISIBLE) {
                                binding.controlButtonsLayout.setVisibility(View.VISIBLE);
                            }
                            playSong(position);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

            }
        }).check();
    }

    public void playSong(final int position) throws IOException {
        curSong = songs.get(position);
        curPos = position;
        player.stop();
        player = new MediaPlayer();
        player.setDataSource(songs.get(position).getPath());
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                binding.playImageView.setImageResource(R.drawable.ic_baseline_pause_24);
                binding.songTitleTextView.setText(songs.get(position).getTitle());
                binding.artistNameTextView.setText(songs.get(position).getSubTitle());
                mp.start();
            }
        });
        player.prepareAsync();
    }


    private void findMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songCursor.getString(songPath));
            InputStream stream = null;
            if(retriever.getEmbeddedPicture() != null) {
                stream = new ByteArrayInputStream(retriever.getEmbeddedPicture());
            }
            retriever.release();

            do {
                if(!songCursor.getString(songArtist).equals("<unknown>")) {
                    songs.add(new Song(songCursor.getString(songTitle), songCursor.getString(songArtist), songCursor.getString(songPath), BitmapFactory.decodeStream(stream)));
                }
            } while (songCursor.moveToNext());
            songCursor.close();
        }
    }

    public Song getCurSong() {
        return curSong;
    }

    public void setCurSong(int curPos) {
        this.curPos = curPos;
        if(this.curPos == songs.size()) {
            this.curPos = 0;
        }
        if(this.curPos == -1) {
            this.curPos = songs.size()-1;
        }
        curSong = songs.get(this.curPos);
    }

    public static class SongBottomSheetFragment extends SuperBottomSheetFragment {
        MainActivity activity;

        SongBottomFragmentBinding binding;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            activity = (MainActivity)getActivity();
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            binding = SongBottomFragmentBinding.inflate(getLayoutInflater());

            binding.nextSongImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "next", Toast.LENGTH_SHORT).show();
                    activity.setCurSong(activity.curPos+1);
                    setSong();
                    try {
                        activity.playSong(activity.curPos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            binding.prevSongImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "prev", Toast.LENGTH_SHORT).show();
                    activity.setCurSong(activity.curPos-1);
                    setSong();
                    try {
                        activity.playSong(activity.curPos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


            setSong();

            return binding.getRoot();
        }

        private void setSong() {
            binding.songTitleTextView.setText(activity.getCurSong().getTitle());
            binding.artistNameTextView.setText(activity.getCurSong().getSubTitle());
            if(activity.getCurSong().getPoster() != null) {
                binding.songPosterImageView.setImageBitmap(activity.getCurSong().getPoster());
            } else {
                binding.songPosterImageView.setImageResource(R.drawable.ic_baseline_music_note_24);
            }
            binding.playStopImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(activity.player.isPlaying()) {
                        activity.player.pause();
                        binding.playStopImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    } else {
                        activity.player.start();
                        binding.playStopImageView.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                }
            });
            updateProgressBar();
        }

        private void updateProgressBar() {
            binding.seekBar.setMax(activity.player.getDuration());
            binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser) {
                        activity.player.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            setSeekBarTimer();
        }

        private void setSeekBarTimer() {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    binding.seekBar.setProgress(activity.player.getCurrentPosition());
                }
            }, 0, 1000);
        }
    }
}