package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequestStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRequestStatusHistoryRepository
        extends JpaRepository<RegistrationRequestStatusHistory, Long> {

    List<RegistrationRequestStatusHistory> findByRegistrationRequestIdOrderByChangedAtAsc(Long registrationRequestId);

}
