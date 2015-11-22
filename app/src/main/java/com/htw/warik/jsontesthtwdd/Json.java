package com.htw.warik.jsontesthtwdd;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by warik on 16.11.15.
 */
public class Json {


    private String sNummer;
    private String RZLogin;
    private String sembez;
    private String semid;

    private String jahrsem;
    private String pruefvon;
    private String pruefbis;
    private String lvvon;
    private String lvbis;

    public String getSembez() {
        return sembez;
    }

    public String getJahrsem() {
        return jahrsem;
    }

        private Context context;

        public Json(Context context)
        {
            this.context = context;
            getSemplan();
        }

        public int getSemplan()
        {
            JSONObject object;

            // Überprüfe Internetverbindung
            if (!HTTPDownloader.CheckInternet(context))
                return 900;

            // Lade Studiengänge des Studenten
            HTTPDownloader downloader = new HTTPDownloader("http://localhost:8888/index.php");
            downloader.urlParameters  = "sembez=" + "Wintersemester";
            downloader.context = context;

            String response = downloader.getStringWithPost();

            if (downloader.ResponseCode != 200)
                return downloader.ResponseCode;

            try
            {
                JSONArray arrayCourses = new JSONArray(response);
                int countCourses = arrayCourses.length();

                for (int i = 0; i < countCourses; i++)
                {
                    // Hole JSON-Objekt
                    object = arrayCourses.getJSONObject(i);

                    // Lade die Noten je nach Studiengang
                    sembez= object.getString("sembez");
                    Log.i("HTWDD",sembez);
                    jahrsem=object.getString("jahrsem");
                    Log.i("HTWDD",jahrsem);

                    response = downloader.getStringWithPost();

                }
            }
            catch (Exception e)
            {
                return 999;
            }

            return downloader.ResponseCode;
        }

        /*public void getNotenLocal()
        {
            // Gets the data repository in read mode
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database you will actually use after this query.
            String[] projection = {
                    DatabaseHandlerNoten.COLUMN_NAME_MODUL,
                    DatabaseHandlerNoten.COLUMN_NAME_Note,
                    DatabaseHandlerNoten.COLUMN_NAME_VERMERK,
                    DatabaseHandlerNoten.COLUMN_NAME_STATUS,
                    DatabaseHandlerNoten.COLUMN_NAME_CREDITS,
                    DatabaseHandlerNoten.COLUMN_NAME_VERSUCH,
                    DatabaseHandlerNoten.COLUMN_NAME_SEMESTER,
                    DatabaseHandlerNoten.COLUMN_NAME_KENNZEICHEN
            };

            Cursor cursor = db.query(DatabaseHandlerNoten.TABLE_NAME, projection, null, null,null,null,null);

            if (cursor.moveToFirst())
            {
                ArrayList<Grade> arrayList = new ArrayList<Grade>();

                do
                {
                    Grade grade = new Grade();
                    grade.Modul = cursor.getString(0);
                    grade.Note  = Float.parseFloat(cursor.getString(1));
                    grade.Vermerk = cursor.getString(2);
                    grade.Status = cursor.getString(3);
                    grade.Credits = Float.parseFloat(cursor.getString(4));
                    grade.Versuch = Short.parseShort(cursor.getString(5));
                    grade.Semester = cursor.getInt(6);
                    grade.Kennzeichen = cursor.getString(7);

                    arrayList.add(grade);
                }while (cursor.moveToNext());

                noten = arrayList.toArray(new Grade[arrayList.size()]);
            }
            else
                noten = new Grade[0];

            cursor.close();
            db.close();
        }

        public void saveNotenLocal()
        {
            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            mDbHelper.clearTable(db);

            for (Grade note : noten)
            {
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(DatabaseHandlerNoten.COLUMN_NAME_MODUL, note.Modul);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_Note, note.Note);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_VERMERK, note.Vermerk);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_STATUS, note.Status);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_CREDITS, note.Credits);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_VERSUCH, note.Versuch);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_SEMESTER, note.Semester);
                values.put(DatabaseHandlerNoten.COLUMN_NAME_KENNZEICHEN, note.Kennzeichen);

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(DatabaseHandlerNoten.TABLE_NAME, null, values);
            }
            db.close();
        }

        public Stats[] getStats()
        {
            // Gets the data repository in read mode
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            ArrayList<Stats> arrayList = new ArrayList<Stats>();

            Cursor cursor = db.rawQuery("SELECT Noten.Semester, MAX(Note), MIN(Note), AnzahlNoten,SUM(Credits),SUM(Note*Credits)" +
                    " FROM Noten" +
                    " JOIN (SELECT Semester, Count(Credits) AS AnzahlNoten FROM Noten WHERE Credits != 0.0 GROUP BY Semester) AS UA" +
                    " ON UA.Semester == Noten.Semester" +
                    " WHERE NOTE != 0" +
                    " GROUP BY Noten.Semester" +
                    " ORDER BY Noten.Semester DESC", null);

            if (cursor.moveToFirst())
            {
                Stats total = new Stats();
                total.Semester = "Studium";
                total.GradeBest = 5.0f;
                total.GradeWorst = 1.0f;

                do
                {
                    Stats stats         = new Stats();
                    stats.Semester      = getSemester(Integer.parseInt(cursor.getString(0)));
                    stats.GradeWorst    = cursor.getFloat(1);
                    stats.GradeBest     = cursor.getFloat(2);
                    stats.GradeCount    = cursor.getInt(3);
                    stats.Credits       = cursor.getFloat(4);
                    stats.Average       = cursor.getFloat(5)/stats.Credits;

                    total.GradeBest     = Math.min(total.GradeBest,cursor.getFloat(2));
                    total.GradeWorst    = Math.max(total.GradeWorst,cursor.getFloat(1));
                    total.Credits       += cursor.getFloat(4);
                    total.GradeCount    += cursor.getInt(3);
                    total.Average       += cursor.getFloat(5);

                    arrayList.add(stats);

                }while (cursor.moveToNext());

                total.Average = total.Average / total.Credits;

                arrayList.add(0, total);

                cursor.close();
                db.close();

                return arrayList.toArray(new Stats[arrayList.size()]);
            }

            cursor.close();
            db.close();
            return new Stats[0];
        }

        public static String getSemester(Integer Semester)
        {
            Semester-=20000;
            if (Semester%2 == 1)
                return "Sommersemester " + Semester / 10;
            else
                return "Wintersemester " + Semester / 10 + " / " + ((Semester / 10)+1);
        }*/
}
