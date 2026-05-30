package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequestStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRequestStatusRepository
        extends JpaRepository<RegistrationRequestStatusEntity, String> {
}
