package com.tosslab.jandi.app.team.room;

import com.tosslab.jandi.app.network.models.start.Folder;

import java.util.List;

public class TopicFolder implements Cloneable {
    private Folder folder;
    private List<TopicRoom> rooms;
    private boolean isDummy = false;

    public TopicFolder(Folder folder, List<TopicRoom> rooms) {
        this.folder = folder;
        this.rooms = rooms;
    }

    public TopicFolder() {
    }

    public static TopicFolder makeDummyFolder() {
        TopicFolder topicFolder = new TopicFolder();
        topicFolder.setDummy(true);
        return topicFolder;
    }

    public boolean isDummy() {
        return isDummy;
    }

    public void setDummy(boolean dummy) {
        isDummy = dummy;
    }

    public Folder getRaw() {
        return folder;
    }

    public long getId() {
        return folder.getId();
    }

    public String getName() {
        return folder.getName();
    }

    public int getSeq() {
        return folder.getSeq();
    }

    public List<TopicRoom> getRooms() {
        return rooms;
    }

    public void setRooms(List<TopicRoom> rooms) {
        this.rooms = rooms;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return "TopicFolder{" +
                "folder=" + folder +
                ", rooms=" + rooms +
                ", isDummy=" + isDummy +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
