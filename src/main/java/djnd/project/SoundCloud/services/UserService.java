package djnd.project.SoundCloud.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import djnd.project.SoundCloud.domain.ResLoginDTO;
import djnd.project.SoundCloud.domain.entity.User;
import djnd.project.SoundCloud.domain.request.users.UpdatePassword;
import djnd.project.SoundCloud.domain.request.users.UserDTO;
import djnd.project.SoundCloud.domain.request.users.UserUpdateDTO;
import djnd.project.SoundCloud.domain.response.ResultPaginationDTO;
import djnd.project.SoundCloud.domain.response.users.ResUser;
import djnd.project.SoundCloud.repositories.RoleRepository;
import djnd.project.SoundCloud.repositories.UserRepository;
import djnd.project.SoundCloud.utils.SecurityUtils;
import djnd.project.SoundCloud.utils.convert.convertUtils;
import djnd.project.SoundCloud.utils.error.DuplicateResourceException;
import djnd.project.SoundCloud.utils.error.PasswordMismatchException;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    SecurityUtils securityUtils;
    SessionManager sessionManager;
    RoleRepository roleRepository;
    MailService mailService;
    FileService fileService;
    // private final UserMapper userMapper;

    public Long create(UserDTO dto) {
        if (this.userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email", dto.getEmail());
        }
        var user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setAccept(false);
        user.setPassword(dto.getManagementPassword().getPassword());
        var lastUser = this.userRepository.save(user);
        return lastUser.getId();
    }

    public ResUser updatePartial(UserUpdateDTO dto) {
        var user = this.userRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getId() + ""));
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            user.setEmail(dto.getEmail());
        }
        var lastUser = this.userRepository.save(user);
        return convertUtils.toResUser(lastUser);

    }

    public ResUser update(UserUpdateDTO dto) {
        var user = this.userRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ID", dto.getId() + ""));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        var lastUser = this.userRepository.save(user);
        return convertUtils.toResUser(lastUser);
    }

    public ResUser findById(long id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ID", id + ""));
        return convertUtils.toResUser(user);
    }

    public void deleteById(long id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ID", ""));
        this.userRepository.delete(user);
    }

    public ResultPaginationDTO fetchAll(Specification<User> spec, Pageable pageable) {
        var page = this.userRepository.findAll(spec, pageable);
        var res = new ResultPaginationDTO();
        var mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        res.setMeta(mt);
        res.setResult(page.getContent().stream().map(u -> {
            var resUser = convertUtils.toResUser(u);
            return resUser;
        }).collect(Collectors.toList()));
        return res;
    }

    public void updateRefreshTokenByEmail(String email, String refreshToken) {
        var user = this.userRepository.findByEmail(email);
        if (user != null) {
            user.setRefreshToken(refreshToken);
            this.userRepository.save(user);
        }
    }

    public long register(UserDTO dto) {
        if (this.userRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("Email User", dto.getEmail());
        }
        var user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAccept(false);
        user.setPassword(this.passwordEncoder.encode(dto.getManagementPassword().getConfirmPassword()));
        user.setRole(this.roleRepository.findByName("USER_NORMAL"));
        var lastUser = this.userRepository.save(user);
        return lastUser.getId();
    }

    public ResLoginDTO handleRefreshTokenWithCondition(String refreshToken, String condition) {
        var res = new ResLoginDTO();
        var userLogin = new ResLoginDTO.UserLogin();
        var decodedToken = this.securityUtils.checkValidRefreshToken(refreshToken);
        var email = decodedToken.getSubject();
        if (email == null) {
            throw new BadCredentialsException("Refresh Token Invalid!");
        }
        var user = this.userRepository.findByEmailAndRefreshToken(email, refreshToken);
        if (user != null) {
            if (condition.equals("delete")) {
                this.sessionManager.invalidateSession(email);
                updateRefreshTokenByEmail(email, null);
                return new ResLoginDTO();
            }
            if (condition.equals("refresh")) {
                userLogin.setEmail(email);
                userLogin.setId(user.getId());
                userLogin.setName(user.getName());
                userLogin.setRole(user.getRole().getName());
                res.setUser(userLogin);
                var sessionID = this.sessionManager.createNewSession(user);
                var accessToken = this.securityUtils.createAccessToken(email, res, sessionID);
                res.setAccessToken(accessToken);
                var newRefreshToken = this.securityUtils.createRefreshToken(email, res);
                updateRefreshTokenByEmail(email, newRefreshToken);
                res.setRefreshToken(newRefreshToken);
                return res;
            }
        }
        throw new BadCredentialsException("Refresh Token Invalid!");
    }

    @CacheEvict(value = "userAccount", key = "'USER_ACCOUNT_' + #email")
    public void logout(String email) {
        var user = this.userRepository.findByEmail(email);
        if (user != null) {
            user.setRefreshToken(null);
            user.setSessionId(null);
            this.userRepository.save(user);
        }
    }

    @Cacheable(value = "userAccount", key = "'USER_ACCOUNT_' + #email")
    public ResLoginDTO.UserGetAccount getAccount() {
        var email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("You do not have access!"));
        var user = this.userRepository.findByEmail(email);
        if (user != null) {
            var res = new ResLoginDTO.UserGetAccount();
            var userLogin = new ResLoginDTO.UserLogin();
            userLogin.setEmail(user.getEmail());
            userLogin.setName(user.getName());
            userLogin.setId(user.getId());
            userLogin.setRole(user.getRole().getName());
            res.setUser(userLogin);
            return res;
        }
        throw new ResourceNotFoundException("Account", email);
    }

    public boolean updatePassword(UpdatePassword dto) {
        var email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("You do not have permission!"));
        var user = this.userRepository.findByEmail(email);
        if (user != null) {
            if (this.passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                if (dto.getManagementPassword().getPassword()
                        .equals(dto.getManagementPassword().getConfirmPassword())) {
                    user.setPassword(this.passwordEncoder.encode(dto.getManagementPassword().getConfirmPassword()));
                    this.userRepository.save(user);
                    // this.mailService.sendOTPToEmail(user, "Mật khẩu của bạn vừa được thay đổi",
                    // false);

                    this.mailService.setUpAndSendFormUpdatePassword(convertUtils.toResUser(user));
                    return true;
                }
            } else {
                throw new PasswordMismatchException("Current Password Incorrect!");
            }
        }
        return false;
    }

    public void forgotPasword(UserDTO dto) {
        var user = this.userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            this.mailService.sendOTPToEmail(user, " Là Mã Khôi Phục Mật Khẩu Sound Clound Account Của Bạn", true);
        } else {
            throw new ResourceNotFoundException("User Email", dto.getEmail());
        }
    }

    public boolean verifyOTP(UserDTO dto) {
        var user = this.userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            if (!user.isOTPRequired()) {
                throw new BadCredentialsException("OTP expires!");
            }
            if (this.passwordEncoder.matches(dto.getOneTimePassword(), user.getOneTimePassword())) {
                user.setAccept(true);
                this.userRepository.save(user);
                return true;
            } else {
                throw new PasswordMismatchException("OTP wrong!");
            }
        } else {
            return false;
        }
    }

    public boolean updatePassword(UserDTO dto) {
        var user = this.userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            if (user.isAccept()) {
                if (dto.getManagementPassword().getConfirmPassword()
                        .equals(dto.getManagementPassword().getPassword())) {
                    user.setPassword(this.passwordEncoder.encode(dto.getManagementPassword().getConfirmPassword()));
                    user.setRefreshToken(null);
                    this.sessionManager.invalidateSession(user.getEmail());
                    user.setAccept(false);
                    this.userRepository.save(user);
                    return true;
                } else {
                    throw new PasswordMismatchException("Password and Confirm Password is not the same!");
                }
            } else {
                throw new BadCredentialsException("You cannot update password!");
            }

        } else {
            return false;
        }
    }

    /*
     * file: avatar file
     */
    public boolean updateAvatarUser(MultipartFile file) throws URISyntaxException, IOException {
        var email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("You cannot upload avatar!"));

        var user = this.userRepository.findByEmail(email);
        if (user != null) {
            if (file != null && !file.isEmpty()) {
                var allowFile = Arrays.asList("jpg", "jpeg", "png");
                if (allowFile.stream().anyMatch(x -> file.getOriginalFilename().toLowerCase().endsWith(x))) {

                    user.setAvatar(this.fileService.getFinalNameAvatarFile(file));
                    this.userRepository.save(user);
                    return true;
                }
                return false;
            }

        }
        return false;
    }

}
