package athi.mc.group11.athi;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

import static libsvm.svm_parameter.PRECOMPUTED;

public class MainActivity extends AppCompatActivity {


    Boolean checkFlag =false;

    private svm_parameter params;
    private int cross_validation;
    private int nr_fold;
    private svm_problem prob;
    private double accuracy=0;
    public GraphView graph;
    public ImageView imageView;
    int bound;
    //svm_node[] feature_copy = new svm_node[bound];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(athi.athi.group11.athi.R.layout.activity_main);


        valid_permissions();



        // Create Database
        final DatabaseHandler db=new DatabaseHandler(this);
        db.creatDatabase(this);
        //defining the buttons being used in the app
        Button power_btn=(Button)findViewById(athi.athi.group11.athi.R.id.power);
        Button plot_btn=(Button)findViewById(athi.athi.group11.athi.R.id.plot3d);
        Button record=(Button)findViewById(athi.athi.group11.athi.R.id.start);
        final RadioGroup activityType = (RadioGroup) findViewById(athi.athi.group11.athi.R.id.radioGroup);
        Button DBToTXT = (Button)findViewById(athi.athi.group11.athi.R.id.convert);
        Button predict = (Button)findViewById(athi.athi.group11.athi.R.id.accuracy_btn);
        Button collect_user_act = (Button)findViewById(athi.athi.group11.athi.R.id.test_btn);
        graph = (GraphView) findViewById(athi.athi.group11.athi.R.id.graph);
        imageView=(ImageView)findViewById(athi.athi.group11.athi.R.id.imageView);

        Viewport viewport=graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMaxX(1000);
        viewport.setMinX(0);



        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int select = activityType.getCheckedRadioButtonId();
                RadioButton radio_btn = (RadioButton) findViewById(select);
                String activity = "";
                if(radio_btn != null){               //Checking if a radio button is selected
                    activity = radio_btn.getText().toString().trim();
                }

                if(!checkFlag){

                    db.insertRows(v.getContext(),activity); //inserts rows in the database


                    Intent accSensor = new Intent(v.getContext(), SensorHandler.class);   //starts the service to start sensing the accelerometer readings
                    v.getContext().startService(accSensor);

                }

            }
        });

        /*
        *Method to change the database file to the text format
        */
        DBToTXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/trainingData.txt";
                DatabaseHandler.convertFile(v.getContext(),fileName,DatabaseHandler.extractData(v.getContext())) ;

            }
        });




        /*
        *Method to plot the values in a 3D graph
        */

        plot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
//
                Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        /*
        *Method to predict accuracy of the SVM model
        */

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView accuracyText = (TextView) findViewById(athi.athi.group11.athi.R.id.accuracyTextView);

                try{
                    setParams();
                    read_problem();

                    String error = svm.svm_check_parameter(prob, params);

                    if (error != null) {
                        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                    }

                    do_cross_validation();

                    accuracyText.setText("" + accuracy+ " "+ " % ");

                }catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        collect_user_act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting to Record", Toast.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Stopped Recording", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(MainActivity.this, "Stopped Recording", Toast.LENGTH_SHORT).show();
                    }
                },3000);
                class svm_predict {
                    private  double atof(String s)
                    {
                        return Double.valueOf(s).doubleValue();
                    }

                    private int atoi(String s)
                    {
                        return Integer.parseInt(s);
                    }

                    private void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException
                    {
                        int correct = 0;
                        int total = 0;
                        double error = 0;
                        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

                        int svm_type=svm.svm_get_svm_type(model);
                        int nr_class=svm.svm_get_nr_class(model);
                        double[] prob_estimates=null;

                        if(predict_probability == 1)
                        {
                            if(svm_type == svm_parameter.EPSILON_SVR ||
                                    svm_type == svm_parameter.NU_SVR)
                            {
                                System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
                            }
                            else
                            {
                                int[] labels=new int[nr_class];
                                svm.svm_get_labels(model,labels);
                                prob_estimates = new double[nr_class];
                                output.writeBytes("labels");
                                for(int j=0;j<nr_class;j++)
                                    output.writeBytes(" "+labels[j]);
                                output.writeBytes("\n");
                            }
                        }
                        while(true)
                        {
                            String line = input.readLine();
                            if(line == null) break;

                            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

                            double target = atof(st.nextToken());
                            int m = st.countTokens()/2;
                            svm_node[] x = new svm_node[m];
                            for(int j=0;j<m;j++)
                            {
                                x[j] = new svm_node();
                                x[j].index = atoi(st.nextToken());
                                x[j].value = atof(st.nextToken());
                            }

                            double v;
                            if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
                            {
                                v = svm.svm_predict_probability(model,x,prob_estimates);
                                output.writeBytes(v+" ");
                                for(int j=0;j<nr_class;j++)
                                    output.writeBytes(prob_estimates[j]+" ");
                                output.writeBytes("\n");
                            }
                            else
                            {
                                v = svm.svm_predict(model,x);
                                output.writeBytes(v+"\n");
                            }

                            if(v == target)
                                ++correct;
                            error += (v-target)*(v-target);
                            sumv += v;
                            sumy += target;
                            sumvv += v*v;
                            sumyy += target*target;
                            sumvy += v*target;
                            ++total;
                        }
                        if(svm_type == svm_parameter.EPSILON_SVR ||
                                svm_type == svm_parameter.NU_SVR)
                        {
                            System.out.print("Mean squared error = "+error/total+" (regression)\n");
                            System.out.print("Squared correlation coefficient = "+
                                    ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
                                            ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
                                    " (regression)\n");
                        }
                        else
                            System.out.print("Accuracy = "+(double)correct/total*100+
                                    "% ("+correct+"/"+total+") (classification)\n");
                    }


                    public void main(String argv[]) throws IOException
                    {
                        int i, predict_probability=0;

                        // parse options
                        for(i=0;i<argv.length;i++)
                        {
                            if(argv[i].charAt(0) != '-') break;
                            ++i;
                            switch(argv[i-1].charAt(1))
                            {
                                case 'b':
                                    predict_probability = atoi(argv[i]);
                                    break;
                                default:
                                    System.err.print("Unknown option: " + argv[i-1] + "\n");

                            }
                        }

                        try
                        {
                            BufferedReader input = new BufferedReader(new FileReader(argv[i]));
                            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
                            svm_model model = svm.svm_load_model(argv[i+1]);
                            if(predict_probability == 1)
                            {
                                if(svm.svm_check_probability_model(model)==0)
                                {
                                    System.err.print("Model does not support probabiliy estimates\n");
                                    System.exit(1);
                                }
                            }
                            else
                            {
                                if(svm.svm_check_probability_model(model)!=0)
                                {
                                    System.out.print("Model supports probability estimates, but disabled in prediction.\n");
                                }
                            }
                            predict(input,output,model,predict_probability);
                            input.close();
                            output.close();
                        }
                        catch(FileNotFoundException e)
                        {
                            System.out.println("error");
                        }
                        catch(ArrayIndexOutOfBoundsException e)
                        {
                            System.out.println("error");
                        }
                    }
                }

                 class svm_train {
                    private svm_parameter param;
                    private svm_problem prob;
                    private svm_model model;
                    private String input_file_name;
                    private String model_file_name;
                    private String error_msg;
                    private int cross_validation;
                    private int nr_fold;

                    private svm_print_interface svm_print_null = new svm_print_interface() {
                        public void print(String s) {
                        }
                    };


                    private void do_cross_validation() {
                        int total_correct = 0;
                        double total_error = 0;
                        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
                        double[] target = new double[prob.l];

                        svm.svm_cross_validation(prob, param, nr_fold, target);
                        if (param.svm_type == svm_parameter.EPSILON_SVR
                                || param.svm_type == svm_parameter.NU_SVR) {
                            for (int i = 0; i < prob.l; i++) {
                                double y = prob.y[i];
                                double v = target[i];
                                total_error += (v - y) * (v - y);
                                sumv += v;
                                sumy += y;
                                sumvv += v * v;
                                sumyy += y * y;
                                sumvy += v * y;
                            }
                            System.out.print("Cross Validation Mean squared error = "
                                    + total_error / prob.l + "\n");
                            System.out
                                    .print("Cross Validation Squared correlation coefficient = "
                                            + ((prob.l * sumvy - sumv * sumy) * (prob.l * sumvy - sumv
                                            * sumy))
                                            / ((prob.l * sumvv - sumv * sumv) * (prob.l * sumyy - sumy
                                            * sumy)) + "\n");
                        } else {

                            int maxLabelsNum = 0;

                            for (int l = 0; l < prob.l; l++) {
                                if (prob.y[l] > maxLabelsNum)
                                    maxLabelsNum = (int) prob.y[l];
                            }

                            int[][] classifMatrix = new int[maxLabelsNum+1][maxLabelsNum+1];

                            for (int l = 0; l < prob.l; l++) {
                                if (target[l] == prob.y[l])
                                    ++total_correct;
                                // store in matrix
                                classifMatrix[(int) prob.y[l]][(int) target[l]]++;
                            }
                            System.out.print("Cross Validation Accuracy = " + 100.0
                                    * total_correct / prob.l + "%\n");

                            // find total classified as per label
                            int[] totalClassifiedAs = new int[maxLabelsNum+1];

                            // matrix calculation

                            for (int i = 0; i <= maxLabelsNum; i++) {
                                int[] hits = classifMatrix[i];
                                StringBuffer buffer = new StringBuffer();
                                buffer.append(i).append("\t: ");
                                for (int j = 0; j < hits.length; j++) {
                                    totalClassifiedAs[j] += hits[j];
                                    buffer.append("\t");
                                    if (i == j)
                                        buffer.append("*");
                                    buffer.append(hits[j]);
                                    if (i == j)
                                        buffer.append("*");
                                }
                                System.out.println(buffer.toString());
                            }


                        }
                    }

                    private void run(String argv[]) throws IOException {

                        read_problem();
                        error_msg = svm.svm_check_parameter(prob, param);

                        if (error_msg != null) {
                            System.err.print("ERROR: " + error_msg + "\n");
                            System.exit(1);
                        }

                        if (cross_validation != 0) {
                            do_cross_validation();
                        } else {
                            model = svm.svm_train(prob, param);
                            svm.svm_save_model(model_file_name, model);
                        }
                    }

                    public void main(String argv[]) throws IOException {
                        svm_train t = new svm_train();
                        t.run(argv);
                    }

                    private double atof(String s) {
                        double d = Double.valueOf(s).doubleValue();
                        if (Double.isNaN(d) || Double.isInfinite(d)) {
                            System.err.print("NaN or Infinity in input\n");
                            System.exit(1);
                        }
                        return (d);
                    }

                    private int atoi(String s) {
                        return Integer.parseInt(s);
                    }


                    // read in a problem (in svmlight format)

                    private void read_problem() throws IOException {
                        BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
                        Vector<Double> vy = new Vector<Double>();
                        Vector<svm_node[]> vx = new Vector<svm_node[]>();
                        int max_index = 0;

                        while (true) {
                            String line = fp.readLine();
                            if (line == null)
                                break;

                            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

                            vy.addElement(atof(st.nextToken()));
                            int m = st.countTokens() / 2;
                            svm_node[] x = new svm_node[m];
                            for (int j = 0; j < m; j++) {
                                x[j] = new svm_node();
                                x[j].index = atoi(st.nextToken());
                                x[j].value = atof(st.nextToken());
                            }
                            if (m > 0)
                                max_index = Math.max(max_index, x[m - 1].index);
                            vx.addElement(x);
                        }

                        prob = new svm_problem();
                        prob.l = vy.size();
                        prob.x = new svm_node[prob.l][];
                        for (int i = 0; i < prob.l; i++)
                            prob.x[i] = vx.elementAt(i);
                        prob.y = new double[prob.l];
                        for (int i = 0; i < prob.l; i++)
                            prob.y[i] = vy.elementAt(i);

                        if (param.gamma == 0 && max_index > 0)
                            param.gamma = 1.0 / max_index;

                        if (param.kernel_type == svm_parameter.PRECOMPUTED)
                            for (int i = 0; i < prob.l; i++) {
                                if (prob.x[i][0].index != 0) {
                                    System.err
                                            .print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                                    System.exit(1);
                                }
                                if ((int) prob.x[i][0].value <= 0
                                        || (int) prob.x[i][0].value > max_index) {
                                    System.err
                                            .print("Wrong input format: sample_serial_number out of range\n");
                                    System.exit(1);
                                }
                            }

                        fp.close();
                    }
                }

            }
        }

        );


        power_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),"Plotting 10 iteration data", Toast.LENGTH_SHORT).show();
                graph.setVisibility(v.VISIBLE);
                imageView.setVisibility(v.INVISIBLE);

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                        // these points are recorded using the powertutor application... ( execution time,power consumption)
                        new DataPoint(25, 24),
                        new DataPoint(150, 45),
                        new DataPoint(200, 40),
                        new DataPoint(450, 27),
                        new DataPoint(480, 29),
                        new DataPoint(600, 29),
                        new DataPoint(650, 31),
                        new DataPoint(780, 21),
                        new DataPoint(860, 26),
                        new DataPoint(1000, 26)
                });
                GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                gridLabel.setHorizontalAxisTitle("Time taken for execution / ms");
                gridLabel.setVerticalAxisTitle("Power Consumed/ mW");
                graph.addSeries(series);
            }
        });
    }

    private void valid_permissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);

            }
        }
    }

    public void setParams(){

        params = new svm_parameter();
        params.svm_type = svm_parameter.C_SVC;  //parameters are set to default values
        params.kernel_type = svm_parameter.RBF;
        params.degree = 3;
        params.gamma = 0.007;
        params.coef0 = 0;
        params.nu = 0.5;
        params.cache_size = 100;
        params.C = 1000;
        params.eps = 1e-7;
        params.p = 0.1;
        params.shrinking = 1;
        params.probability = 0;
        params.nr_weight = 0;
        params.weight_label = new int[0];
        params.weight = new double[0];
        cross_validation = 1;
        nr_fold = 3;
    }

    public void read_problem() throws IOException {

        prob = new svm_problem();
        prob.l=0;
        Vector<Double> labels = new Vector<Double>();
        Vector<svm_node[]> features = new Vector<svm_node[]>();
        int max_index = 0;

        Reader is = new InputStreamReader(getAssets().open("trainingData.txt"));
        BufferedReader br = new BufferedReader(is);

        while(true)
        {
            String line = br.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            labels.addElement(atod(st.nextToken()));
            prob.l++;
            int m = st.countTokens()/2;
            bound = m;
            svm_node[] feature = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                feature[j] = new svm_node();
                //feature_copy[j] = new svm_node();
                feature[j].index = Integer.parseInt(st.nextToken());
                //feature_copy[j].index = feature[j].index;
                feature[j].value = atod(st.nextToken());
                //feature_copy[j].value = feature[j].value;
            }
            if(m>0) max_index = Math.max(max_index, feature[m-1].index);
            features.addElement(feature);
        }

        prob.x = new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = features.elementAt(i);
        prob.y = new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = labels.elementAt(i);

        if(params.gamma == 0 && max_index > 0)
            params.gamma = 1.0/max_index;

        if(params.kernel_type == PRECOMPUTED)
            for(int i=0;i<prob.l;i++)
            {
                if (prob.x[i][0].index != 0)
                {
                    System.err.print("Wrong input format: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
                {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }

        br.close();
    }

    void do_cross_validation()
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        svm.svm_cross_validation(prob,params,nr_fold,target);
        if(params.svm_type == svm_parameter.EPSILON_SVR ||
                params.svm_type == svm_parameter.NU_SVR)
        {
            for(i=0;i<prob.l;i++)
            {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v-y)*(v-y);
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
            System.out.print("Cross Validation Squared correlation coefficient = "+
                    ((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
                            ((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))
            );
        }
        else {
            for (i = 0; i < prob.l; i++)
                if (target[i] == prob.y[i])
                    ++total_correct;
            accuracy = 100.0 * total_correct / prob.l;
            Toast.makeText(getBaseContext(), "Calculated Accuracy = " + accuracy, Toast.LENGTH_LONG).show();
        }
    }

    private static double atod(String s)
    {
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }



    public static void stopService(Context context) {

        Intent accSensor = new Intent(context, SensorHandler.class);
        Log.v("Sensor - main activity","stopping service");
        context.stopService(accSensor);


    }
}
