package com.tosslab.jandi.lib.sprinkler.track;

/**
 * Created by tonyjs on 15. 7. 22..
 * <p>
 * Flush 를 위한 Track
 */
public class Track {
    // Gson Serialize 붎필요.
    private transient int index;
    private String ev;
    private String id;
    private String pl;
    private String pr;
    private long time;

    public Track(int index, String ev, String id, String pl, String pr, long time) {
        this.index = index;
        this.ev = ev;
        this.id = id;
        this.pl = pl;
        this.pr = pr;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Track{" +
                "index=" + index +
                ", ev='" + ev + '\'' +
                ", id='" + id + '\'' +
                ", pl='" + pl + '\'' +
                ", pr='" + pr + '\'' +
                ", time=" + time +
                '}';
    }

    public int getIndex() {
        return index;
    }

//    public String getEv() {
//        return ev;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public String getPl() {
//        return pl;
//    }
//
//    public String getPr() {
//        return pr;
//    }

    public long getTime() {
        return time;
    }
}
