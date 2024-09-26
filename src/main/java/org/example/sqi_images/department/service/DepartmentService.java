package org.example.sqi_images.department.service;

import lombok.RequiredArgsConstructor;
import org.example.sqi_images.common.exception.NotFoundException;
import org.example.sqi_images.department.domain.Department;
import org.example.sqi_images.department.domain.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import static org.example.sqi_images.common.exception.type.ErrorType.DEPARTMENT_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public Department findExistingDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException(DEPARTMENT_NOT_FOUND_ERROR));
    }
}
