package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final MemberJPARepository memberJPARepository;

    @Transactional
    public Long join (Member member) {
        validateDuplicateMember(member);
        validateDuplicateEmail(member);
        memberRepository.save(member);
        return member.getId();
    }
    private void validateDuplicateEmail(Member member) {
        List<Member> findEmail = memberJPARepository.findByEmail(member.getEmail());

        if (!findEmail.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이메일 입니다");
        }
    }

    private void validateDuplicateMember(Member member) {

        List<Member> findMembers = memberRepository.findByName(member.getUsername());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이름 입니다");
        }
    }

    public  List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setUsername(name);
    }

}
