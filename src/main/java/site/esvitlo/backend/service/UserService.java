package site.esvitlo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.esvitlo.backend.domain.User;
import site.esvitlo.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreate(Long telegramId, String username) {

        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setTelegramId(telegramId);
                    user.setUsername(username);
                    return userRepository.save(user);
                });
    }
}

