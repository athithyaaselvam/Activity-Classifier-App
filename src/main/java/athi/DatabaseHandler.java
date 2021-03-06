package athi.mc.group11.athi;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;


public class DatabaseHandler extends AppCompatActivity {


    public static final String DATABASE_NAME = "Group11DB.db";
    public static final String TABLE_NAME = "ActivityTable";
    public static int rowCount = 1;          // Should not be greater than 60
    public static int currentcCount = 10;   // Should not be greater than 50

    protected Context context;
    public DatabaseHandler(Context context){
        this.context = context;
    }

    //Creating database for storing the activity label and their axis values
    public void creatDatabase(Context context) {
        SQLiteDatabase databasehandler = null;
        try{
            databasehandler = context.openOrCreateDatabase( DATABASE_NAME,MODE_PRIVATE, null );
            databasehandler.beginTransaction();
            try{

                databasehandler.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " ("+ " ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , "
                        + "X_Val_1 float, "+"Y_Val_1 float, "+"Z_Val_1 float, "+"X_Val_2 float, "+"Y_Val_2 float, "+"Z_Val_2 float, "+"X_Val_3 float, "+"Y_Val_3 float, "+"Z_Val_3 float, "+"X_Val_4 float, "+"Y_Val_4 float, "+"Z_Val_4 float, "+"X_Val_5 float, "+"Y_Val_5 float, "+"Z_Val_5 float, "+"X_Val_6 float, "+"Y_Val_6 float, "+"Z_Val_6 float, "+"X_Val_7 float, "+"Y_Val_7 float, "+"Z_Val_7 float, "+"X_Val_8 float, "+"Y_Val_8 float, "+"Z_Val_8 float, "+"X_Val_9 float, "+"Y_Val_9 float, "+"Z_Val_9 float, "+"X_Val_10 float, "+"Y_Val_10 float, "+"Z_Val_10 float, "+"X_Val_11 float, "+"Y_Val_11 float, "+"Z_Val_11 float, "+"X_Val_12 float, "+"Y_Val_12 float, "+"Z_Val_12 float, "+"X_Val_13 float, "+"Y_Val_13 float, "+"Z_Val_13 float, "+"X_Val_14 float, "+"Y_Val_14 float, "+"Z_Val_14 float, "+"X_Val_15 float, "+"Y_Val_15 float, "+"Z_Val_15 float, "+"X_Val_16 float, "+"Y_Val_16 float, "+"Z_Val_16 float, "+"X_Val_17 float, "+"Y_Val_17 float, "+"Z_Val_17 float, "+"X_Val_18 float, "+"Y_Val_18 float, "+"Z_Val_18 float, "+"X_Val_19 float, "+"Y_Val_19 float, "+"Z_Val_19 float, "+"X_Val_20 float, "+"Y_Val_20 float, "+"Z_Val_20 float, "+"X_Val_21 float, "+"Y_Val_21 float, "+"Z_Val_21 float, "+"X_Val_22 float, "+"Y_Val_22 float, "+"Z_Val_22 float, "+"X_Val_23 float, "+"Y_Val_23 float, "+"Z_Val_23 float, "+"X_Val_24 float, "+"Y_Val_24 float, "+"Z_Val_24 float, "+"X_Val_25 float, "+"Y_Val_25 float, "+"Z_Val_25 float, "+"X_Val_26 float, "+"Y_Val_26 float, "+"Z_Val_26 float, "+"X_Val_27 float, "+"Y_Val_27 float, "+"Z_Val_27 float, "+"X_Val_28 float, "+"Y_Val_28 float, "+"Z_Val_28 float, "+"X_Val_29 float, "+"Y_Val_29 float, "+"Z_Val_29 float, "+"X_Val_30 float, "+"Y_Val_30 float, "+"Z_Val_30 float, "+"X_Val_31 float, "+"Y_Val_31 float, "+"Z_Val_31 float, "+"X_Val_32 float, "+"Y_Val_32 float, "+"Z_Val_32 float, "+"X_Val_33 float, "+"Y_Val_33 float, "+"Z_Val_33 float, "+"X_Val_34 float, "+"Y_Val_34 float, "+"Z_Val_34 float, "+"X_Val_35 float, "+"Y_Val_35 float, "+"Z_Val_35 float, "+"X_Val_36 float, "+"Y_Val_36 float, "+"Z_Val_36 float, "+"X_Val_37 float, "+"Y_Val_37 float, "+"Z_Val_37 float, "+"X_Val_38 float, "+"Y_Val_38 float, "+"Z_Val_38 float, "+"X_Val_39 float, "+"Y_Val_39 float, "+"Z_Val_39 float, "+"X_Val_40 float, "+"Y_Val_40 float, "+"Z_Val_40 float, "+"X_Val_41 float, "+"Y_Val_41 float, "+"Z_Val_41 float, "+"X_Val_42 float, "+"Y_Val_42 float, "+"Z_Val_42 float, "+"X_Val_43 float, "+"Y_Val_43 float, "+"Z_Val_43 float, "+"X_Val_44 float, "+"Y_Val_44 float, "+"Z_Val_44 float, "+"X_Val_45 float, "+"Y_Val_45 float, "+"Z_Val_45 float, "+"X_Val_46 float, "+"Y_Val_46 float, "+"Z_Val_46 float, "+"X_Val_47 float, "+"Y_Val_47 float, "+"Z_Val_47 float, "+"X_Val_48 float, "+"Y_Val_48 float, "+"Z_Val_48 float, "+"X_Val_49 float, "+"Y_Val_49 float, "+"Z_Val_49 float, "+"X_Val_50 float, "+"Y_Val_50 float, "+"Z_Val_50 float, "+"Label varchar(30)  ); "
                );

                databasehandler.setTransactionSuccessful();
            }
            catch (SQLiteException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                databasehandler.endTransaction();
                databasehandler.close();
            }
        }catch (SQLException e){
            Toast.makeText( context , e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Inserting rows of data into the database for each activity recorded
    public void insertRows(Context context , String activity) {
        SQLiteDatabase databasehandler = null;

        currentcCount =1;
        try{
            databasehandler = context.openOrCreateDatabase( DATABASE_NAME,MODE_PRIVATE, null );
            databasehandler.beginTransaction();

            try{

                databasehandler.execSQL( "INSERT INTO "
                        + TABLE_NAME
                        + "( Label ) VALUES ('"
                        + activity + "');"

                );
                databasehandler.setTransactionSuccessful(); //commit your changes.setTransactionSuccessful();

            }
            catch (SQLiteException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                databasehandler.endTransaction();
                databasehandler.close();
            }
        }catch (SQLException e){

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Updating the axis values for every activity each time the activity is recorded upto 50 time
    public void updateRows(Context context , Float x, Float y , Float z) {
        SQLiteDatabase databasehandler = null;
        try{
            databasehandler = context.openOrCreateDatabase( DATABASE_NAME,MODE_PRIVATE, null );
            databasehandler.beginTransaction();

            try{

                databasehandler.execSQL( "UPDATE " + TABLE_NAME + " SET X_Val_"+currentcCount+ " = " + x+ " , Y_Val_"+currentcCount+ " = " + y+ ", Z_Val_"+currentcCount+ " = " + z+" WHERE ID =  (Select Max(ID) from "+ TABLE_NAME + " ); "
                );
                databasehandler.setTransactionSuccessful(); //commit your changes.setTransactionSuccessful();

            }
            catch (SQLiteException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                databasehandler.endTransaction();
                databasehandler.close();
            }
        }catch (SQLException e){

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }


        //stopService
        if (currentcCount == 50) {

            Log.v("Sensor","stopping service");
            MainActivity.stopService(context);

        }
        else {
            currentcCount++;
        }
    }

    public static float[][] extractData(Context context){
        float[][] data = new float[100][151];
        Cursor cursor;
        SQLiteDatabase handler = null;
        try{
            handler = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            String records = "SELECT * FROM "+TABLE_NAME;
            String count = "SELECT count(*) AS \"Count\"FROM "+TABLE_NAME;

            cursor = handler.rawQuery(records, null);
            int r = 0;

            if (cursor.moveToFirst()) {

                int c = 0;
                while (!cursor.isAfterLast()){

                    for(c=0; c<50; c++){
                        String xColName = "X_Val_"+(c+1);
                        String yColName = "Y_Val_"+(c+1);
                        String zColName = "Z_Val_"+(c+1);

                        data[r][c*3] = (cursor.getFloat(cursor.getColumnIndex(xColName)));
                        data[r][c*3+1] = (cursor.getFloat(cursor.getColumnIndex(yColName)));
                        data[r][c*3+2] = (cursor.getFloat(cursor.getColumnIndex(zColName)));

                    }
                    String activity = (cursor.getString(cursor.getColumnIndex("Label")));

                    // Enter the label

                    if(activity.equals("Walking")){
                        data[r][c*3] = 1;
                    }else if(activity.equals("Running")){
                        data[r][c*3] = 2;
                    }else{
                        data[r][c*3] = 3;
                    }

                    cursor.moveToNext();
                    r += 1;
                }
                cursor.close();
            }

            cursor=handler.rawQuery(count,null);

            if (cursor.moveToFirst()) {
                rowCount=  cursor.getInt(cursor.getColumnIndex("Count"));

                if (rowCount == 60){

                    Toast.makeText(context,"Reached Here", Toast.LENGTH_LONG).show();

                }

            }

        }catch(Exception e){


        }
        finally {
            handler.close();
            return data;
        }
    }

    //Taking data from database and pushing to text file to view the data
    public static void convertFile(Context context, String fileName, float tuple[][]){
        try{
            File file = new File(fileName);
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for(int r=0;r<tuple.length;r++){
                String str=Integer.toString(r);
                Log.v("Trace_Rows",str);

                int a = (int)tuple[r][150];
                String data = ""+ a;
                for(int c=0;c<150;c++){
                    data += " "+(c+1)+":"+tuple[r][c];
                }
                writer.println(data);
                Log.v("Trace_Write",data);
            }
            writer.close();
            Toast.makeText(context, "FilePath : " + fileName,Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            Toast.makeText(context, e.getMessage()+ "Failed#############################",Toast.LENGTH_SHORT).show();
            Log.v("exception ", e.getMessage());


        }
    }

    //Getting data from database to visualize and the label is used in representing each activity
    public static ArrayList getDataToVisualize(Context context) {
        Cursor cursor;
        ArrayList data = new ArrayList();

        SQLiteDatabase db = null;

        try {
            db = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            String records = "SELECT * FROM " + TABLE_NAME;
            String count = "SELECT count(*) AS \"Count\"FROM " + TABLE_NAME;


            cursor = db.rawQuery(records, null);

            if (cursor.moveToFirst()) {
                ArrayList walkingArray = new ArrayList();
                ArrayList runningArray = new ArrayList();
                ArrayList jumpingArray = new ArrayList();
                int c = 0;
                while (!cursor.isAfterLast()) {
                    for (c = 0; c < 50; c++) {
                        JSONObject point = new JSONObject();
                        String xColName = "X_Val_" + (c + 1);
                        String yColName = "Y_Val_" + (c + 1);
                        String zColName = "Z_Val_" + (c + 1);

                        String activity = (cursor.getString(cursor.getColumnIndex("Label")));

                        int label ;
                        if(activity.equals("Walking")){
                            label = 1;
                        }else if(activity.equals("Running")){
                            label = 2;
                        }else{
                            label = 3;
                        }

                        float xVal = cursor.getFloat(cursor.getColumnIndex(xColName));
                        float yVal = cursor.getFloat(cursor.getColumnIndex(yColName));
                        float zVal = cursor.getFloat(cursor.getColumnIndex(zColName));

                        point.put("x", xVal);
                        point.put("y", yVal);
                        point.put("z", zVal);
                        point.put("c", label);

                        if(label == 1) {
                            walkingArray.add(point);
                        } else if (label == 2) {
                            runningArray.add(point);
                        } else {
                            jumpingArray.add(point);
                        }
                    }

                    cursor.moveToNext();
                }
                data.add(walkingArray);
                data.add(runningArray);
                data.add(jumpingArray);

                cursor.close();
            }

            cursor = db.rawQuery(count, null);

            if (cursor.moveToFirst()) {
                rowCount = cursor.getInt(cursor.getColumnIndex("Count"));

                if (rowCount == 60) {

                    Toast.makeText(context, "Reached Here", Toast.LENGTH_LONG).show();

                }

            }

        } catch (Exception e) {
            Log.v("Viz",e.toString());

        } finally {
            db.close();
            return data;
        }

    }


}
