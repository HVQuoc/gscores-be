package com.gotest.gscore.service;

import com.gotest.gscore.dto.SubjectDTO;
import com.gotest.gscore.repository.SubjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepo subjectRepo;
    public List<SubjectDTO> getAllSubjects() {
        return subjectRepo.findAll().stream()
                .map(s -> new SubjectDTO(s.getCode(), s.getName()))
                .collect(Collectors.toList());
    }
}
