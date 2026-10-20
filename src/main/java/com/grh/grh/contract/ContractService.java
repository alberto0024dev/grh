package com.grh.grh.contract;

import com.grh.grh.employee.Employee;
import com.grh.grh.employee.EmployeeRepository;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.usuario.ApplicationUserRole;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final UsuarioRepository usuarioRepository;


    @Transactional // se añade por que haces dos operaciones de escritura en la bd (hace rollback tambien si falla uno de los dos).
    public ContractResponseDTO create(ContractRequestDTO contractRequestDTO) {
        Employee employee= employeeRepository.findById(contractRequestDTO.getEmployeeId())
                .orElseThrow(()-> new ResourceNotFoundException("Empleado para el contrato no encontrado"));
        if (employee.getContract() != null) {
            throw new ConflictException("Este empleado ya tiene un contrato. Edítalo en vez de crear otro.");
        }
        Contract contract = Contract.toEntity(contractRequestDTO, employee);
        Contract saved  = contractRepository.save(contract);
        employee.setContract(saved);
        employeeRepository.save(employee);
        return ContractResponseDTO.fromEntity(saved);
    }

    public ContractResponseDTO update(Long id,ContractUpdateDTO dto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: "+id));
        updateContract(dto, contract);
        Contract saved = contractRepository.save(contract);
        return ContractResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));
        Employee owner = contract.getEmployee();
        if (owner != null) {
            owner.setContract(null);
            employeeRepository.save(owner);
        }
        contractRepository.delete(contract);
    }

    public ContractResponseDTO getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));
        String emailLogged = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(emailLogged)
                .orElseThrow();
        if(userLogged.getRole() == ApplicationUserRole.ADMIN){
            return ContractResponseDTO.fromEntity(contract);
        }

        Employee employeeLogged = employeeRepository.findByUsuario(userLogged).orElseThrow();
        Employee owner = contract.getEmployee();
        if (owner == null) {
            throw new ForbiddenAccessException("No tienes acceso a este contrato.");
        }

        boolean isOwn = owner.getId().equals(employeeLogged.getId());
        boolean sameDepartment = userLogged.getRole() == ApplicationUserRole.MANAGER
                && owner.getDepartment() != null
                && employeeLogged.getDepartment() != null
                && owner.getDepartment().getId().equals(employeeLogged.getDepartment().getId());

        if (!isOwn && !sameDepartment) {
            throw new ForbiddenAccessException("No tienes acceso a este contrato.");
        }
        return ContractResponseDTO.fromEntity(contract);
    }

    public List<ContractResponseDTO> getAll() {
        return contractRepository.findAll()
                .stream()
                .map(ContractResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractResponseDTO getByEmployeeId(Long id) {
        String emailLogged = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario userLogged = usuarioRepository.findByEmail(emailLogged).orElseThrow();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));

        if (userLogged.getRole() != ApplicationUserRole.ADMIN) {
            Employee logged = employeeRepository.findByUsuario(userLogged).orElseThrow();
            boolean isOwn = logged.getId().equals(employee.getId());
            boolean sameDepartment = userLogged.getRole() == ApplicationUserRole.MANAGER
                    && employee.getDepartment() != null
                    && logged.getDepartment() != null
                    && employee.getDepartment().getId().equals(logged.getDepartment().getId());
            if (!isOwn && !sameDepartment) {
                throw new ForbiddenAccessException("No tienes acceso a este contrato.");
            }
        }

        Contract contract = contractRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("El empleado no tiene contrato"));
        return ContractResponseDTO.fromEntity(contract);
    }

    private void updateContract(ContractUpdateDTO dto, Contract contract) {
        contract.setContractType(dto.getContractType());
        contract.setSalary(dto.getSalary());
        contract.setCommencementDate(dto.getCommencementDate());
        contract.setExpiryDate(dto.getExpiryDate());
        contract.setVacationDays(dto.getVacationDays());
        contract.setWorkPlace(dto.getWorkPlace());
        contract.setWorkSchedule(dto.getWorkSchedule());
    }
}
