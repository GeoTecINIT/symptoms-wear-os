package es.uji.geotec.symptomswearos;

import androidx.appcompat.app.AppCompatActivity;

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

public class TestActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private CollectionCommand command;
    private PlainMessageClient messageClient;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        setupLayout();
        setupDestination();
        progressBar = findViewById(R.id.progress_bar);

        messageClient = new PlainMessageClient(this);
        messageClient.registerListener(message -> {
            Log.d("TestActivity", "received message: " + message);

            if (message.toString() == "Exposure started") {
                this.startCollection();
            }
            if (message.toString() == "Exposure finished") {
                this.stopCollection();
            }
            if (message.toString() == "Exposure progress: ") {
                Log.d("Exposure progress TEST", "SE PILLA BIEN EL MENSAJE DE PROGRESO");
                this.updateProgressBar(message.getPlainMessage().getMessage());
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

        Animation pulsating_animation = AnimationUtils.loadAnimation(this, R.anim.pulsating_animation);
        Animation blinking_animation = AnimationUtils.loadAnimation(this, R.anim.blinking_animation);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(pulsating_animation);
        animationSet.addAnimation(blinking_animation);
        ImageView imageView = findViewById(R.id.heart_icon);
        imageView.startAnimation(animationSet);

        command.executeStart(heartRateSensor);

        switchUI(true);
    }

    public void stopCollection() {
        WearSensor hearRateSensor = WearSensor.HEART_RATE;

        ImageView imageView = findViewById(R.id.heart_icon);
        imageView.clearAnimation();

        command.executeStop(hearRateSensor);

        switchUI(false);
    }

    public void updateProgressBar(String message) {
        Log.d("TestActivity updateProgressBar", "received message: " + message);
        progressBar.setMax(100);
        int progress = Integer.parseInt(message);
        progressBar.setProgress(progress);
    }

    public void switchUI(boolean showCollectionUI) {
        if (showCollectionUI) {
            findViewById(R.id.inform_start_exposure).setVisibility(View.GONE);
            findViewById(R.id.inform_data_collection).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_icon).setVisibility(View.VISIBLE);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.inform_start_exposure).setVisibility(View.VISIBLE);
            findViewById(R.id.inform_data_collection).setVisibility(View.GONE);
            findViewById(R.id.heart_icon).setVisibility(View.GONE);
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }
    }

}