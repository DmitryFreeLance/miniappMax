package com.maxminiapp.service;

import com.maxminiapp.exception.ForbiddenException;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser getOrCreateByMaxUserId(Long maxUserId) {
        return appUserRepository.findByMaxUserId(maxUserId)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setMaxUserId(maxUserId);
                    user.setAdmin(false);
                    return appUserRepository.save(user);
                });
    }

    public void requireAdmin(Long maxUserId) {
        AppUser user = appUserRepository.findByMaxUserId(maxUserId)
                .orElseThrow(() -> new ForbiddenException("Нет доступа: пользователь не найден"));
        if (!user.isAdmin()) {
            throw new ForbiddenException("Нет доступа: нужны права администратора");
        }
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Transactional
    public AppUser grantAdmin(Long maxUserId) {
        AppUser user = getOrCreateByMaxUserId(maxUserId);
        user.setAdmin(true);
        return appUserRepository.save(user);
    }

    public List<AppUser> findAdmins() {
        return appUserRepository.findByAdminTrueOrderByCreatedAtDesc();
    }
}
