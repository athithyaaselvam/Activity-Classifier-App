package athi.mc.group11.athi;



import libsvm.*;
import java.io.*;
import java.util.*;


public class svm_train {
    private svm_parameter param;
    private svm_problem prob;
    private svm_model model;
    private String input_file_name;
    private String model_file_name;
    private String error_msg;
    private int cross_validation;
    private int nr_fold;

    private static svm_print_interface svm_print_null = new svm_print_interface() {
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

    public static void main(String argv[]) throws IOException {
        svm_train t = new svm_train();
        t.run(argv);
    }

    private static double atof(String s) {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return (d);
    }

    private static int atoi(String s) {
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

