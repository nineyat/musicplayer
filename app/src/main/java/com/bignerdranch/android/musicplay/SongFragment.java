package com.bignerdranch.android.musicplay;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

import com.bignerdranch.android.musicplay.dao.Song;
import com.bignerdranch.android.musicplay.lab.SongLab;

public class SongFragment extends Fragment {

    private static final String ARG_SONG_ID = "song_id";
    private Song mSong;
    private TextView mSongWordsView;
    private TextView mSongNameView;
    private TextView mSongIntroView;
    private ImageView mSongImage;
    private Button mButton;
    int[] imageArray = {R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seven, R.drawable.eight, R.drawable.nine, R.drawable.ten};
    private Animation mAnimation;

    public static SongFragment newInstance(UUID songId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_SONG_ID, songId);

        SongFragment fragment = new SongFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void replay (UUID songId) {
        mSong = SongLab.get(getActivity()).getSong(songId);
        if (SongPagerActivity.getPlayer().isPlaying()) {
            SongPagerActivity.getPlayer().stop();
            SongPagerActivity.destroyPlayer();
        }

        try {
            Log.e(this.getClass().getName(), mSong.getMusic());
            AssetFileDescriptor fd = getContext().getAssets().openFd(mSong.getMusic());
            SongPagerActivity.getPlayer().setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            SongPagerActivity.getPlayer().prepare();
            SongPagerActivity.getPlayer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID songId = (UUID) getArguments().getSerializable(ARG_SONG_ID);
        mSong = SongLab.get(getActivity()).getSong(songId);
    }

    @Override
    public void onPause() {
        super.onPause();
        SongLab.get(getActivity()).updateSong(mSong);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_song, container, false);
        mSongImage = (ImageView) v.findViewById(R.id.image_view);
        mSongImage.setImageResource(imageArray[Integer.parseInt(mSong.getOrder()) - 1]);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.img_animation);
        mSongImage.startAnimation(mAnimation);
        mSongWordsView = (TextView) v.findViewById(R.id.song_words);
        mSongWordsView.setText(mSong.getSongWords());
        mSongWordsView.setMovementMethod(new ScrollingMovementMethod());
        mSongNameView = (TextView) v.findViewById(R.id.song_name);
        mSongNameView.setText(mSong.getSongName());
        mSongIntroView = (TextView) v.findViewById(R.id.song_intro);
        mSongIntroView.setText(mSong.getSongUnit());
        mButton = (Button) v.findViewById(R.id.song_comments);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("song", mSong.getSongName());
                Intent intent = new Intent(getActivity(), CommentListActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        this.replay(mSong.getId());
        return v;
    }

    public void onDestroy() {
        SongPagerActivity.getPlayer().stop();
        super.onDestroy();
    }
}
