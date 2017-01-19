package com.tosslab.jandi.app.local.orm.persister;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;
import com.tosslab.jandi.app.utils.dynamicl10n.DynamicMessageUtil;

import java.io.IOException;
import java.sql.SQLException;

public class FormatMessageConverter extends BaseDataType {
    private static FormatMessageConverter singleton = new FormatMessageConverter();

    public FormatMessageConverter() {
        super(SqlType.STRING, new Class[]{
                FormatParam.class
        });
    }

    public static FormatMessageConverter getSingleton() {

        return singleton;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        if (javaObject instanceof FormatParam) {
            try {
                return JsonMapper.getInstance().getObjectMapper().writeValueAsString(javaObject);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "{}";
            }
        } else {
            return "{}";
        }
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        if (sqlArg instanceof String) {
            try {
                FormatParam formatParam = JsonMapper.getInstance().getObjectMapper().readValue(((String) sqlArg), FormatParam.class);
                String formatKey = formatParam.getTypOf();
                return DynamicMessageUtil.getFormatParam(formatKey, ((String) sqlArg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new FormatParam();
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return new FormatParam();
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }
}
