package es.uji.geotec.symptomswearos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import es.uji.geotec.symptomswearos.command.CollectionCommand;
import es.uji.geotec.symptomswearos.command.RemoteCollectionCommand;
import es.uji.geotec.wearossensors.permissions.PermissionsManager;
import es.uji.geotec.wearossensors.plainmessage.PlainMessage;
import es.uji.geotec.wearossensors.plainmessage.PlainMessageClient;
import es.uji.geotec.wearossensors.sensor.WearSensor;

public class MainActivity extends Activity {

    private LinearLayout linearLayout;
    private CollectionCommand command;
    private PlainMessageClient messageClient;
    private ProgressBar progressBar;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLayout();
        setupDestination();
        progressBar = findViewById(R.id.progress_bar);

        messageClient = new PlainMessageClient(this);
        messageClient.registerListener(message -> {
            Log.d("AAAAA", message.toString()); // ReceivedMessage{senderNodeId='adf339e4', plainMessage=PlainMessage{message='Exposure started', inResponseTo=null}, requiresResponse=false}
            this.message = message.getPlainMessage().getMessage().toString(); // Exposure started
            Log.d("AAAAA", this.message.toString());
            if (this.message.equals("Exposure started")) {
                this.startCollection();
            }
            if (this.message.equals("Exposure finished")) {
                this.stopCollection();
            }
        });

        PermissionsManager.setPermissionsActivity(this, RequestPermissionsActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionsManager.launchRequiredPermissionsRequest(this);
        }
    }

    private void setupLayout() {
        linearLayout = findViewById(R.id.linear_layout);
        if (this.getResources().getConfiguration().isScreenRound()) {
            int padding = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.146467f);
            linearLayout.setPadding(padding, padding, padding, padding);
        }
    }

    public void setupDestination() {
        this.command = new RemoteCollectionCommand(this);
        // if you would rather collect on smartwatch:
        // this.command = new LocalCollectionCommand(this);
    }

    public void startCollection() {
        // activate sensor
        WearSensor heartRateSensor = WearSensor.HEART_RATE;
        setupAnimation();
        command.executeStart(heartRateSensor);
        runOnUiThread(() -> {
            switchUI(true);
        });
    }

    public void stopCollection() {
        WearSensor heartRateSensor = WearSensor.HEART_RATE;

        ImageView imageView = findViewById(R.id.heart_icon);
        imageView.clearAnimation();

        command.executeStop(heartRateSensor);

        runOnUiThread(() -> {
            switchUI(false);
        });
    }

    public void setupAnimation() {
        Animation pulsating_animation = AnimationUtils.loadAnimation(this, R.anim.pulsating_animation);
        Animation blinking_animation = AnimationUtils.loadAnimation(this, R.anim.blinking_animation);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(pulsating_animation);
        animationSet.addAnimation(blinking_animation);
        ImageView imageView = findViewById(R.id.heart_icon);
        imageView.startAnimation(animationSet);
    }

    public void updateProgressBar(String message) {
        Log.d("AAAAA", "PROGRESS BAR RECEIVED");
        progressBar.setMax(100);
        try {
            int progress = Integer.parseInt(message);
            progressBar.setProgress(progress);
        } catch (NumberFormatException e) {
            Log.e("MainActivity", "Invalid progress value: " + message);
        }
    }

    public void switchUI(boolean showCollectionUI) {
        if (showCollectionUI) {
            findViewById(R.id.inform_start_exposure).setVisibility(View.GONE);
            findViewById(R.id.inform_data_collection).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_icon).setVisibility(View.VISIBLE);
            // findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.inform_start_exposure).setVisibility(View.VISIBLE);
            findViewById(R.id.inform_data_collection).setVisibility(View.GONE);
            findViewById(R.id.heart_icon).setVisibility(View.GONE);
            // findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }
    }
}
