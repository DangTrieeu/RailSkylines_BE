package com.fourt.railskylines.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fourt.railskylines.domain.Role;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.domain.response.ResCreateUserDTO;
import com.fourt.railskylines.domain.response.ResUpdateUserDTO;
import com.fourt.railskylines.domain.response.ResUserDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.domain.response.VerifyCodeDTO;
import com.fourt.railskylines.domain.response.VerifyEmailDTO;
import com.fourt.railskylines.domain.response.ResUserDTO.RoleUser;
import com.fourt.railskylines.repository.RoleRepository;
import com.fourt.railskylines.repository.UserRepository;
import com.fourt.railskylines.util.CodeUtil;
import com.fourt.railskylines.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService mailService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,

            EmailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Check if the email exists in the database
    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User handleCreateNewUser(User newUser) {

        newUser.setStatus(true);
        newUser.setCode(CodeUtil.generateVerificationCode());
        newUser.setCodeExpired(Instant.now().plusSeconds(5 * 60)); // 5 minutes expiration
        User savedUser = userRepository.save(newUser);
        mailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getCode());
        return savedUser;
    }

    public User handleRegisterNewUser(User newUser) {
        Role normalUserRole = roleRepository.findByName("NORMAL_USER");
        if (normalUserRole != null) {
            newUser.setRole(normalUserRole);
        }
        newUser.setStatus(false);
        newUser.setCode(CodeUtil.generateVerificationCode());
        newUser.setCodeExpired(Instant.now().plusSeconds(5 * 60)); // 5 minutes expiration
        User savedUser = userRepository.save(newUser);
        mailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getCode());
        return savedUser;
    }

    // public void verifyCode(VerifyCodeDTO verifyCodeDTO) throws IdInvalidException
    // {
    // User user = userRepository.findByEmail(verifyCodeDTO.getEmail());

    // if (!user.getCode().equals(verifyCodeDTO.getCode())) {
    // throw new IdInvalidException(
    // "Verification code " + verifyCodeDTO.getCode() + " is not correct, please try
    // again");
    // }

    // if (user.getCodeExpired().isBefore(Instant.now())) {
    // throw new IdInvalidException("Verification code has expired, please request a
    // new one");
    // }

    // user.setStatus(true);
    // userRepository.save(user);
    // }

    // public void verifyEmail(VerifyEmailDTO verifyEmailDTO) throws
    // IdInvalidException {
    // User user = userRepository.findByEmail(verifyEmailDTO.getEmail());
    // String newCode = CodeUtil.generateVerificationCode();
    // user.setCode(newCode);
    // user.setCodeExpired(Instant.now().plusSeconds(5 * 60)); // 5 minutes
    // expiration
    // this.mailService.sendVerificationEmail(user.getEmail(), newCode);
    // this.userRepository.save(user);
    // }

    // verify code
    // Fetch user by id
    public User handleFetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.get();
    }

    // Fetch all users
    public ResultPaginationDTO handleFetchAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        res.setMeta(meta);

        // remove sensitive data
        List<ResUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        res.setResult(listUser);
        return res;
    }

    // Update user
    public User handleUpdateUser(long id, User updateUser) {
        User user = this.handleFetchUserById(id);
        if (user != null) {

            user.setFullName(updateUser.getFullName());
            user.setPhoneNumber(updateUser.getPhoneNumber());
            user.setAvatar(updateUser.getAvatar());
            user.setCitizenId(updateUser.getCitizenId());
            String hashPassword = this.passwordEncoder.encode(updateUser.getPassword());
            user.setPassword(hashPassword);
            user = this.userRepository.save(user);
        }
        return user;
    }

    // Delete user
    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    // Convert User to ResUserDTO
    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setUserId(user.getUserId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setCitizenId(user.getCitizenId());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatar(user.getAvatar());
        if (user.getRole() != null) {
            RoleUser roleUser = new RoleUser(user.getRole().getId(), user.getRole().getName());
            res.setRole(roleUser);
        }
        return res;
    }

    // Convert User to ResCreateUserDTO
    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setUserId(user.getUserId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setCitizenId(user.getCitizenId());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatar(user.getAvatar());
        res.setCreatedAt(user.getCreatedAt());
        res.setCodeExpired(user.getCodeExpired());
        return res;
    }

    // Convert User to ResUpdateUserDTO
    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setUserId(user.getUserId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setCitizenId(user.getCitizenId());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatar(user.getAvatar());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }

    public User handleGetUserByEmail(String username) {
        return this.userRepository.findByEmail(username);
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByEmail(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public User handleGetUserByUsername(String userName) {
        return this.userRepository.findByEmail(userName);
    }

    // verify email
    public void verifyEmail(VerifyEmailDTO verifyEmailDTO) throws IdInvalidException {
        String email = verifyEmailDTO.getEmail();

        if (!isValidEmail(email)) {
            throw new IdInvalidException("Định dạng email không hợp lệ");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {

            throw new IdInvalidException("Email chưa được đăng ký");
        }

        String newCode = CodeUtil.generateVerificationCode();
        user.setCode(newCode);
        user.setCodeExpired(Instant.now().plusSeconds(5 * 60)); // 5 phút hết hạn
        user.setStatus(false); // Đặt trạng thái để yêu cầu xác minh OTP
        userRepository.save(user);

        try {
            mailService.sendVerificationEmail(user.getEmail(), newCode);

        } catch (Exception e) {

            throw new IdInvalidException("Không thể gửi email xác minh, vui lòng thử lại sau");
        }
    }

    // Verify code
    public void verifyCode(VerifyCodeDTO verifyCodeDTO) throws IdInvalidException {
        String email = verifyCodeDTO.getEmail();
        String code = verifyCodeDTO.getCode();

        User user = userRepository.findByEmail(email);
        if (user == null) {

            throw new IdInvalidException("Email chưa được đăng ký");
        }

        if (user.getCode() == null || !user.getCode().equals(code)) {

            throw new IdInvalidException("Mã xác minh " + code + " không đúng, vui lòng thử lại");
        }

        if (user.getCodeExpired().isBefore(Instant.now())) {

            throw new IdInvalidException("Mã xác minh đã hết hạn, vui lòng yêu cầu mã mới");
        }

        user.setStatus(true); // Đánh dấu OTP đã được xác minh
        userRepository.save(user);

    }

    // change password
    public void resetPassword(String email, String verificationCode, String newPassword) throws IdInvalidException {

        if (!isValidEmail(email)) {
            throw new IdInvalidException("Định dạng email không hợp lệ");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {

            throw new IdInvalidException("Email chưa được đăng ký");
        }

        if (user.getCode() == null || !user.getCode().equals(verificationCode)
                || user.getCodeExpired().isBefore(Instant.now())) {

            throw new IdInvalidException("Mã xác minh không hợp lệ hoặc đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu
        user.setCode(null); // Xóa mã OTP
        user.setCodeExpired(null);
        user.setStatus(false); // Đặt lại trạng thái để yêu cầu xác minh mới
        userRepository.save(user);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }
}