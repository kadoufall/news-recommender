package com.kadoufall.recommender.model;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "userOperation")
@Data
public class UserOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "userId")
    private long userId;

    @Column(name = "newsId")
    private long newsId;

    @Column(name = "readTime")
    private Timestamp readTime;

    @Column(name = "preference")
    private float preference;

    public UserOperation() {

    }
}

