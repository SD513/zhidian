package com.bupt.zhidian.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Data
@Accessors(chain = true)
@Document(collection = "notes")
public class Note implements Serializable {
    private static final long serialVersionUID = -3258839839160856613L;

    @Id
    private String id;

    private String userId;
    private NotesHeaderData notesHeaderData;
    private ArrayList<NotesInfoData> notesInfoDataArrayList;
}

class NotesHeaderData {
    String companyName;
    String dataBuyIn;
    String dataSellOut;
    String dataStopLoss;
    String dataTakeProfit;
    Time time;
    String title;
}

class Time {
    String notesDay;
    String notesHour;
    String notesMinute;
    String notesMonth;
    String notesYear;
}   

class NotesInfoData {
    ArrayList<String> _content;
}