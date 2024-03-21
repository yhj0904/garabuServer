package garabu.garabuServer.service;

import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBookService {
    private final UserBookJpaRepository userBookJpaRepository;

    public Long registUserBook(UserBook userBook){
        userBookJpaRepository.save(userBook);
        return userBook.getId();
    }
}
