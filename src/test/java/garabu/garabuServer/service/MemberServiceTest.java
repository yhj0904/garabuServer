/*

package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception{

     //given
     Member member = new Member();
     member.setUsername("test2");

     //when
        Long savedId = memberService.join(member);
     //then

        assertEquals(member, memberRepository.findOne(savedId));

     }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception{

        //given
        Member member1 = new Member();
        member1.setUsername("kim");

        Member member2 = new Member();
        member2.setUsername("kim");
        //when
        memberService.join(member1);
        memberService.join(member2);
//         try {
//             memberService.join(member2);
//         } catch (IllegalStateException e){
//             return;
//         }

        //then
        fail("예외 발생해야함");
    }
}
 */