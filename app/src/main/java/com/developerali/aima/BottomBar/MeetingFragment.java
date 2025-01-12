package com.developerali.aima.BottomBar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentMeetingBinding;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import me.ibrahimsn.lib.SmoothBottomBar;
import timber.log.Timber;


public class MeetingFragment extends Fragment {

    FragmentMeetingBinding binding;
    private SmoothBottomBar bottomBar;

    public MeetingFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMeetingBinding.inflate(inflater, container, false);
        Helper.changeStatusBarColor(getActivity(), R.color.white);
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(2);


        Bundle arguments = getArguments();
        if (arguments != null) {
            String secretCode = arguments.getString("secretCode");

            if (secretCode != null){
                binding.code.setText(secretCode);
                Toast.makeText(getActivity(), "click Join button", Toast.LENGTH_SHORT).show();
            }
        }


        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();

        binding.joinBtn.setOnClickListener(c->{
            if (binding.code.getText().toString().isEmpty()){
                Toast.makeText(getActivity(), "Enter Secret Code", Toast.LENGTH_LONG).show();
                binding.code.setError("Not Valid !");
            } else {
                JitsiMeetConferenceOptions options
                        = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(binding.code.getText().toString())
                        // Settings for audio and video
                        //.setAudioMuted(true)
                        //.setVideoMuted(true)
                        .build();
                // Launch the new activity with the given options. The launch() method takes care
                // of creating the required Intent and passing the options.
                JitsiMeetActivity.launch(getActivity(), options);
            }

        });

        binding.shareBtn.setOnClickListener(c->{

            //throw new RuntimeException("Carsh Test");

            if (binding.code.getText().toString().isEmpty()){
                Toast.makeText(getActivity(), "Enter Secret Code", Toast.LENGTH_LONG).show();
            }else {
                String post = readyShare(binding.code.getText().toString());
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/html");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, post);
                if (sharingIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(sharingIntent,"Sharing Meeting"));
                }
            }
        });




























        return binding.getRoot();
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }


    public String readyShare(String meeting_id){
        Date date = new Date();
        String post =
                "       JOIN MEETING\n" +
                        "      Presented By *AIMA*\n" +
                        "----------------------------\n\n" +

                        "You are invited for AIMA's meeting. Please follow below steps to join the meeting!\n" +
                        "\n" +
                        "Steps for joining meeting" +
                        "\n"+
                        "Download App and Install -> Go to Meeting tab -> enter meeting secret code -> click JOIN button-> enter password -> Ok\n" +
                        "\n" +
                        "meeting secret code- \n" +
                        meeting_id + "\n" +
                        "----------------------------\n" +
                        "Thank You "

                ;
        return post;
    }

}