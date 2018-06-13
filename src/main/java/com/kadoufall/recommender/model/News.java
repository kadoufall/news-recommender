package com.kadoufall.recommender.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "news")
@Data
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "passageContent")
    private String passageContent;

    @Column(name = "postTime")
    private Timestamp postTime;

    @Column(name = "viewNum")
    private int viewNum;

    @Column(name = "commentNum")
    private int commentNum;

}
