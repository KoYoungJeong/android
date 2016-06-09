package com.tosslab.jandi.app.local.orm.persister;

import android.text.TextUtils;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionLongConverter extends BaseDataType {
    private static final CollectionLongConverter singleton = new CollectionLongConverter();

    protected CollectionLongConverter() {
        super(SqlType.STRING, new Class[]{
                Collection.class
        });
    }

    public static CollectionLongConverter getSingleton() {
        return singleton;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        if (javaObject instanceof Collection) {
            StringBuilder builder = new StringBuilder();
            Collection javaObject1 = (Collection) javaObject;
            boolean first = true;
            for (Object o : javaObject1) {
                if (first) {
                    first = false;
                } else {
                    builder.append(",");
                }
                builder.append(o.toString());
            }
            return builder.toString();
        }
        return "";
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        if (sqlArg instanceof String) {
            List<Long> retValues = new ArrayList<>();
            String[] split = ((String) sqlArg).split(",");
            for (String s : split) {
                if (TextUtils.isEmpty(s)) {
                    continue;
                }
                try {
                    retValues.add(Long.parseLong(s));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return retValues;
        }
        return new ArrayList<Long>();
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return defaultStr;
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }
}
