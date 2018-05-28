package athi.mc.group11.athi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;



//When there is a new sensor SensorEventListener is used to recieve notifications
public class SensorHandler extends Service implements SensorEventListener {

    private SensorManager accelerometer_Manage;
    private Sensor senseAccel;
    float accel_X ;
    float accel_Y;
    float accel_Z;

//At new sensor event OnSensorChanged is called
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accel_X = sensorEvent.values[0];
            accel_Y = sensorEvent.values[1];
            accel_Z = sensorEvent.values[2];
            DatabaseHandler db = new DatabaseHandler(this);
            db.updateRows(this, accel_X, accel_Y, accel_Z);
        }
    }

    //Bindservice is used to bind the client to a service
    //Client uses Ibinder argument to ciommunicate with the bound service
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Call OnAccuractChanges when the accuracy of the sensor changes
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//Use Oncreate to initialize the activity
    @Override
    public void onCreate(){
        super.onCreate();
        Log.v("Sensor","onCreate");
        Toast.makeText(this, "Started Service", Toast.LENGTH_SHORT).show();
        accelerometer_Manage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelerometer_Manage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer_Manage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }
    //Use OnStartCommand to start the service by client everytime
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
//Now unregister the sensor listener
    @Override
    public void onDestroy() {
        this.accelerometer_Manage.unregisterListener(this);
        super.onDestroy();

        Toast.makeText(this, "Stopped Service!", Toast.LENGTH_SHORT).show();
    }
//When all clients unbind the service, it is destroyed
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
