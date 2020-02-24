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

public class LastAddedAdapter extends RecyclerView.Adapter<LastAddedAdapter.ViewHolder> {

    private Context context;

    private List<Songs> songsList;

    public LastAddedAdapter(List<Songs> songsList) {
        this.songsList = songsList;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item,parent,false);
        context= parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, final int position) {
        final Songs songs = songsList.get(position);
        holder.songsName.setText(songs.getTitle());
        holder.artistName.setText(songs.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //implement
                Intent playerIntent = new Intent(context, PlayerActivity.class);
                playerIntent.putExtra("path",songs.getData());
                playerIntent.putExtra("name", songs.getTitle());
                playerIntent.putExtra("artist",songs.getArtist());
//                playerIntent.putExtra("id",songs.getId());
                playerIntent.putExtra("position",position);
                playerIntent.putExtra("allSongs",false);
                playerIntent.putExtra("fromIntent",true);

                context.startActivity(playerIntent);

                Toast.makeText(context, "Clicked : "+ position, Toast.LENGTH_SHORT).show();

//                Uri uri = Uri.parse(String.valueOf(songs.getData()));
//                MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                try {
//                    mediaPlayer.setDataSource(context,uri);
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }



            }
        });
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
