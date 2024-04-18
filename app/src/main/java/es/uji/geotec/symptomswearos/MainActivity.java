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

        Log.d("AAAAA setup", "AAAAA SETUP DONE");

        messageClient = new PlainMessageClient(this);
        messageClient.registerListener(message -> {
            Log.d("AAAAA TestActivity", "AAAAA received message: "
                    + message.getPlainMessage().getMessage().toString());

            this.message = message.getPlainMessage().getMessage().toString();
            if (this.message.equals("Exposure started")) {
                this.startCollection();
            }
            if (this.message.equals("Exposure finished")) {
                this.stopCollection();
            }
            if (this.message.equals("Exposure progress: ")) {
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
        Log.d("AAAAA", "AAAAA RECOLECCION EMPEZADA");
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
        Log.d("AAAAA", "AAAAA RECOLECCION TERMINADA");
        WearSensor hearRateSensor = WearSensor.HEART_RATE;

        ImageView imageView = findViewById(R.id.heart_icon);
        imageView.clearAnimation();

        command.executeStop(hearRateSensor);

        switchUI(false);
    }

    public void updateProgressBar(String message) {
        Log.d("AAAAA", "AAAAA PROGRESS BAR RECIBIDO");
        progressBar.setMax(100);
        int progress = Integer.parseInt(message);
        progressBar.setProgress(progress);
    }

    public void switchUI(boolean showCollectionUI) {
        if (showCollectionUI) {
            Log.d("AAAAA", "AAAAA UI SI");

            findViewById(R.id.inform_start_exposure).setVisibility(View.GONE);
            findViewById(R.id.inform_data_collection).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_icon).setVisibility(View.VISIBLE);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        }
        else {
            Log.d("AAAAA", "AAAAA UI NO");

            findViewById(R.id.inform_start_exposure).setVisibility(View.VISIBLE);
            findViewById(R.id.inform_data_collection).setVisibility(View.GONE);
            findViewById(R.id.heart_icon).setVisibility(View.GONE);
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }
    }

}











/*package es.uji.geotec.symptomswearos;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import es.uji.geotec.backgroundsensors.sensor.Sensor;
import es.uji.geotec.backgroundsensors.sensor.SensorManager;
import es.uji.geotec.wearossensors.plainmessage.PlainMessage;
import es.uji.geotec.wearossensors.plainmessage.PlainMessageClient;
import es.uji.geotec.wearossensors.permissions.PermissionsManager;
import es.uji.geotec.wearossensors.sensor.WearSensor;
import es.uji.geotec.symptomswearos.command.CollectionCommand;
import es.uji.geotec.symptomswearos.command.LocalCollectionCommand;
import es.uji.geotec.symptomswearos.command.RemoteCollectionCommand;

public class MainActivity extends Activity {

    private LinearLayout linearLayout;
    private RadioGroup destinationRadio;
    private Button startSingle, stopSingle;
    private Spinner sensorSpinner;
    private CollectionCommand command;
    private PlainMessageClient messageClient;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLayout();
        setupButtons();
        setupSpinner();
        updateProgressBar();

        command = new LocalCollectionCommand(this);
        messageClient = new PlainMessageClient(this);
        messageClient.registerListener(message -> {
            Log.d("MainActivity", "received " + message);

            if (message.responseRequired()) {
                Log.d("MainActivity", "response required! sending response...");
                PlainMessage response = new PlainMessage("PONG!", message.getPlainMessage());
                messageClient.send(response);
            }
        });

        PermissionsManager.setPermissionsActivity(this, RequestPermissionsActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionsManager.launchRequiredPermissionsRequest(this);
        }
    }

    public void onDestinationButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (!checked) return;

        if (view.getId() == R.id.local_collection) {
            this.command = new LocalCollectionCommand(this);
        }
        else if (view.getId() == R.id.remote_collection) {
            this.command = new RemoteCollectionCommand(this);
        }
    }

    public void setupDestination() {
        this.command = new RemoteCollectionCommand(this);
        // if want collection on smartwatch:
        // this.command = new LocalCollectionCommand(this);
    }

    public void onStartSingleCommandTap(View view) {
        WearSensor selectedSensor = (WearSensor) sensorSpinner.getSelectedItem();
        boolean requested = PermissionsManager.launchPermissionsRequestIfNeeded(this, selectedSensor.getRequiredPermissions());
        if (requested) return;

        ImageView imageView = findViewById(R.id.check_icon);
        Animation pulsating_animation = AnimationUtils.loadAnimation(this, R.anim.pulsating_animation);
        Animation blinking_animation = AnimationUtils.loadAnimation(this, R.anim.blinking_animation);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(pulsating_animation);
        animationSet.addAnimation(blinking_animation);

        imageView.startAnimation(animationSet);

        toggleVisibilityButton(stopSingle, startSingle);
        toggleVisibility(findViewById(R.id.inform_start_exposure), false);
        toggleVisibility(findViewById(R.id.inform_data_collection), true);
        toggleVisibility(findViewById(R.id.check_icon), true);
        toggleVisibility(findViewById(R.id.progress_bar), true);
        sensorSpinner.setEnabled(false);
        destinationRadio.setEnabled(false);

        command.executeStart(selectedSensor);
    }

   public void setupHeartRateSensor(View view) {
       SensorManager sensorManager = new SensorManager(this);


   }

    private void setupSpinner() {
        sensorSpinner = findViewById(R.id.sensor_spinner);

        SensorManager sensorManager = new SensorManager(this);

        ArrayAdapter<Sensor> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Sensor sensor : sensorManager.availableSensors(WearSensor.values())) {
            adapter.add(sensor);
        }

        sensorSpinner.setAdapter(adapter);
    }

    public void onStopSingleCommandTap(View view) {
        toggleVisibilityButton(startSingle, stopSingle);
        sensorSpinner.setEnabled(true);
        destinationRadio.setEnabled(true);

        ImageView imageView = findViewById(R.id.check_icon);
        imageView.clearAnimation();

        WearSensor selectedSensor = (WearSensor) sensorSpinner.getSelectedItem();
        command.executeStop(selectedSensor);
        toggleVisibility(findViewById(R.id.inform_start_exposure), true);
        toggleVisibility(findViewById(R.id.inform_data_collection), false);
        toggleVisibility(findViewById(R.id.check_icon), false);
        toggleVisibility(findViewById(R.id.progress_bar), false);
    }

    public void onSenFreeMessageTap(View view) {
        PlainMessage message = new PlainMessage("Hi! This is a test message");
        messageClient.send(message);
    }

    private void setupLayout() {
        linearLayout = findViewById(R.id.linear_layout);
        if (this.getResources().getConfiguration().isScreenRound()) {
            int padding = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.146467f);
            linearLayout.setPadding(padding, padding, padding, padding);
        }
    }

    private void setupButtons() {
        startSingle = findViewById(R.id.start_single_command);
        stopSingle = findViewById(R.id.stop_single_command);
        destinationRadio = findViewById(R.id.destination_collection);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void updateProgressBar() {
        progressBar.setMax(100);
        progressBar.setProgress(50);
    }

    private void toggleVisibilityButton(@NonNull Button setVisible, @NonNull Button setGone) {
        setVisible.setVisibility(View.VISIBLE);
        setGone.setVisibility(View.GONE);
    }

    private void toggleVisibility(View element, boolean visible) {
        if (visible) {
            element.setVisibility(View.VISIBLE);
        }
        else {
            element.setVisibility(View.GONE);
        }
    }
}*/
