package com.myapps.audioplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<Song> songList;
    private Context context;

    private MediaPlayer player;

    public SongsAdapter(Context context, LayoutInflater inflater, ArrayList<Song> songList) {
        this.context = context;
        this.inflater = inflater;
        this.songList = songList;
        player = new MediaPlayer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.song_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Song song = songList.get(position);
        holder.artistTextView.setText(song.getSubTitle());
        holder.titleTextView.setText(song.getTitle());
        if(song.getPoster() != null) {
            holder.posterImageView.setImageBitmap(song.getPoster());
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView, artistTextView;
        final ImageView posterImageView;
        ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.songTitleTextView);
            artistTextView = view.findViewById(R.id.artistNameTextView);
            posterImageView = view.findViewById(R.id.songImageView);
        }
    }
}
