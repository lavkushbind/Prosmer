package com.healweal.prosmer;
 import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.Timestamp;

public class JournalEntry implements Parcelable {
    private String id;
    private String title;
    private String content;
    private Timestamp timestamp;

    public JournalEntry() {} // Needed for Firestore

    public JournalEntry(String id, String title, String content, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters...
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }

    // --- Parcelable Implementation ---
    protected JournalEntry(Parcel in) {
        id = in.readString();
        title = in.readString();
        content = in.readString();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeParcelable(timestamp, flags);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<JournalEntry> CREATOR = new Creator<JournalEntry>() {
        @Override
        public JournalEntry createFromParcel(Parcel in) { return new JournalEntry(in); }
        @Override
        public JournalEntry[] newArray(int size) { return new JournalEntry[size]; }
    };
}