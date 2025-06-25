package com.gotest.gscore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "students")
public class Student {
    @Id
    @Column(name = "sbd", length = 20)
    private String sbd;

    @Column(name = "ma_ngoai_ngu", length = 10)
    private String ma_ngoai_ngu;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scores = new ArrayList<>();
}

