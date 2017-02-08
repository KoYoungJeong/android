package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InitialInfoRepository extends LockExecutorTemplate {
    private static InitialInfoRepository instance;

    synchronized public static InitialInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialInfoRepository();
        }
        return instance;
    }

    public boolean upsertRawInitialInfo(RawInitialInfo info) {
        return execute(() -> {
            File teamDir = getTeamDir();
            File teamJson = new File(teamDir, getTeamJsonFileName(info.getTeamId()));
            FileWriter fileWriter = null;
            BufferedWriter bw = null;
            try {
                teamJson.createNewFile();
                fileWriter = new FileWriter(teamJson);
                bw = new BufferedWriter(fileWriter);
                bw.write(info.getRawValue());
                fileWriter.flush();
                bw.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private File getTeamDir() {
        File rootDir = JandiApplication.getContext().getFilesDir();
        File teamDir = new File(rootDir, "team_json");
        if (!teamDir.exists()) {
            teamDir.mkdirs();
        }

        return teamDir;
    }

    @NonNull
    public List<Long> getSavedTeamList() {
        return execute(() -> {

            File teamDir = getTeamDir();
            String[] list = teamDir.list();

            ArrayList<Long> teamIds = new ArrayList<>();
            for (String teamJson : list) {
                int idx = teamJson.indexOf(".");
                if (idx > 0) {
                    String teamId = teamJson.substring(0, idx);
                    try {
                        teamIds.add(Long.parseLong(teamId));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            return teamIds;
        });
    }

    @Nullable
    public RawInitialInfo getRawInitialInfo(long teamId) {
        return execute(() -> {

            File teamDir = getTeamDir();
            File teamJson = new File(teamDir, getTeamJsonFileName(teamId));
            if (teamJson.exists()) {
                FileReader fileReader = null;
                BufferedReader br = null;
                try {
                    fileReader = new FileReader(teamJson);
                    br = new BufferedReader(fileReader);
                    String temp = null;
                    StringBuilder sb = new StringBuilder();
                    while ((temp = br.readLine()) != null) {
                        sb.append(temp);
                    }
                    return new RawInitialInfo(teamId, sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                return null;
            }
        });
    }

    @NonNull
    private String getTeamJsonFileName(long teamId) {return teamId + ".json";}

    public boolean hasInitialInfo(long teamId) {
        return execute(() -> {
            File teamDir = getTeamDir();
            File file = new File(teamDir, getTeamJsonFileName(teamId));
            return file.exists();
        });
    }

    public boolean removeInitialInfo(long teamId) {
        return execute(() -> {

            File teamDir = getTeamDir();
            File file = new File(teamDir, getTeamJsonFileName(teamId));
            return file.delete();
        });
    }

    public boolean clear() {
        return execute(() -> {
            File[] files = getTeamDir().listFiles();
            for (File file : files) {
                file.delete();
            }
            return true;
        });
    }
}
