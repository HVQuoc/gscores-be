package com.gotest.gscore.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "scores")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sbd", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_code", referencedColumnName = "code", nullable = false)
    private Subject subject;

    @Column(name = "score")
    private Double score;
}

