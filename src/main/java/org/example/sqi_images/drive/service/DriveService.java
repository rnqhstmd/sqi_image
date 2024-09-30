package org.example.sqi_images.drive.service;

import lombok.RequiredArgsConstructor;
import org.example.sqi_images.common.dto.page.request.PageRequestDto;
import org.example.sqi_images.common.dto.page.response.PageResultDto;
import org.example.sqi_images.common.exception.NotFoundException;
import org.example.sqi_images.drive.domain.Drive;
import org.example.sqi_images.drive.domain.DriveEmployee;
import org.example.sqi_images.drive.domain.repository.DriveEmployeeRepository;
import org.example.sqi_images.drive.domain.repository.DriveRepository;
import org.example.sqi_images.drive.dto.request.AssignRoleRequest;
import org.example.sqi_images.drive.dto.request.AssignRoleRequestList;
import org.example.sqi_images.drive.dto.request.CreateDriveDto;
import org.example.sqi_images.employee.domain.Employee;
import org.example.sqi_images.employee.service.EmployeeService;
import org.example.sqi_images.file.domain.FileInfo;
import org.example.sqi_images.file.domain.repository.FileInfoRepository;
import org.example.sqi_images.file.dto.response.FileDownloadDto;
import org.example.sqi_images.file.dto.response.FileInfoResponseDto;
import org.example.sqi_images.file.service.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.sqi_images.common.exception.type.ErrorType.*;
import static org.example.sqi_images.drive.domain.DriveAccessType.ADMIN;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final FileService fileService;
    private final EmployeeService employeeService;
    private final FileInfoRepository fileInfoRepository;
    private final DriveRepository driveRepository;
    private final DriveEmployeeRepository driveEmployeeRepository;

    public void createDrive(Employee employee, CreateDriveDto createDriveDto) {
        Drive drive = new Drive(createDriveDto.driveName());
        driveRepository.save(drive);

        DriveEmployee driveEmployee = new DriveEmployee(employee, drive, ADMIN);
        driveEmployeeRepository.save(driveEmployee);
    }

    @Transactional
    public void assignAndUpdateRoles(Long driveId, AssignRoleRequestList request) {
        Drive drive = findExistingDrive(driveId);

        // 요청된 employeeIds 추출
        Set<Long> employeeIds = request.employeeRoles().stream()
                .map(AssignRoleRequest::employeeId)
                .collect(Collectors.toSet());

        // 해당 driveId와 employeeIds 에 해당하는 모든 권한 조회
        Map<Long, DriveEmployee> employeesMap = driveEmployeeRepository.findByDriveIdAndEmployeeIds(driveId, employeeIds)
                .stream()
                .collect(Collectors.toMap(DriveEmployee::getEmployeeId, Function.identity()));

        // 요청된 모든 직원 ID가 실제로 존재하는지 확인
        if (!employeeService.verifyEmployeeIdsExist(employeeIds)) {
            throw new NotFoundException(EMPLOYEE_NOT_FOUND_ERROR);
        }

        List<DriveEmployee> toUpdate = new ArrayList<>();
        List<DriveEmployee> toDelete = new ArrayList<>();

        request.employeeRoles().forEach(assignRoleRequest -> {
            DriveEmployee existingEmployee = employeesMap.get(assignRoleRequest.employeeId());
            if (existingEmployee != null) {
                if (assignRoleRequest.role() == null) {
                    toDelete.add(existingEmployee);
                } else {
                    existingEmployee.setRole(assignRoleRequest.role());
                    toUpdate.add(existingEmployee);
                }
            } else {
                // 존재하지 않는 직원 ID에 대한 처리는 필요 없음, 이미 검사 완료
                Employee newEmployee = employeeService.findExistingEmployee(assignRoleRequest.employeeId());
                toUpdate.add(new DriveEmployee(newEmployee, drive, assignRoleRequest.role()));
            }
        });

        if (!toDelete.isEmpty()) {
            driveEmployeeRepository.deleteAllInBatch(toDelete);
        }
        if (!toUpdate.isEmpty()) {
            driveEmployeeRepository.saveAll(toUpdate);
        }
    }

    public void uploadFileToDrive(Employee employee, Long driveId, MultipartFile file) throws IOException {
        Drive drive = findExistingDrive(driveId);
        fileService.saveFile(file, employee, drive);
    }

    public FileDownloadDto downloadDriveFile(Long driveId, Long fileId) {
        if (!driveRepository.existsById(driveId)) {
            throw new NotFoundException(DRIVE_NOT_FOUND_ERROR);
        }
        FileInfo fileInfo = fileService.findFileInfoByDriveId(fileId, driveId);

        return fileService.downloadFile(fileInfo);
    }

    public PageResultDto<FileInfoResponseDto, FileInfo> getAllDriveFiles(Long driveId, int page) {
        PageRequestDto pageRequestDto = new PageRequestDto(page);
        Pageable pageable = pageRequestDto.toPageable();
        Page<FileInfo> result = fileInfoRepository.findByDriveIdWithFetchJoin(driveId, pageable);

        return new PageResultDto<>(result, FileInfoResponseDto::from);
    }

    private Drive findExistingDrive(Long driveId) {
        return driveRepository.findById(driveId)
                .orElseThrow(() -> new NotFoundException(DRIVE_NOT_FOUND_ERROR));
    }
}
