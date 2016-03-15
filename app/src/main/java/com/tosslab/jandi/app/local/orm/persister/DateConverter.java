package com.tosslab.jandi.app.local.orm.persister;

import android.database.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongType;

import java.util.Date;

/**
 * Created by tee on 16. 3. 3..
 */
public class DateConverter extends LongType {

    private static final DateConverter singleton = new DateConverter();

    protected DateConverter() {
        super(SqlType.LONG, new Class<?>[]{
                Date.class
        });
    }

    public static DateConverter getSingleton() {
        return singleton;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject)
            throws SQLException {
        if (javaObject instanceof Date) {
            return ((Date) javaObject).getTime();
        }
        return javaObject;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
            throws SQLException {
        if (sqlArg instanceof Long) {
            return new Date((Long) sqlArg);
        }
        return null;
    }

}
