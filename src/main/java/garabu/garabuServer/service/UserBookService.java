package garabu.garabuServer.service;

import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.repository.UserBookJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookService {

    private final UserBookJpaRepository userBookJpaRepository;

    /** 가계부-회원 매핑 등록 */
    @Transactional
    public Long registUserBook(UserBook userBook) {
        userBookJpaRepository.save(userBook);
        return userBook.getId();
    }

    /** bookId에 해당하는 소유자(UserBook) 리스트 조회 */
    public List<UserBook> findOwnersByBookId(Long bookId) {

        /* 1) 가계부 존재 여부 검증 */
        if (!userBookJpaRepository.existsByBookId(bookId)) {
            throw new EntityNotFoundException("해당 ID(" + bookId + ")의 가계부를 찾을 수 없습니다.");
        }

        /* 2) 소유자 조회 (Member를 FETCH JOIN) */
        List<UserBook> owners = userBookJpaRepository.findByBookId(bookId);

        /* 3) 소유자가 없을 경우 비즈니스 예외 발생 */
        if (owners.isEmpty()) {
            throw new EntityNotFoundException("bookId=" + bookId + " 에 대한 소유자 정보가 없습니다.");
        }

        return owners;
    }
}
