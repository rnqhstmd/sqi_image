package org.example.sqi_images.employee.service;

import lombok.RequiredArgsConstructor;
import org.example.sqi_images.common.exception.NotFoundException;
import org.example.sqi_images.employee.dto.response.SearchEmployeeResponse;
import org.example.sqi_images.employee.domain.Employee;
import org.example.sqi_images.employee.domain.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.example.sqi_images.common.exception.type.ErrorType.EMPLOYEE_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<SearchEmployeeResponse> searchEmployees(String email) {
        List<Employee> employees = employeeRepository.findByEmailContaining(email);
        return employees.stream()
                .map(SearchEmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean verifyEmployeeIdsExist(Set<Long> employeeIds) {
        return employeeRepository.allExistByIds(employeeIds, employeeIds.size());
    }

    public Employee findExistingEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND_ERROR));
    }

    public Employee findEmployeeWithDetails(Long employeeId) {
        return employeeRepository.findByIdWithDetail(employeeId)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND_ERROR));
    }
}
