package airjaw.butterflyandroid;

import android.content.SharedPreferences;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by airjaw on 2/18/17.
 */

public class Helper {
    public static int calculateAgeFromDateString(String birthdayString) {

        int defaultAge = 30; // default

        // facebook birthday in: MM/DD/YYYY
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        try {
            Date date1 = dateFormat.parse(birthdayString);

            Calendar dob = Calendar.getInstance();
            dob.setTime(date1);
            int year = dob.get(Calendar.YEAR);
            int month = dob.get(Calendar.MONTH);
            int day = dob.get(Calendar.DAY_OF_MONTH);

            LocalDate birthdate = new LocalDate(year, month, day);
            LocalDate now = new LocalDate();
            Years age = Years.yearsBetween(birthdate, now);
            return age.getYears();

        }
        catch (ParseException e){
            e.printStackTrace();
        }
        return defaultAge;
    }

}
