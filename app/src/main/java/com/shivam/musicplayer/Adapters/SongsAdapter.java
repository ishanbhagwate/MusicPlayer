package com.shivam.musicplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shivam.musicplayer.Model.Songs;
import com.shivam.musicplayer.PlayerActivity;
import com.shivam.musicplayer.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private Context context;

    private List<Songs> songsList;

    public SongsAdapter(List<Songs> songsList,Context context) {
        this.songsList = songsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item,parent,false);
        context= parent.getContext();

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder( final ViewHolder holder, final int position) {

        final Songs songs = songsList.get(position);
        holder.songsName.setText(songs.getTitle());
        holder.artistName.setText(songs.getArtist());


    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView songsName,artistName;

        public ViewHolder( View itemView) {
            super(itemView);

            songsName = itemView.findViewById(R.id.listSongName_id);
            artistName = itemView.findViewById(R.id.listSongArtistName_id);
        }
    }

}
